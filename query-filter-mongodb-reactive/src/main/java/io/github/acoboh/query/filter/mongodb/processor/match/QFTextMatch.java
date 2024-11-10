package io.github.acoboh.query.filter.mongodb.processor.match;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.util.MultiValueMap;

import io.github.acoboh.query.filter.mongodb.operations.QFOperationTextEnum;
import io.github.acoboh.query.filter.mongodb.processor.QFSpecificationPart;
import io.github.acoboh.query.filter.mongodb.processor.definitions.QFDefinitionText;
import io.github.acoboh.query.filter.mongodb.spel.SpelResolverInterface;
import reactor.core.publisher.Mono;

/**
 * This class is used to represent a text match operation in the query filter
 */
public class QFTextMatch implements QFSpecificationPart {

	private final QFDefinitionText definition;
	private final QFOperationTextEnum operation;
	private final String value;

	/**
	 * Default constructor
	 * 
	 * @param value      The value to match
	 * @param operation  The operation to perform
	 * @param definition The definition of the field to match
	 */
	public QFTextMatch(String value, QFOperationTextEnum operation, QFDefinitionText definition) {
		this.definition = definition;
		this.operation = operation;
		this.value = value;
	}

	@Override
	public QFDefinitionText getDefinition() {
		return definition;
	}

	/**
	 * Get the value to match
	 * 
	 * @return The value to match
	 */
	public String getValue() {
		return value;
	}

	@Override
	public Mono<Void> processPart(Map<String, List<Criteria>> criteriaMap, MultiValueMap<String, Object> mlmap,
			SpelResolverInterface spelResolver) {
		var bson = operation.generateCriteriaFilter(this, mlmap);
		criteriaMap.computeIfAbsent(definition.getFilterName(), t -> new ArrayList<>()).add(bson);
		return Mono.empty();
	}

}
