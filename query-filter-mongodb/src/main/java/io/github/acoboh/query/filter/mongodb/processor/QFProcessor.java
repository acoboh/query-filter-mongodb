package io.github.acoboh.query.filter.mongodb.processor;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.util.Pair;

import io.github.acoboh.query.filter.mongodb.annotations.QFDefinitionClass;
import io.github.acoboh.query.filter.mongodb.annotations.QFProjection;
import io.github.acoboh.query.filter.mongodb.annotations.QFProjections;
import io.github.acoboh.query.filter.mongodb.exceptions.QueryFilterException;
import io.github.acoboh.query.filter.mongodb.exceptions.definition.QFClassException;
import io.github.acoboh.query.filter.mongodb.exceptions.definition.QFElementException;
import io.github.acoboh.query.filter.mongodb.exceptions.definition.QFNotSortableDefinitionException;
import io.github.acoboh.query.filter.mongodb.exceptions.definition.QFTypeException;
import io.github.acoboh.query.filter.mongodb.exceptions.definition.QueryFilterDefinitionException;
import io.github.acoboh.query.filter.mongodb.processor.definitions.IDefinitionSortable;
import io.github.acoboh.query.filter.mongodb.processor.definitions.QFAbstractDefinition;
import io.github.acoboh.query.filter.mongodb.processor.definitions.QFDefinitionElement;
import io.github.acoboh.query.filter.mongodb.processor.match.QFElementMatch;
import io.github.acoboh.query.filter.mongodb.processor.projection.ProjectionDefinition;

/**
 * Query filter processor
 *
 * @param <F> filter class
 * @param <E> entity class
 */
public class QFProcessor<F, E> {

	private static final Logger LOGGER = LoggerFactory.getLogger(QFProcessor.class);

	private final Class<F> filterClass;
	private final Class<E> entityClass;

	private final QFDefinitionClass queryFilterClass;

	private final Map<String, QFAbstractDefinition> definitionMap;

	private final List<QFElementMatch> defaultMatches;

	private final List<Pair<IDefinitionSortable, Direction>> defaultSorting;

	private final ApplicationContext appContext;

	private final Map<Class<?>, ProjectionDefinition> mapProjections;

	/**
	 * Create a new instance of {@linkplain QFProcessor}
	 *
	 * @param filterClass filter class
	 * @param entityClass entity class
	 * @param appContext  application context
	 * @throws QueryFilterDefinitionException if any parsing exception occurs
	 */
	public QFProcessor(Class<F> filterClass, Class<E> entityClass, ApplicationContext appContext)
			throws QueryFilterDefinitionException {

		this.filterClass = filterClass;
		this.entityClass = entityClass;
		this.appContext = appContext;

		if (!filterClass.isAnnotationPresent(QFDefinitionClass.class)) {
			throw new QFClassException(QFDefinitionClass.class, filterClass.getName());
		}

		this.queryFilterClass = filterClass.getAnnotation(QFDefinitionClass.class);
		if (this.queryFilterClass.value() != entityClass) {
			throw new QFClassException(queryFilterClass.value(), filterClass, entityClass);
		}

		this.definitionMap = getDefinition(filterClass, queryFilterClass);
		this.defaultMatches = defaultMatches(definitionMap);
		this.defaultSorting = getDefaultSorting(queryFilterClass, definitionMap, filterClass);

		LOGGER.debug("Initialized query filter processor for classes {} to {}", filterClass, entityClass);

		boolean hasProjects = filterClass.isAnnotationPresent(QFProjection.class)
				|| filterClass.isAnnotationPresent(QFProjections.class);
		if (hasProjects) {
			this.mapProjections = getProjections(filterClass);
		} else {
			this.mapProjections = Collections.emptyMap();
		}

	}

	private static Map<Class<?>, ProjectionDefinition> getProjections(Class<?> filterClass) throws QFTypeException {
		Map<Class<?>, ProjectionDefinition> map = new HashMap<>();

		for (var annotation : filterClass.getAnnotationsByType(QFProjection.class)) {
			map.put(annotation.value(), new ProjectionDefinition(annotation.value()));
		}

		return map;

	}

	private static Map<String, QFAbstractDefinition> getDefinition(Class<?> filterClass,
			QFDefinitionClass queryFilterClass) throws QueryFilterDefinitionException {

		Map<String, QFAbstractDefinition> map = new HashMap<>();

		for (Field field : filterClass.getDeclaredFields()) {

			var qfd = QFAbstractDefinition.buildDefinition(field, filterClass, queryFilterClass.value());
			if (qfd == null) {
				continue;
			}

			map.put(qfd.getFilterName(), qfd);

		}

		return map;
	}

	private static List<QFElementMatch> defaultMatches(Map<String, QFAbstractDefinition> definitionMap) {
		List<QFElementMatch> ret = new ArrayList<>();

		for (var abstractDef : definitionMap.values()) {
			if (abstractDef instanceof QFDefinitionElement def) {
				for (var elem : def.getElementAnnotations()) {
					if (elem.defaultValues().length > 0) {
						ret.add(new QFElementMatch(Arrays.asList(elem.defaultValues()), elem.defaultOperation(), def));
					}
				}
			}
		}

		return Collections.unmodifiableList(ret);
	}

	private static List<Pair<IDefinitionSortable, Direction>> getDefaultSorting(QFDefinitionClass queryFilterClass,
			Map<String, QFAbstractDefinition> definitionMap, Class<?> filterClass)
			throws QueryFilterDefinitionException {

		if (queryFilterClass.defaultSort() == null || queryFilterClass.defaultSort().length == 0) {
			return Collections.emptyList();
		}

		List<Pair<IDefinitionSortable, Direction>> ret = new ArrayList<>();

		for (var sort : queryFilterClass.defaultSort()) {

			var definition = definitionMap.get(sort.value());
			if (definition == null) {
				throw new QFElementException(sort.value(), filterClass);
			}

			if (!(definition instanceof IDefinitionSortable)) {
				throw new QFNotSortableDefinitionException(definition.getFilterName(), filterClass);
			}

			ret.add(Pair.of((IDefinitionSortable) definition, sort.direction()));

		}

		return Collections.unmodifiableList(ret);

	}

	/**
	 * Get all definitions of any field
	 *
	 * @return map of definitions
	 */
	public Map<String, QFAbstractDefinition> getDefinitionMap() {
		return definitionMap;
	}

	/**
	 * Get filter class
	 *
	 * @return filter class
	 */
	public Class<F> getFilterClass() {
		return filterClass;
	}

	/**
	 * Get entity class
	 *
	 * @return entity model class
	 */
	public Class<E> getEntityClass() {
		return entityClass;
	}

	/**
	 * Get definition class annotation
	 * 
	 * @return definition class annotation
	 */
	protected QFDefinitionClass getDefinitionClassAnnotation() {
		return queryFilterClass;
	}

	/**
	 * Get default matches of the processor
	 * 
	 * @return default matches
	 */
	protected List<QFElementMatch> getDefaultMatches() {
		return defaultMatches;
	}

	/**
	 * Get default sorting operations
	 * 
	 * @return default sorting operations
	 */
	protected List<Pair<IDefinitionSortable, Direction>> getDefaultSorting() {
		return defaultSorting;
	}

	/**
	 * Get application context
	 * 
	 * @return application context
	 */
	protected ApplicationContext getApplicationContext() {
		return appContext;
	}

	/**
	 * Get all projections
	 * 
	 * @return map of projections
	 */
	protected Map<Class<?>, ProjectionDefinition> getMapProjections() {
		return mapProjections;
	}

	/**
	 * Create a new {@linkplain QueryFilter} instance
	 *
	 * @param input string filter
	 * @param type  standard type
	 * @return new {@linkplain QueryFilter} instance
	 * @throws QueryFilterException if any parsing exception occurs
	 */
	public QueryFilter<E> newQueryFilter(String input, QFParamType type) throws QueryFilterException {
		return new QueryFilter<>(input, type, this);
	}

}
