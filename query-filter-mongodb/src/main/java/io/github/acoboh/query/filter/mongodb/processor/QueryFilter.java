package io.github.acoboh.query.filter.mongodb.processor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.data.util.Pair;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import io.github.acoboh.query.filter.mongodb.annotations.QFDefinitionClass;
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
import io.github.acoboh.query.filter.mongodb.spel.SpelResolverContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * The QueryFilter class is responsible for parsing and processing query filter parameters to generate MongoDB queries. It
 * provides methods to construct criteria queries and execute find queries based on the parsed filter parameters.
 *
 * @param <E> the entity class type
 */
public class QueryFilter<E> {

	private static final Logger LOGGER = LoggerFactory.getLogger(QueryFilter.class);

	private static final String REGEX_SORT = "^[a-zA-Z0-9]+=([+-]?[a-zA-Z0-9]+)(,[+-]?[a-zA-Z0-9]+)*+$";
	private static final Pattern REGEX_PATTERN = Pattern.compile("([+-])([a-zA-Z0-9]+)");

	private final String initialInput;

	private final QFSpecificationsWrap specificationsWarp;

	private final List<Pair<IDefinitionSortable, Direction>> defaultSorting;

	private final Map<String, QFAbstractDefinition> definitionMap;

	private final QFDefinitionClass queryFilterClassAnnotation;

	private boolean defaultSortEnabled = true;

	private final Class<E> entityClass;
	private final Class<?> predicateClass;
	private final SpelResolverContext spelResolver;
	private final List<Pair<IDefinitionSortable, Direction>> sortDefinitionList = new ArrayList<>();
	private boolean isConstructor = true;

	private final MongoTemplate mongoTemplate;

	private final Map<Class<?>, ProjectionDefinition> mapProjections;

	private HttpServletRequest request;
	private HttpServletResponse response;

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
		this.queryFilterClassAnnotation = processor.getDefinitionClassAnnotation();
		this.mapProjections = processor.getMapProjections();

		this.specificationsWarp = new QFSpecificationsWrap(processor.getDefaultMatches());

		this.defaultSorting = processor.getDefaultSorting();
		this.entityClass = processor.getEntityClass();
		this.predicateClass = processor.getFilterClass();
		this.spelResolver = processor.getApplicationContext().getBean(SpelResolverContext.class);

		this.mongoTemplate = processor.getApplicationContext().getBean(MongoTemplate.class);

		try {
			ServletRequestAttributes sra = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
			if (sra != null) {
				this.request = sra.getRequest();
				this.response = sra.getResponse();
			}
		} catch (Exception e) {
			LOGGER.warn("Error getting request and response from RequestContextHolder");
		}

		this.initialInput = input != null ? input : "";

		if (input != null && !input.isEmpty()) {
			String[] parts = input.split("&");

			for (String part : parts) {
				if (part.matches(type.getFullRegex())) {
					parseValuePart(part, type);
				} else if (part.matches(REGEX_SORT)) {
					parseSortPart(part);
				} else {
					throw new QFParseException(part, input);
				}

			}
		}

		isConstructor = false;

	}

	private void parseValuePart(String part, QFParamType type)
			throws QFParseException, QFFieldNotFoundException, QFOperationNotFoundException,
			QFDiscriminatorNotFoundException, QFBlockException, QFJsonParseException, QFNotValuable {

		Matcher matcher = type.getPattern().matcher(part);
		if (!(matcher.find() && matcher.groupCount() == 3)) {
			LOGGER.error("Error parsing part {}. Matcher not found matches", part);
			throw new QFParseException(part, type.name());
		}

		String field = matcher.group(1);
		String op = matcher.group(2);
		String value = matcher.group(3);

		QFAbstractDefinition def = definitionMap.get(field);
		if (def == null) {
			throw new QFFieldNotFoundException(field);
		}

		if (def.isConstructorBlocked() && isConstructor) {
			throw new QFBlockException(field);
		}

		QFSpecificationPart qfSpecificationPart;
		if (def instanceof QFDefinitionElement qdef) {
			qfSpecificationPart = new QFElementMatch(Arrays.asList(value.split(",")), QFOperationEnum.fromValue(op),
					qdef);
		} else if (def instanceof QFDefinitionText qdef) {
			qfSpecificationPart = new QFTextMatch(value, QFOperationTextEnum.fromValue(op), qdef);
		} else {
			throw new QFNotValuable(field);
		}

		specificationsWarp.addSpecification(qfSpecificationPart);

	}

	private void parseSortPart(String part)
			throws QFParseException, QFNotSortableException, QFMultipleSortException, QFFieldNotFoundException {

		if (!part.startsWith(queryFilterClassAnnotation.sortProperty() + "=")) {
			throw new QFParseException(part, "sort part");
		}

		String partPostEqual = part.substring(part.indexOf('='));

		String[] parts = partPostEqual.split(",");

		for (String orderPart : parts) {

			Matcher matcher = REGEX_PATTERN.matcher(orderPart);
			if (!matcher.find() || matcher.groupCount() != 2) {
				LOGGER.error("Error parsing sort part {}, Matcher not found matches", orderPart);
				throw new QFParseException(orderPart, "sort part");
			}

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

			Direction dir;
			if (order.equals("+")) {
				dir = Direction.ASC;
			} else {
				dir = Direction.DESC;
			}

			Pair<IDefinitionSortable, Direction> pair = Pair.of((IDefinitionSortable) def, dir);
			this.sortDefinitionList.add(pair);
			this.defaultSortEnabled = false;

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
	 * Create a criteria query based on the parsed filter parameters
	 * 
	 * @return a criteria query
	 */
	public Criteria toCriteria() {
		Map<String, List<Criteria>> criteriaMap = new HashMap<>();

		var sortedParts = specificationsWarp.getAllPartsSorted();

		MultiValueMap<String, Object> mlmap = new LinkedMultiValueMap<>(sortedParts.size());

		for (var part : sortedParts) {
			part.processPart(criteriaMap, mlmap, spelResolver, request, response);
		}

		Criteria finalCriteria = parseFinalCriteria(criteriaMap);

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("Final criteria {}", finalCriteria.getCriteriaObject().toBsonDocument());
		}
		return finalCriteria;
	}

	/**
	 * Generate a Criteria query based on the parsed filter parameters with sort operations
	 * 
	 * @return a Criteria query
	 */
	public Query toCriteriaQuery() {
		return toCriteriaQuery(true);
	}

	/**
	 * Generate a Criteria query based on the parsed filter parameters
	 * 
	 * @param withSorts if true, the query will include sorting operations
	 * @return a Criteria query
	 */
	public Query toCriteriaQuery(boolean withSorts) {

		Criteria finalCriteria = toCriteria();

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("Final criteria {}", finalCriteria.getCriteriaObject().toBsonDocument());
		}

		Query query = new Query().addCriteria(finalCriteria);

		if (withSorts) {
			query = processSort(query);
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Final generated: query '{}' sort '{}' skip '{}' limit '{}'",
					query.getQueryObject().toBsonDocument(), query.getSortObject().toBsonDocument(), query.getSkip(),
					query.getLimit());
		}

		return query;

	}

	private Query processSort(Query baseQuery) {

		var orders = getOrders();
		if (orders.isEmpty()) {
			return baseQuery;
		}

		return baseQuery.with(Sort.by(orders));

	}

	private List<Order> getOrders() {
		var sortList = defaultSortEnabled ? defaultSorting : sortDefinitionList;
		if (sortList.isEmpty()) {
			return Collections.emptyList();
		}
		LOGGER.trace("Adding all sort operations");

		List<Order> orders = new ArrayList<>(sortList.size());

		for (var sortOp : sortList) {
			orders.add(parseSort(sortOp));
		}

		return orders;
	}

	/**
	 * Execute a find query based on the parsed filter parameters
	 * 
	 * @return a list of entities
	 */
	public List<E> executeFindQuery() {
		var query = toCriteriaQuery(true);

		return mongoTemplate.find(query, entityClass);
	}

	/**
	 * Execute a aggregate query based on the parsed filter parameters with pagination
	 * 
	 * @param returnType the return type of the projection
	 * @return a page of entities
	 */
	public <T> List<T> executeAggregateAndProject(Class<T> returnType) {
		var query = toCriteria();

		List<AggregationOperation> aggs = new ArrayList<>(3);

		aggs.add(Aggregation.match(query));

		var orders = getOrders();
		boolean aggregated = false;
		if (!orders.isEmpty()) {
			boolean firstOrder = true;
			if (mapProjections.containsKey(returnType)) {
				var projectionSet = mapProjections.get(returnType).getFieldKeys();
				firstOrder = !orders.stream().allMatch(e -> projectionSet.contains(e.getProperty()));
			}

			if (firstOrder) {
				aggs.add(Aggregation.sort(Sort.by(orders)));
				aggs.add(getProjectionOfClass(returnType));
			} else {
				aggs.add(getProjectionOfClass(returnType));
				aggs.add(Aggregation.sort(Sort.by(orders)));
			}

			aggregated = true;

		}

		if (!aggregated) {
			aggs.add(getProjectionOfClass(returnType));
		}

		var pipeline = Aggregation.newAggregation(aggs);
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Final generated aggregate pipeline: '{}'", pipeline);
		}

		return mongoTemplate.aggregate(pipeline, entityClass, returnType).getMappedResults();
	}

	/**
	 * Execute a aggregate query based on the parsed filter parameters with pagination
	 * 
	 * @param pageable   the pagination information
	 * @param returnType the return type of the projection
	 * @return a page of entities
	 */
	public <T> Page<T> executeAggregateAndProject(Pageable pageable, Class<T> returnType) {
		var query = toCriteria();

		List<AggregationOperation> aggs = new ArrayList<>(5);

		aggs.add(Aggregation.match(query));

		var orders = getOrders();
		boolean aggregated = false;
		if (!orders.isEmpty()) {
			boolean firstOrder = true;
			if (mapProjections.containsKey(returnType)) {
				var projectionSet = mapProjections.get(returnType).getFieldKeys();
				firstOrder = !orders.stream().allMatch(e -> projectionSet.contains(e.getProperty()));
			}

			if (firstOrder) {
				aggs.add(Aggregation.sort(Sort.by(orders)));
				aggs.add(getProjectionOfClass(returnType));
			} else {
				aggs.add(getProjectionOfClass(returnType));
				aggs.add(Aggregation.sort(Sort.by(orders)));
			}

			aggregated = true;

		}

		if (!aggregated) {
			aggs.add(getProjectionOfClass(returnType));
		}

		aggs.add(Aggregation.skip(pageable.getOffset()));
		aggs.add(Aggregation.limit(pageable.getPageSize()));

		var pipeline = Aggregation.newAggregation(aggs);
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Final generated aggregate pipeline: '{}'", pipeline);
		}

		var results = mongoTemplate.aggregate(pipeline, entityClass, returnType).getMappedResults();

		return PageableExecutionUtils.getPage(results, pageable, () -> executeQueryCount(query));
	}

	private ProjectionOperation getProjectionOfClass(Class<?> returnType) {
		if (mapProjections.containsKey(returnType)) {
			return mapProjections.get(returnType).getProjectionOperation();
		} else {
			return Aggregation.project(returnType);
		}
	}

	/**
	 * Execute a find query based on the parsed filter parameters with pagination
	 * 
	 * @param pageable the pagination information
	 * @return a page of entities
	 */
	public Page<E> executeFindQuery(Pageable pageable) {
		var query = toCriteriaQuery(true).with(pageable);

		var list = mongoTemplate.find(query, entityClass);

		return PageableExecutionUtils.getPage(list, pageable, () -> executeQueryCount(query));
	}

	/**
	 * Execute a count query based on the parsed filter parameters
	 * 
	 * @return the number of entities
	 */
	public long executeCount() {
		var query = toCriteriaQuery(false);
		return executeQueryCount(query);
	}

	private long executeQueryCount(Query query) {
		return mongoTemplate.count(Query.of(query).skip(-1).limit(-1), entityClass);
	}

	private long executeQueryCount(Criteria criteria) {
		return mongoTemplate.count(new Query().addCriteria(criteria).skip(-1).limit(-1), entityClass);
	}

	private Sort.Order parseSort(Pair<IDefinitionSortable, Direction> sortDefinition) {
		return new Order(sortDefinition.getSecond(), sortDefinition.getFirst().getFirstPathMappingName());
	}

	private Criteria parseFinalCriteria(Map<String, List<Criteria>> criteriaMap) {

		if (criteriaMap.isEmpty()) {
			return new Criteria();
		}

		// Size check to reduce and nested operations
		Map<String, Criteria> simplifiedCriterias = criteriaMap.entrySet().stream()
				.collect(Collectors.toMap(Entry::getKey, e -> {
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

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return initialInput;
	}

}
