package io.github.acoboh.query.filter.mongodb.processor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.util.Pair;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import io.github.acoboh.query.filter.mongodb.exceptions.QFBlockException;
import io.github.acoboh.query.filter.mongodb.exceptions.QFDiscriminatorNotFoundException;
import io.github.acoboh.query.filter.mongodb.exceptions.QFFieldNotFoundException;
import io.github.acoboh.query.filter.mongodb.exceptions.QFJsonParseException;
import io.github.acoboh.query.filter.mongodb.exceptions.QFMultipleSortException;
import io.github.acoboh.query.filter.mongodb.exceptions.QFNotSortableException;
import io.github.acoboh.query.filter.mongodb.exceptions.QFNotValuable;
import io.github.acoboh.query.filter.mongodb.exceptions.QFOperationNotFoundException;
import io.github.acoboh.query.filter.mongodb.exceptions.QFParseException;
import io.github.acoboh.query.filter.mongodb.operations.QFOperationEnum;
import io.github.acoboh.query.filter.mongodb.operations.QFOperationTextEnum;
import io.github.acoboh.query.filter.mongodb.processor.definitions.IDefinitionSortable;
import io.github.acoboh.query.filter.mongodb.processor.definitions.QFAbstractDefinition;
import io.github.acoboh.query.filter.mongodb.processor.definitions.QFDefinitionElement;
import io.github.acoboh.query.filter.mongodb.processor.definitions.QFDefinitionText;
import io.github.acoboh.query.filter.mongodb.processor.match.QFElementMatch;
import io.github.acoboh.query.filter.mongodb.processor.match.QFTextMatch;
import io.github.acoboh.query.filter.mongodb.processor.projection.ProjectionDefinition;
import io.github.acoboh.query.filter.mongodb.spel.SpelResolverInterface;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class QueryFilter<E> {

	private static final Logger LOGGER = LoggerFactory.getLogger(QueryFilter.class);

	private static final String FIELD_NOT_NULL_MESSAGE = "field cannot be null";
	private static final String OPERATION_NOT_NULL_MESSAGE = "operation cannot be null";
	private static final String VALUES_NOT_NULL_MESSAGE = "values cannot be null";

	private static final Pattern REGEX_PATTERN = Pattern.compile("([+-])([a-zA-Z0-9]+)");
	private static final String LOG_FINAL_PIPELINE = "Final generated aggregate pipeline: '{}'";

	private final String initialInput;

	private final QFSpecificationsWrap specificationsWarp;

	private final List<Pair<IDefinitionSortable, Sort.Direction>> defaultSorting;

	private final Map<String, QFAbstractDefinition> definitionMap;

	private boolean defaultSortEnabled = true;

	private final Class<E> entityClass;
	private final Class<?> predicateClass;
	private final SpelResolverInterface spelResolver;
	private final List<Pair<IDefinitionSortable, Sort.Direction>> sortDefinitionList = new ArrayList<>();
	private boolean isConstructor = true;

	private final Map<Class<?>, ProjectionDefinition> mapProjections;

	private final ReactiveMongoTemplate mongoTemplate;

	/**
	 * Create a new QueryFilter instance with the given input, type, and processor.
	 *
	 * @param input     the query filter input
	 * @param type      the query filter parameter type
	 * @param processor the query filter processor
	 */
	protected QueryFilter(String input, QFParamType type, QFProcessor<?, E> processor) {
		Assert.notNull(type, "type cannot be null");

		this.definitionMap = processor.getDefinitionMap();
		this.mapProjections = processor.getMapProjections();

		this.specificationsWarp = new QFSpecificationsWrap(processor.getDefaultMatches());

		this.defaultSorting = processor.getDefaultSorting();
		this.entityClass = processor.getEntityClass();
		this.predicateClass = processor.getFilterClass();
		this.spelResolver = processor.getApplicationContext().getBean(SpelResolverInterface.class);

		this.initialInput = input != null ? input : "";

		if (input != null && !input.isEmpty()) {

			var matcher = type.getPattern().matcher(input);
			while (matcher.find()) {
				if (LOGGER.isTraceEnabled()) {
					LOGGER.trace("Found match: {}", matcher.group());
				}

				if (matcher.group(1) != null) {
					parseSortPart(matcher.group(2));
				} else if (matcher.group(3) != null) {
					parseValuePart(matcher.group(4), matcher.group(5), matcher.group(6));
				} else {
					throw new QFParseException(matcher.group(), input);
				}

			}

		}

		isConstructor = false;

		this.mongoTemplate = processor.getApplicationContext().getBean(ReactiveMongoTemplate.class);

	}

	private void parseValuePart(String field, String op, String value)
			throws QFParseException, QFFieldNotFoundException, QFOperationNotFoundException,
			QFDiscriminatorNotFoundException, QFBlockException, QFJsonParseException, QFNotValuable {

		QFAbstractDefinition def = definitionMap.get(field);
		if (def == null) {
			throw new QFFieldNotFoundException(field);
		}

		if (def.isConstructorBlocked() && isConstructor) {
			throw new QFBlockException(field);
		}

		QFSpecificationPart qfSpecificationPart;
		if (def instanceof QFDefinitionElement qdef) {
			var operation = op == null ? QFOperationEnum.EQUAL : QFOperationEnum.fromValue(op);
			qfSpecificationPart = new QFElementMatch(Arrays.asList(value.split(",")), operation, qdef);
		} else if (def instanceof QFDefinitionText qdef) {
			var operation = op == null ? QFOperationTextEnum.EQUAL : QFOperationTextEnum.fromValue(op);
			qfSpecificationPart = new QFTextMatch(value, operation, qdef);
		} else {
			throw new QFNotValuable(field);
		}

		specificationsWarp.addSpecification(qfSpecificationPart);

	}

	private void parseSortPart(String values)
			throws QFParseException, QFNotSortableException, QFMultipleSortException, QFFieldNotFoundException {

		Matcher matcher = REGEX_PATTERN.matcher(values);
		boolean match = false;
		while (matcher.find()) {
			match = true;

			String order = matcher.group(1);
			String fieldName = matcher.group(2);

			QFAbstractDefinition def = definitionMap.get(fieldName);
			if (def == null) {
				throw new QFFieldNotFoundException(fieldName);
			}

			if (!(def instanceof IDefinitionSortable)) {
				throw new QFNotSortableException(fieldName);
			}

			if (this.sortDefinitionList.stream().anyMatch(e -> e.getFirst().getFilterName().equals(fieldName))) {
				throw new QFMultipleSortException(fieldName);
			}

			Sort.Direction dir;
			if (order.equals("+")) {
				dir = Sort.Direction.ASC;
			} else {
				dir = Sort.Direction.DESC;
			}

			Pair<IDefinitionSortable, Sort.Direction> pair = Pair.of((IDefinitionSortable) def, dir);
			this.sortDefinitionList.add(pair);
			this.defaultSortEnabled = false;

		}

		if (!match) {
			throw new QFParseException(values, initialInput);
		}

	}

	/**
	 * Get the input used on the constructor
	 *
	 * @return original input
	 */
	public String getInitialInput() {
		return initialInput;
	}

	/**
	 * Get the entity class
	 *
	 * @return entity class
	 */
	public Class<E> getEntityClass() {
		return entityClass;
	}

	/**
	 * Get the predicate class
	 *
	 * @return predicate class
	 */
	public Class<?> getPredicateClass() {
		return predicateClass;
	}

	/**
	 * Manually adds a new operation on any field
	 *
	 * @param field     field of filter
	 * @param operation operation to be applied
	 * @param values    values to match
	 * @throws QFFieldNotFoundException if the field is not found
	 */
	public void addNewField(String field, QFOperationEnum operation, List<String> values) {
		Assert.notNull(field, FIELD_NOT_NULL_MESSAGE);
		Assert.notNull(operation, OPERATION_NOT_NULL_MESSAGE);
		Assert.notNull(values, VALUES_NOT_NULL_MESSAGE);

		QFAbstractDefinition def = definitionMap.get(field);
		if (def == null) {
			throw new QFFieldNotFoundException(field);
		}

		if (!(def instanceof QFDefinitionElement)) {
			throw new QFNotValuable(field);
		}

		specificationsWarp.addSpecification(new QFElementMatch(values, operation, (QFDefinitionElement) def));

	}

	/**
	 * Manually adds a new operation on any text field
	 *
	 * @param field     field of filter
	 * @param operation operation to be applied
	 * @param value     value to match
	 * @throws QFFieldNotFoundException if the field is not found
	 */
	public void addNewField(String field, QFOperationTextEnum operation, String value) {
		Assert.notNull(field, FIELD_NOT_NULL_MESSAGE);
		Assert.notNull(operation, OPERATION_NOT_NULL_MESSAGE);
		Assert.notNull(value, VALUES_NOT_NULL_MESSAGE);

		QFAbstractDefinition def = definitionMap.get(field);
		if (def == null) {
			throw new QFFieldNotFoundException(field);
		}

		if (!(def instanceof QFDefinitionText)) {
			throw new QFNotValuable(field);
		}

		specificationsWarp.addSpecification(new QFTextMatch(value, operation, (QFDefinitionText) def));
	}

	/**
	 * Override sort configuration
	 *
	 * @param field     Field name of sorting
	 * @param direction Direction of sorting
	 * @throws QFNotSortableException   not sortable
	 * @throws QFFieldNotFoundException if field does not exist
	 * @throws QFMultipleSortException  if multiple sort exists on the same field
	 */
	public void addSortBy(String field, Sort.Direction direction) {

		Assert.notNull(field, FIELD_NOT_NULL_MESSAGE);
		Assert.notNull(direction, "direction cannot be null");

		QFAbstractDefinition def = definitionMap.get(field);
		if (def == null) {
			throw new QFFieldNotFoundException(field);
		}

		if (!(def instanceof IDefinitionSortable isort) || !isort.isSortable()) {
			throw new QFNotSortableException(field);
		}

		if (this.sortDefinitionList.stream().anyMatch(e -> e.getFirst().getFilterName().equals(field))) {
			throw new QFMultipleSortException(field);
		}

		this.sortDefinitionList.add(Pair.of(isort, direction));
		this.defaultSortEnabled = false;

	}

	/**
	 * Remove the actual sort configuration
	 */
	public void clearSort() {
		this.defaultSortEnabled = false;
		this.sortDefinitionList.clear();
	}

	/**
	 * Get if any sort filter is applied
	 *
	 * @return true if any sort is applied, false otherwise
	 */
	public boolean isSorted() {
		List<Pair<IDefinitionSortable, Sort.Direction>> list = defaultSortEnabled ? defaultSorting : sortDefinitionList;
		return !list.isEmpty();
	}

	/**
	 * Get all the sort fields
	 *
	 * @return list of a pair of sorting fields
	 */
	public List<Pair<String, Sort.Direction>> getSortFields() {
		List<Pair<IDefinitionSortable, Sort.Direction>> list = defaultSortEnabled ? defaultSorting : sortDefinitionList;
		return list.stream().map(e -> Pair.of(e.getFirst().getFilterName(), e.getSecond()))
				.collect(Collectors.toList());
	}

	/**
	 * Get if the filter is sorted by the selected field
	 *
	 * @param field field to check
	 * @return true if is actually sorting, false otherwise
	 */
	public boolean isSortedBy(String field) {
		List<Pair<IDefinitionSortable, Sort.Direction>> list = defaultSortEnabled ? defaultSorting : sortDefinitionList;
		return list.stream().anyMatch(e -> e.getFirst().getFilterName().equals(field));
	}

	/**
	 * Check if the field is currently used for filtering
	 *
	 * @param field Filter name to check
	 * @return true if the field is present, false otherwise
	 */
	public boolean isFiltering(String field) {
		return specificationsWarp.getAllParts().stream().anyMatch(e -> e.getDefinition().getFilterName().equals(field));
	}

	/**
	 * Check if any of the fields is are currently used for filtering
	 *
	 * @param fields Filter names to be checked
	 * @return true, if any of the fields are present, false is all of them are
	 *         actually missing
	 */
	public boolean isFilteringAny(String... fields) {
		Set<String> set = Set.of(fields);
		return specificationsWarp.getAllParts().stream().anyMatch(e -> set.contains(e.getDefinition().getFilterName()));
	}

	/**
	 * Override any field. If not present, a new field will be created
	 *
	 * @param field     Field filter name
	 * @param operation Operation to apply
	 * @param value     value of filter
	 * @throws QFFieldNotFoundException Missing field exception
	 * @throws QFNotValuable            if the field is not valuable or type
	 *                                  compatible
	 */
	public void overrideField(String field, QFOperationEnum operation, String value)
			throws QFFieldNotFoundException, QFNotValuable {

		Assert.notNull(field, FIELD_NOT_NULL_MESSAGE);
		Assert.notNull(operation, OPERATION_NOT_NULL_MESSAGE);
		Assert.notNull(value, "value cannot be null");

		QFAbstractDefinition def = definitionMap.get(field);
		if (def == null) {
			throw new QFFieldNotFoundException(field);
		}

		if (!(def instanceof QFDefinitionElement)) {
			throw new QFNotValuable(field);
		}

		specificationsWarp.deleteSpecificationField(field);
		QFElementMatch match = new QFElementMatch(Arrays.asList(value.split(",")), operation,
				(QFDefinitionElement) def);
		specificationsWarp.addSpecification(match);

	}

	/**
	 * Override any text field. If not present, a new field will be created
	 * 
	 * @param field     Field filter name
	 * @param operation Operation to apply
	 * @param value     value of filter
	 */
	public void overrideField(String field, QFOperationTextEnum operation, String value) {

		Assert.notNull(field, FIELD_NOT_NULL_MESSAGE);
		Assert.notNull(operation, OPERATION_NOT_NULL_MESSAGE);
		Assert.notNull(value, "value cannot be null");

		QFAbstractDefinition def = definitionMap.get(field);
		if (def == null) {
			throw new QFFieldNotFoundException(field);
		}

		if (!(def instanceof QFDefinitionText)) {
			throw new QFNotValuable(field);
		}

		specificationsWarp.deleteSpecificationField(field);
		QFTextMatch match = new QFTextMatch(value, operation, (QFDefinitionText) def);
		specificationsWarp.addSpecification(match);
	}

	/**
	 * Get the actual value of a field
	 * 
	 * @param field Field name
	 * @return a flux of the list of values
	 * @throws QFFieldNotFoundException if the field is not found
	 */
	public Flux<List<Object>> getActualValue(String field) throws QFFieldNotFoundException {
		QFAbstractDefinition def = definitionMap.get(field);
		if (def == null) {
			throw new QFFieldNotFoundException(field);
		}

		var qfSpec = specificationsWarp.getAllParts().stream()
				.filter(e -> e.getDefinition().getFilterName().equals(field)).findFirst().orElse(null);

		if (qfSpec == null) {
			return Flux.empty();
		} else if (qfSpec instanceof QFElementMatch qfElement) {
			return qfElement.getParsedValues();
		} else if (qfSpec instanceof QFTextMatch qfText) {
			return Flux.just(List.of(qfText.getValue()));
		} else {
			return Flux.empty();
		}

	}

	/**
	 * Delete the field of the filter
	 *
	 * @param field field to delete
	 * @throws QFFieldNotFoundException if the is not present
	 */
	public void deleteField(String field) {
		QFAbstractDefinition def = definitionMap.get(field);
		if (def == null) {
			throw new QFFieldNotFoundException(field);
		}

		specificationsWarp.deleteSpecificationField(field);
	}

	// Public query methods

	/**
	 * Create a criteria query based on the parsed filter parameters
	 *
	 * @return a criteria query
	 */
	public Mono<Criteria> toCriteria() {
		Map<String, List<Criteria>> criteriaMap = new HashMap<>();

		var sortedParts = specificationsWarp.getAllPartsSorted();

		MultiValueMap<String, Object> mlmap = new LinkedMultiValueMap<>(sortedParts.size());

		LOGGER.debug("Processing parts: {}", sortedParts);

		return Flux.fromIterable(sortedParts).doOnNext(e -> LOGGER.debug("Processing part {}", e)).flatMap(part ->
		// Process each part
		part.processPart(criteriaMap, mlmap, spelResolver)).then( // Process the final criteria
				Mono.fromCallable(() -> parseFinalCriteria(criteriaMap)) // Final criteria processing
		).doOnSuccess(c -> {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("Final criteria {}", c.getCriteriaObject().toBsonDocument());
			}
		}).doOnTerminate(() -> {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Final criteria map: {}", criteriaMap);
			}
		}).doOnError(e -> {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error("Error processing criteria", e);
			}
		});
	}

	/**
	 * Generate a Criteria query based on the parsed filter parameters with sort
	 * operations
	 *
	 * @return a Criteria query
	 */
	public Mono<Query> toCriteriaQuery() {
		return toCriteriaQuery(true);
	}

	/**
	 * Generate a Criteria query based on the parsed filter parameters
	 *
	 * @param withSorts if true, the query will include sorting operations
	 * @return a Criteria query
	 */
	public Mono<Query> toCriteriaQuery(boolean withSorts) {
		return toCriteria().doOnNext(e -> {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("Final criteria {}", e.getCriteriaObject().toBsonDocument());
			}
		}).map(finalCriteria -> {

			Query query = new Query().addCriteria(finalCriteria);

			if (withSorts) {
				query = processSort(query);
			}

			return query;

		}).doOnNext(e -> {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Final generated: query '{}' sort '{}' skip '{}' limit '{}'",
						e.getQueryObject().toBsonDocument(), e.getSortObject().toBsonDocument(), e.getSkip(),
						e.getLimit());
			}
		});

	}

	/**
	 * Execute a find query based on the parsed filter parameters
	 *
	 * @return a list of entities
	 */
	public Flux<E> executeFindQuery() {
		return toCriteriaQuery(true).flatMapMany(va -> mongoTemplate.find(va, entityClass));
	}

	public Flux<E> executeFindQuery(int limit) {
		return toCriteriaQuery(true).flatMapMany(va -> {
			va.limit(limit);
			return mongoTemplate.find(va, entityClass);
		});
	}

	/**
	 * Execute a aggregate query based on the parsed filter parameters with
	 * pagination
	 *
	 * @param returnType the return type of the projection
	 * @return a page of entities
	 */
	public <T> Flux<T> executeAggregateAndProject(Class<T> returnType) {
		return toCriteria().flatMapMany(query -> {
			List<AggregationOperation> aggs = new ArrayList<>(3);

			aggs.add(Aggregation.match(query));

			var orders = getOrders();
			if (!orders.isEmpty()) {
				aggs.add(Aggregation.sort(Sort.by(orders)));
			}

			aggs.add(getProjectionOfClass(returnType));

			var pipeline = Aggregation.newAggregation(aggs);
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug(LOG_FINAL_PIPELINE, pipeline);
			}

			return mongoTemplate.aggregate(pipeline, entityClass, returnType);

		});
	}

	public <T> Flux<T> executeAggregateAndProject(Class<T> returnType, int limit) {
		return toCriteria().flatMapMany(query -> {
			List<AggregationOperation> aggs = new ArrayList<>(4);

			aggs.add(Aggregation.match(query));

			var orders = getOrders();
			if (!orders.isEmpty()) {
				aggs.add(Aggregation.sort(Sort.by(orders)));
			}

			aggs.add(getProjectionOfClass(returnType));
			aggs.add(Aggregation.limit(limit));

			var pipeline = Aggregation.newAggregation(aggs);
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug(LOG_FINAL_PIPELINE, pipeline);
			}

			return mongoTemplate.aggregate(pipeline, entityClass, returnType);

		});
	}

	/**
	 * Execute a aggregate query based on the parsed filter parameters with
	 * pagination
	 *
	 * @param pageable   the pagination information
	 * @param returnType the return type of the projection
	 * @return a page of entities
	 */
	public <T> Mono<Page<T>> executeAggregateAndProject(Pageable pageable, Class<T> returnType) {
		return toCriteria().flatMap(query -> {
			List<AggregationOperation> aggs = new ArrayList<>(5);

			aggs.add(Aggregation.match(query));

			var orders = getOrders();
			if (!orders.isEmpty()) {
				aggs.add(Aggregation.sort(Sort.by(orders)));
			}

			aggs.add(getProjectionOfClass(returnType));
			aggs.add(Aggregation.skip(pageable.getOffset()));
			aggs.add(Aggregation.limit(pageable.getPageSize()));

			var pipeline = Aggregation.newAggregation(aggs);
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug(LOG_FINAL_PIPELINE, pipeline);
			}

			var results = mongoTemplate.aggregate(pipeline, entityClass, returnType);

			return results.collectList().zipWith(executeQueryCount(query))
					.map(tuple -> new PageImpl<>(tuple.getT1(), pageable, tuple.getT2()));
		});

	}

	/**
	 * Execute a find query based on the parsed filter parameters with pagination
	 *
	 * @param pageable the pagination information
	 * @return a page of entities
	 */
	public Mono<Page<E>> executeFindQuery(Pageable pageable) {

		return toCriteriaQuery(true).map(query -> {
			query.with(pageable);
			return query;
		}).flatMap(query -> {
			var list = mongoTemplate.find(query, entityClass);
			return list.collectList().zipWith(executeQueryCount(query))
					.map(tuple -> new PageImpl<>(tuple.getT1(), pageable, tuple.getT2()));
		});

	}

	/**
	 * Execute a count query based on the parsed filter parameters
	 *
	 * @return the number of entities
	 */
	public Mono<Long> executeCount() {
		return toCriteriaQuery(false).flatMap(this::executeQueryCount);
	}

	// Private util methods

	private Query processSort(Query baseQuery) {

		var orders = getOrders();
		if (orders.isEmpty()) {
			return baseQuery;
		}

		return baseQuery.with(Sort.by(orders));

	}

	private List<Sort.Order> getOrders() {
		var sortList = defaultSortEnabled ? defaultSorting : sortDefinitionList;
		if (sortList.isEmpty()) {
			return Collections.emptyList();
		}
		LOGGER.trace("Adding all sort operations");

		List<Sort.Order> orders = new ArrayList<>(sortList.size());

		for (var sortOp : sortList) {
			orders.add(parseSort(sortOp));
		}

		return orders;
	}

	private ProjectionOperation getProjectionOfClass(Class<?> returnType) {
		if (mapProjections.containsKey(returnType)) {
			return mapProjections.get(returnType).getProjectionOperation();
		} else {
			return Aggregation.project(returnType);
		}
	}

	private Sort.Order parseSort(Pair<IDefinitionSortable, Sort.Direction> sortDefinition) {
		return new Sort.Order(sortDefinition.getSecond(), sortDefinition.getFirst().getFirstPathMappingName());
	}

	private Criteria parseFinalCriteria(Map<String, List<Criteria>> criteriaMap) {

		if (criteriaMap.isEmpty()) {
			return new Criteria();
		}

		// Size check to reduce and nested operations
		Map<String, Criteria> simplifiedCriterias = criteriaMap.entrySet().stream()
				.collect(Collectors.toMap(Map.Entry::getKey, e -> {
					if (e.getValue().size() == 1) {
						return e.getValue().get(0);
					} else {
						return new Criteria().andOperator(e.getValue());
					}
				}));

		if (simplifiedCriterias.size() == 1) {
			return simplifiedCriterias.values().iterator().next();
		}

		return new Criteria().andOperator(simplifiedCriterias.values());

	}

	private Mono<Long> executeQueryCount(Query query) {
		return mongoTemplate.count(Query.of(query).skip(-1).limit(-1), entityClass);
	}

	private Mono<Long> executeQueryCount(Criteria criteria) {
		return mongoTemplate.count(new Query().addCriteria(criteria).skip(-1).limit(-1), entityClass);
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return initialInput;
	}

}
