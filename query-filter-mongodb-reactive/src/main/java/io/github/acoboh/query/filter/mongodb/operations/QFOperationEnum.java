package io.github.acoboh.query.filter.mongodb.operations;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.util.MultiValueMap;

import io.github.acoboh.query.filter.mongodb.exceptions.QFOperationNotFoundException;
import io.github.acoboh.query.filter.mongodb.operations.resolutors.QFOperationResolutor;
import io.github.acoboh.query.filter.mongodb.processor.match.QFElementMatch;
import reactor.core.publisher.Mono;

/**
 * Enumerated with all the operations
 *
 * @author Adrián Cobo
 * 
 */
public enum QFOperationEnum implements QFOperationResolutor {

	/**
	 * Equal operation
	 */
	EQUAL("eq") {

		@Override
		public Mono<Criteria> generateCriteria(QFElementMatch match, int index,
				MultiValueMap<String, Object> mlContext) {

			// Get the index element of the flux
			return match.getParsedValues().elementAt(index).map(list -> {
				Object val = list.get(0);
				mlContext.add(match.getDefinition().getFilterName(), val);
				return Criteria.where(match.getDefinition().getPathMappingName(index)).is(val);
			});
		}

	},
	/**
	 * Not equal operation
	 */
	NOT_EQUAL("ne") {
		@Override
		public Mono<Criteria> generateCriteria(QFElementMatch match, int index,
				MultiValueMap<String, Object> mlContext) {
			return match.getParsedValues().elementAt(index).map(list -> {
				Object val = list.get(0);
				mlContext.add(match.getDefinition().getFilterName(), val);
				return Criteria.where(match.getDefinition().getPathMappingName(index)).ne(val);
			});
		}

	},
	/**
	 * Greater than operation
	 */
	GREATER_THAN("gt") {
		@Override
		public Mono<Criteria> generateCriteria(QFElementMatch match, int index,
				MultiValueMap<String, Object> mlContext) {
			return match.getParsedValues().elementAt(index).map(list -> {
				Object val = list.get(0);
				mlContext.add(match.getDefinition().getFilterName(), val);
				return Criteria.where(match.getDefinition().getPathMappingName(index)).gt(val);
			});
		}

	},
	/**
	 * Greater or equal than
	 */
	GREATER_EQUAL_THAN("gte") {
		@Override
		public Mono<Criteria> generateCriteria(QFElementMatch match, int index,
				MultiValueMap<String, Object> mlContext) {
			return match.getParsedValues().elementAt(index).map(list -> {
				Object val = list.get(0);
				mlContext.add(match.getDefinition().getFilterName(), val);
				return Criteria.where(match.getDefinition().getPathMappingName(index)).gte(val);
			});
		}

	},
	/**
	 * Less than operation
	 */
	LESS_THAN("lt") {
		@Override
		public Mono<Criteria> generateCriteria(QFElementMatch match, int index,
				MultiValueMap<String, Object> mlContext) {
			return match.getParsedValues().elementAt(index).map(list -> {
				Object val = list.get(0);
				mlContext.add(match.getDefinition().getFilterName(), val);
				return Criteria.where(match.getDefinition().getPathMappingName(index)).lt(val);
			});
		}

	},
	/**
	 * Less or equal than
	 */
	LESS_EQUAL_THAN("lte") {
		@Override
		public Mono<Criteria> generateCriteria(QFElementMatch match, int index,
				MultiValueMap<String, Object> mlContext) {
			return match.getParsedValues().elementAt(index).map(list -> {
				Object val = list.get(0);
				mlContext.add(match.getDefinition().getFilterName(), val);
				return Criteria.where(match.getDefinition().getPathMappingName(index)).lte(val);
			});
		}

	},
	/**
	 * Like operation for strings
	 */
	LIKE("like") {
		@Override
		public Mono<Criteria> generateCriteria(QFElementMatch match, int index,
				MultiValueMap<String, Object> mlContext) {
			return match.getParsedValues().elementAt(index).map(list -> {
				Object val = list.get(0);
				mlContext.add(match.getDefinition().getFilterName(), val);
				return Criteria.where(match.getDefinition().getPathMappingName(index)).regex(val.toString(),
						match.getDefinition().regexOptions(index));
			});
		}

	},
	/**
	 * Starts with operation for strings
	 */
	STARTS_WITH("starts") {

		@Override
		public Mono<Criteria> generateCriteria(QFElementMatch match, int index,
				MultiValueMap<String, Object> mlContext) {
			return match.getParsedValues().elementAt(index).map(list -> {
				Object val = list.get(0);
				mlContext.add(match.getDefinition().getFilterName(), val);
				return Criteria.where(match.getDefinition().getPathMappingName(index)).regex("^" + val.toString(),
						match.getDefinition().regexOptions(index));
			});
		}

	},
	/**
	 * Ends with operation for strings
	 */
	ENDS_WITH("ends") {
		@Override
		public Mono<Criteria> generateCriteria(QFElementMatch match, int index,
				MultiValueMap<String, Object> mlContext) {
			return match.getParsedValues().elementAt(index).map(list -> {
				Object val = list.get(0);
				mlContext.add(match.getDefinition().getFilterName(), val);
				return Criteria.where(match.getDefinition().getPathMappingName(index)).regex(val.toString() + "$",
						match.getDefinition().regexOptions(index));
			});
		}

	},
	/**
	 * IN operation
	 */
	IN("in") {
		@Override
		public Mono<Criteria> generateCriteria(QFElementMatch match, int index,
				MultiValueMap<String, Object> mlContext) {
			return match.getParsedValues().elementAt(index).map(list -> {
				mlContext.addAll(match.getDefinition().getFilterName(), list);
				return Criteria.where(match.getDefinition().getPathMappingName(index)).in(list);
			});
		}

	},
	/**
	 * Not in operation
	 */
	NOT_IN("nin") {
		@Override
		public Mono<Criteria> generateCriteria(QFElementMatch match, int index,
				MultiValueMap<String, Object> mlContext) {
			return match.getParsedValues().elementAt(index).map(list -> {
				mlContext.addAll(match.getDefinition().getFilterName(), list);
				return Criteria.where(match.getDefinition().getPathMappingName(index)).nin(list);
			});
		}

	},
	/**
	 * Is null operation
	 */
	ISNULL("null") {
		@Override
		public Mono<Criteria> generateCriteria(QFElementMatch match, int index,
				MultiValueMap<String, Object> mlContext) {

			return match.getParsedValues().elementAt(index).map(e -> {
				var val = e.get(0);
				var part = Criteria.where(match.getDefinition().getPathMappingName(index));
				if (Boolean.TRUE.equals(val)) {
					return part.is(null);
				} else {
					return part.ne(null);
				}
			});
		}

	};

	private static final Map<String, QFOperationEnum> CONSTANTS = new HashMap<>();

	static {
		for (QFOperationEnum c : values()) {
			CONSTANTS.put(c.value, c);
		}
	}

	private final String value;

	QFOperationEnum(String value) {
		this.value = value;
	}

	/**
	 * Get parameter value on string filter
	 *
	 * @return value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Find operation from the parameter value
	 *
	 * @param value parameter value
	 * @return operation found
	 * @throws io.github.acoboh.query.filter.mongodb.exceptions.QFOperationNotFoundException if
	 *                                                                                       the
	 *                                                                                       operation
	 *                                                                                       is
	 *                                                                                       not
	 *                                                                                       found
	 */
	public static QFOperationEnum fromValue(String value) throws QFOperationNotFoundException {
		QFOperationEnum constant = CONSTANTS.get(value);
		if (constant == null) {
			throw new QFOperationNotFoundException(value);
		}
		return constant;
	}

	/**
	 * Get allowed operations of any class
	 *
	 * @param clazz class to check
	 * @return set of operations
	 */
	public static Set<QFOperationEnum> getOperationsOfClass(Class<?> clazz) {

		Set<QFOperationEnum> ret = new HashSet<>();

		for (QFOperationEnum op : values()) {

			switch (op) {
			case GREATER_THAN, GREATER_EQUAL_THAN, LESS_THAN, LESS_EQUAL_THAN:
				if (Comparable.class.isAssignableFrom(clazz) || clazz.isPrimitive()) {
					ret.add(op);
				}
				break;
			case ENDS_WITH, STARTS_WITH, LIKE:
				if (String.class.isAssignableFrom(clazz)) {
					ret.add(op);
				}
				break;
			default:
				ret.add(op);
			}

		}

		return ret;

	}

	/** {@inheritDoc} */
	@Override
	public String getOperation() {
		return value;
	}

}
