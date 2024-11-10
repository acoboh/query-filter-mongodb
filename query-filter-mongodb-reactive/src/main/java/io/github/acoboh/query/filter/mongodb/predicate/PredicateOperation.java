package io.github.acoboh.query.filter.mongodb.predicate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.data.mongodb.core.query.Criteria;

/**
 * Predicate operations for custom predicates enumeration
 *
 * @author Adrián Cobo
 * 
 */
public enum PredicateOperation {

	/**
	 * And operator
	 */
	AND("AND") {

		@Override
		public Criteria getPredicate(List<Criteria> predicates) {

			if (predicates.isEmpty()) {
				return new Criteria();
			} else if (predicates.size() == 1) {
				return predicates.get(0);
			}

			return new Criteria().andOperator(predicates);
		}

	},

	/**
	 * Or operator
	 */
	OR("OR") {

		@Override
		public Criteria getPredicate(List<Criteria> predicates) {
			if (predicates.isEmpty()) {
				return new Criteria();
			} else if (predicates.size() == 1) {
				return predicates.get(0);
			}

			return new Criteria().orOperator(predicates);
		}

	};

	private static final Map<String, PredicateOperation> map;

	static {
		map = Stream.of(PredicateOperation.values()).collect(Collectors.toMap(PredicateOperation::getValue, e -> e));
	}

	private final String value;

	PredicateOperation(String value) {
		this.value = value;
	}

	/**
	 * Get the enumerated from the string value
	 *
	 * @param value String value of operation to be found
	 * @return predicate operation found. Null if the operation is not found
	 */
	public static PredicateOperation getOperator(String value) {
		return map.get(value);
	}

	/**
	 * Resolve the predicate with criteria builder
	 * 
	 * @param predicates Predicates to be used on operation
	 *
	 * @return the predicate
	 */
	public abstract Criteria getPredicate(List<Criteria> predicates);

	/**
	 * Get the value of the predicate
	 * 
	 * @return value of the predicate
	 */
	public String getValue() {
		return value;
	}
}
