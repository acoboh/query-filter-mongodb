package io.github.acoboh.query.filter.mongodb.processor;

import java.util.List;
import java.util.Map;

import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.util.MultiValueMap;

import io.github.acoboh.query.filter.mongodb.processor.definitions.QFAbstractDefinition;
import io.github.acoboh.query.filter.mongodb.spel.SpelResolverInterface;
import reactor.core.publisher.Mono;

/**
 * Query filter specification part
 */
public interface QFSpecificationPart {

	/**
	 * Process the part
	 * 
	 * @param criteriaMap  criteria map
	 * @param mlmap        multi-value map
	 * @param spelResolver SpEL resolver
	 */
	public Mono<Void> processPart(Map<String, List<Criteria>> criteriaMap, MultiValueMap<String, Object> mlmap,
			SpelResolverInterface spelResolver);

	/**
	 * Get definition of the filter field
	 * 
	 * @return definition
	 */
	public QFAbstractDefinition getDefinition();

}
