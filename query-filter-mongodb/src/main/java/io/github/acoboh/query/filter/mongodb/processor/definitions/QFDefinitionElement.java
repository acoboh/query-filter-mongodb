package io.github.acoboh.query.filter.mongodb.processor.definitions;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.acoboh.query.filter.mongodb.annotations.QFBlockParsing;
import io.github.acoboh.query.filter.mongodb.annotations.QFDate;
import io.github.acoboh.query.filter.mongodb.annotations.QFElement;
import io.github.acoboh.query.filter.mongodb.annotations.QFElements;
import io.github.acoboh.query.filter.mongodb.exceptions.definition.QFDateClassNotSupported;
import io.github.acoboh.query.filter.mongodb.exceptions.definition.QFDateParseError;
import io.github.acoboh.query.filter.mongodb.exceptions.definition.QFElementMultipleClassesException;
import io.github.acoboh.query.filter.mongodb.exceptions.definition.QueryFilterDefinitionException;
import io.github.acoboh.query.filter.mongodb.predicate.PredicateOperation;
import io.github.acoboh.query.filter.mongodb.processor.QFPath;
import io.github.acoboh.query.filter.mongodb.utils.DateUtils;

/**
 * Definition of QFElement annotation
 */
public class QFDefinitionElement extends QFAbstractDefinition implements IDefinitionSortable {

	private static final Logger LOGGER = LoggerFactory.getLogger(QFDefinitionElement.class);

	private final QFElement[] elementAnnotations;
	private final PredicateOperation defaultOperation;

	private final QFDate dateAnnotation;

	private final DateTimeFormatter dateTimeFormatter;

	private final List<List<QFPath>> paths;
	private final List<Class<?>> finalClasses;
	private final List<String> pathMappingNames;

	// Extra properties
	private final boolean sortable;
	private final boolean spelExpression;
	private final boolean blankIgnore;

	private final int order;

	QFDefinitionElement(Field filterField, Class<?> filterClass, Class<?> entityClass, QFBlockParsing blockedParsing,
			QFElements elementsAnnotation, QFElement[] elementAnnotations, QFDate dateAnnotation)
			throws QueryFilterDefinitionException {
		super(filterField, filterClass, entityClass, blockedParsing);
		this.elementAnnotations = elementAnnotations;
		this.dateAnnotation = dateAnnotation;

		if (elementsAnnotation != null) {
			this.defaultOperation = elementsAnnotation.operation();
		} else {
			this.defaultOperation = PredicateOperation.AND;
		}

		// Only if one element and has name override name
		if (elementAnnotations.length == 1 && !elementAnnotations[0].name().isEmpty()) {
			filterName = elementAnnotations[0].name();
		} else if (elementAnnotations.length > 1) {
			LOGGER.info(
					"Multiple element annotations found on field {}. Will ignore name parameter on all annotations and will be the field name {}",
					filterField, getFilterName());
		}

		LOGGER.debug("Checking sortable on element annotations");
		if (elementAnnotations.length != 1) {
			LOGGER.info("Multiple element annotations on field {}. All properties must be true to be consider true",
					field);
		}

		sortable = Stream.of(elementAnnotations).allMatch(QFElement::sortable);
		spelExpression = Stream.of(elementAnnotations).allMatch(QFElement::isSpPELExpression);
		blankIgnore = Stream.of(elementAnnotations).allMatch(QFElement::blankIgnore);
		order = Stream.of(elementAnnotations).mapToInt(QFElement::order).max().getAsInt();

		var pair = getBuildPaths(elementAnnotations, entityClass);

		this.paths = pair.paths();
		this.finalClasses = pair.finalClasses();
		this.pathMappingNames = pair.mappingNames();

		if (dateAnnotation != null || (finalClasses.stream().allMatch(DateUtils::classIsDate))) {
			dateTimeFormatter = checkDateTimeFormatter();
		} else {
			dateTimeFormatter = null;
		}

	}

	private DateTimeFormatter checkDateTimeFormatter() throws QFDateClassNotSupported, QFDateParseError {
		LocalDateTime now = LocalDateTime.now();

		DateTimeFormatter formatter = DateUtils.getFormatter(dateAnnotation);
		String value = formatter.format(now);

		for (Class<?> finalClass : finalClasses) {
			try {
				Object parsed = DateUtils.parseDate(formatter, value, finalClass, dateAnnotation);
				if (parsed == null) {
					throw new QFDateClassNotSupported(finalClass, filterName);
				}

			} catch (DateTimeParseException e) {
				throw new QFDateParseError(dateAnnotation.timeFormat(), finalClass, e);
			}
		}

		return formatter;

	}

	private static DefinitionInfo getBuildPaths(QFElement[] elementAnnotations, Class<?> entityClass)
			throws QueryFilterDefinitionException {
		LOGGER.debug("Creating paths for all element annotation. Total {}", elementAnnotations.length);

		List<List<QFPath>> paths = new ArrayList<>(elementAnnotations.length);
		List<Class<?>> finalClasses = new ArrayList<>(elementAnnotations.length);
		List<String> mappingNames = new ArrayList<>(elementAnnotations.length);

		for (QFElement elem : elementAnnotations) {

			LOGGER.trace("Creating paths for element annotation {}", elem);
			var processor = new FieldClassProcessor(entityClass, elem.value());
			var path = processor.getPaths();

			paths.add(path);
			finalClasses.add(processor.getFinalClass());
			String mappingName = path.stream().map(QFPath::getMappingName).collect(Collectors.joining("."));
			mappingNames.add(mappingName);
		}

		long distinct = finalClasses.stream().distinct().count();
		if (distinct != 1) {
			throw new QFElementMultipleClassesException();
		}

		return new DefinitionInfo(Collections.unmodifiableList(paths), Collections.unmodifiableList(finalClasses),
				mappingNames);
	}

	public List<List<QFPath>> getPaths() {
		return paths;
	}

	/**
	 * Get all the mapping names of the filtered fields
	 * 
	 * @return mapping names
	 */
	public List<String> getPathMappingNames() {
		return pathMappingNames;
	}

	/**
	 * Get specific path mapping name
	 * 
	 * @param index index of the element
	 * @return path mapping name
	 */
	public String getPathMappingName(int index) {
		return pathMappingNames.get(index);
	}

	@Override
	public String getFirstPathMappingName() {
		return pathMappingNames.get(0);
	}

	/**
	 * Get if the field is sortable
	 *
	 * @return true if sortable
	 */
	public boolean isSortable() {
		return sortable;
	}

	/**
	 * Get the date annotation
	 *
	 * @return date annotation
	 */
	public QFDate getDateAnnotation() {
		return dateAnnotation;
	}

	/**
	 * Get date time formatted created for the field
	 *
	 * @return date time formatted
	 */
	public DateTimeFormatter getDateTimeFormatter() {
		return dateTimeFormatter;
	}

	/**
	 * Get if the field has spel expressions
	 *
	 * @return true if the field has spel
	 */
	public boolean isSpelExpression() {
		return spelExpression;
	}

	/**
	 * Get if with blank values must be ignored
	 *
	 * @return true if with blank values must be ignored
	 */
	public boolean isBlankIgnore() {
		return blankIgnore;
	}

	/**
	 * Get all the element annotations
	 *
	 * @return element annotations
	 */
	public QFElement[] getElementAnnotations() {
		return elementAnnotations;
	}

	/**
	 * Get regex options of an specific element
	 * 
	 * @param index index of the element
	 * @return regex options
	 */
	public String regexOptions(int index) {
		return elementAnnotations[index].regexOptions();
	}

	/**
	 * Get order of evaluation
	 *
	 * @return order of evaluation
	 */
	public int getOrder() {
		return order;
	}

	/**
	 * Get predicate operation with multiple element annotations
	 *
	 * @return predicate operation
	 */
	public PredicateOperation getPredicateOperation() {
		return defaultOperation;
	}

	/**
	 * Get the first final class of the definition element
	 * 
	 * @return first final class located
	 */
	public Class<?> getFirstFinalClass() {
		return finalClasses.get(0);
	}

}
