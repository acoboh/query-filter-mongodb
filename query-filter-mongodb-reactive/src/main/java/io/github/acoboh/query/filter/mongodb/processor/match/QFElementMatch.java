package io.github.acoboh.query.filter.mongodb.processor.match;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.util.MultiValueMap;

import io.github.acoboh.query.filter.mongodb.exceptions.QFDateParsingException;
import io.github.acoboh.query.filter.mongodb.exceptions.QFEnumException;
import io.github.acoboh.query.filter.mongodb.exceptions.QFFieldOperationException;
import io.github.acoboh.query.filter.mongodb.operations.QFOperationEnum;
import io.github.acoboh.query.filter.mongodb.processor.QFPath;
import io.github.acoboh.query.filter.mongodb.processor.QFPath.QFElementDefType;
import io.github.acoboh.query.filter.mongodb.processor.QFSpecificationPart;
import io.github.acoboh.query.filter.mongodb.processor.definitions.QFDefinitionElement;
import io.github.acoboh.query.filter.mongodb.spel.SpelResolverInterface;
import io.github.acoboh.query.filter.mongodb.utils.DateUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Match definition of QFElement types
 */
public class QFElementMatch implements QFSpecificationPart {

	private static final Logger LOGGER = LoggerFactory.getLogger(QFElementMatch.class);

	private final QFDefinitionElement definition;

	private final List<String> originalValues;

	private final List<List<QFPath>> paths;

	private final QFOperationEnum operation;

	private Mono<List<String>> processedValues;
	private Flux<List<Object>> parsedValues;

	private final DateTimeFormatter formatter;

	private boolean initialized = false;

	/**
	 * Default constructor
	 * 
	 * @param values     values to match
	 * @param operation  operation to perform
	 * @param definition definition of the element
	 */
	public QFElementMatch(List<String> values, QFOperationEnum operation, QFDefinitionElement definition) {

		this.definition = definition;
		this.originalValues = values;
		this.operation = operation;

		formatter = definition.getDateTimeFormatter();

		paths = definition.getPaths();

		if (!definition.isSpelExpression()) {
			initialize(null, null);
		}

	}

	/**
	 * Initialize method to resolve matching elements of resolve SpEL expressions
	 * 
	 * @param spelResolver bean to resolve SpEL expressions
	 * @param context      context to resolve SpEL expressions
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void initialize(SpelResolverInterface spelResolver, MultiValueMap<String, Object> context) {
		if (definition.isSpelExpression() && !originalValues.isEmpty()) {
			if (spelResolver == null) {
				throw new IllegalStateException(
						"Unable to evaluate SpEL expressions on missing bean SpelContextResolver");
			}

			String firstValue = originalValues.get(0);
			var result = spelResolver.evaluate(firstValue, context);
			processedValues = parseResults(result);
			initialized = false;
		}

		if (initialized) {
			return;
		}

		if (processedValues == null) {
			processedValues = Mono.just(new ArrayList<>(originalValues));
		}

		parsedValues = Flux.fromIterable(paths).flatMap(path -> {

			QFPath lastPath = path.get(path.size() - 1);
			Class<?> finalClass = lastPath.getFieldClass();

			checkOperation(finalClass);

			return processedValues.map(list -> {
				boolean isEnum = lastPath.getType() == QFElementDefType.ENUM;
				List<Object> parsedPathValue = new ArrayList<>();
				for (var val : list) {
					if (formatter != null && operation != QFOperationEnum.ISNULL) {
						parsedPathValue.add(parseTimestamp(val, finalClass));
					} else if (finalClass.equals(Double.class) || finalClass.equals(double.class)) {
						parsedPathValue.add(Double.valueOf(val));
					} else if (finalClass.equals(Integer.class) || finalClass.equals(int.class)) {
						parsedPathValue.add(Integer.valueOf(val));
					} else if (finalClass.equals(Boolean.class) || finalClass.equals(boolean.class)
							|| operation == QFOperationEnum.ISNULL) {
						parsedPathValue.add(Boolean.valueOf(val));
					} else if (isEnum) { // Parse enum
						Class<? extends Enum> enumClass = (Class<? extends Enum>) finalClass;
						try {
							parsedPathValue.add(Enum.valueOf(enumClass, val));
						} catch (IllegalArgumentException e) {
							Enum[] constants = enumClass.getEnumConstants();
							String[] allowed = new String[constants.length];

							for (int i = 0; i < constants.length; i++) { // Get allowed values
								allowed[i] = constants[i].name();
							}

							throw new QFEnumException(definition.getFilterName(), val, enumClass, allowed);
						}

					} else if (finalClass.equals(UUID.class)) {
						parsedPathValue.add(UUID.fromString(val));
					} else { // Use as string
						parsedPathValue.add(val);
					}
				}

				return parsedPathValue;

			});

		});

		initialized = true;

	}

	private Object parseTimestamp(String value, Class<?> finalClass) {

		try {
			Object parsedValue = DateUtils.parseDate(formatter, value, finalClass, definition.getDateAnnotation());
			if (parsedValue == null) {
				throw new IllegalStateException("Unsupported date class " + finalClass);
			}

			return parsedValue;

		} catch (DateTimeParseException e) {
			throw new QFDateParsingException(definition.getFilterName(), value,
					definition.getDateAnnotation().timeFormat(), e);
		}

	}

	private void checkOperation(Class<?> clazz) throws QFFieldOperationException {

		switch (operation) {
		case GREATER_THAN, GREATER_EQUAL_THAN, LESS_THAN, LESS_EQUAL_THAN:
			if (!Comparable.class.isAssignableFrom(clazz) && !clazz.isPrimitive()) {
				throw new QFFieldOperationException(operation, definition.getFilterName());
			}
			break;

		case ENDS_WITH, STARTS_WITH, LIKE:
			if (!String.class.isAssignableFrom(clazz)) {
				throw new QFFieldOperationException(operation, definition.getFilterName());
			}
			break;
		default:
			LOGGER.trace("Operation {} allowed on field {} by default", operation, definition.getFilterName());
		}
	}

	private Mono<List<String>> parseResults(Mono<Object> monoResult) {

		return monoResult.map(spelResolved -> {
			if (spelResolved == null) {
				return Collections.emptyList();
			}

			Class<?> originalClass = spelResolved.getClass();

			if (spelResolved instanceof String[] casted) {
				return Arrays.asList(casted);
			} else if (spelResolved instanceof Collection<?>) {
				return ((Collection<?>) spelResolved).stream().map(Object::toString).toList();
			} else if (String.class.equals(originalClass)) {
				return Collections.singletonList((String) spelResolved);
			}

			// Primitive array copy
			List<String> retList = null;
			if (boolean[].class.equals(originalClass)) {
				boolean[] fromArray = (boolean[]) spelResolved;
				retList = new ArrayList<>(fromArray.length);
				for (boolean b : fromArray) {
					retList.add(String.valueOf(b));
				}

			} else if (byte[].class.equals(originalClass)) {
				byte[] fromArray = (byte[]) spelResolved;
				retList = new ArrayList<>(fromArray.length);
				for (byte b : fromArray) {
					retList.add(String.valueOf(b));
				}

			} else if (short[].class.equals(originalClass)) {
				short[] fromArray = (short[]) spelResolved;
				retList = new ArrayList<>(fromArray.length);
				for (short value : fromArray) {
					retList.add(String.valueOf(value));
				}

			} else if (int[].class.equals(originalClass)) {
				int[] fromArray = (int[]) spelResolved;
				retList = new ArrayList<>(fromArray.length);
				for (int j : fromArray) {
					retList.add(String.valueOf(j));
				}

			} else if (long[].class.equals(originalClass)) {
				long[] fromArray = (long[]) spelResolved;
				retList = new ArrayList<>(fromArray.length);
				for (long l : fromArray) {
					retList.add(String.valueOf(l));
				}

			} else if (float[].class.equals(originalClass)) {
				float[] fromArray = (float[]) spelResolved;
				retList = new ArrayList<>(fromArray.length);
				for (float v : fromArray) {
					retList.add(String.valueOf(v));
				}

			} else if (double[].class.equals(originalClass)) {
				double[] fromArray = (double[]) spelResolved;
				retList = new ArrayList<>(fromArray.length);
				for (double v : fromArray) {
					retList.add(String.valueOf(v));
				}

			} else if (char[].class.equals(originalClass)) {
				char[] fromArray = (char[]) spelResolved;
				retList = new ArrayList<>(fromArray.length);
				for (char c : fromArray) {
					retList.add(String.valueOf(c));
				}
			}

			if (retList != null) {
				return retList;
			}

			return Collections.singletonList(spelResolved.toString());
		});

	}

	/**
	 * Get parsed values
	 *
	 * @return parsed values
	 */
	public Flux<List<Object>> getParsedValues() {
		return parsedValues;
	}

	/**
	 * Get field definition
	 *
	 * @return field definition
	 */
	public QFDefinitionElement getDefinition() {
		return definition;
	}

	/**
	 * Get if the matching element must be evaluated
	 *
	 * @return array if it must be evaluated
	 */
	public Mono<Boolean> needToEvaluate() {
		if (!initialized) {
			throw new IllegalStateException();
		}

		if (definition.isBlankIgnore()) {
			return processedValues.hasElement(); // Has no elements
		}

		return Mono.just(true);

	}

	@Override
	public Mono<Void> processPart(Map<String, List<Criteria>> criteriaMap, MultiValueMap<String, Object> mlmap,
			SpelResolverInterface spelResolver) {

		initialize(spelResolver, mlmap);

		return needToEvaluate().flatMap(e -> {
			if (!e) {
				return Mono.empty();
			}

			// Create flux from 0 to paths.size()
			return Flux.range(0, paths.size()) // Full flux for all paths
					.flatMap(i -> operation.generateCriteria(this, i, mlmap)) // Create criteria for each path
					.collectList() // Process full list
					.map(criteriaList -> {
						var expr = definition.getPredicateOperation().getPredicate(criteriaList);
						criteriaMap.computeIfAbsent(definition.getFilterName(), t -> new ArrayList<>()).add(expr);
						return Mono.empty();
					}).then();
		});

	}
}
