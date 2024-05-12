package io.github.acoboh.query.filter.mongodb.operations;

import java.util.HashMap;
import java.util.Map;

import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.util.MultiValueMap;

import com.mongodb.client.model.Filters;

import io.github.acoboh.query.filter.mongodb.exceptions.QFOperationNotFoundException;
import io.github.acoboh.query.filter.mongodb.operations.resolutors.QFTextOperationResolutor;
import io.github.acoboh.query.filter.mongodb.processor.match.QFTextMatch;

public enum QFOperationTextEnum implements QFTextOperationResolutor {

	/**
	 * Equal operation
	 */
	EQUAL("eq") {

		@Override
		public Criteria generateCriteriaFilter(QFTextMatch match, MultiValueMap<String, Object> mlContext) {

			var val = match.getValue();
			mlContext.add(match.getDefinition().getFilterName(), val);
			var textOptions = match.getDefinition().getTextSearchOptions();

			var bsonFilter = Filters.text(val, textOptions);
			var bsonVal = bsonFilter.toBsonDocument().get("$text");

			return Criteria.where("$text").is(bsonVal);
		}

	};

	private static final Map<String, QFOperationTextEnum> CONSTANTS = new HashMap<>();

	static {
		for (QFOperationTextEnum c : values()) {
			CONSTANTS.put(c.value, c);
		}
	}

	private final String value;

	QFOperationTextEnum(String value) {
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
	 * @throws QFOperationNotFoundException if the operation is not found
	 */
	public static QFOperationTextEnum fromValue(String value) throws QFOperationNotFoundException {
		QFOperationTextEnum constant = CONSTANTS.get(value);
		if (constant == null) {
			throw new QFOperationNotFoundException(value);
		}
		return constant;
	}

	/** {@inheritDoc} */
	@Override
	public String getOperation() {
		return value;
	}
}
