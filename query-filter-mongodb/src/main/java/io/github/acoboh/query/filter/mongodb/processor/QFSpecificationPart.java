package io.github.acoboh.query.filter.mongodb.processor;

import java.util.List;
import java.util.Map;

import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.util.MultiValueMap;

import io.github.acoboh.query.filter.mongodb.processor.definitions.QFAbstractDefinition;
import io.github.acoboh.query.filter.mongodb.spel.SpelResolverContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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
	 * @param request      HTTP request
	 * @param response     HTTP response
	 */
	void processPart(Map<String, List<Criteria>> criteriaMap, MultiValueMap<String, Object> mlmap,
			SpelResolverContext spelResolver, HttpServletRequest request, HttpServletResponse response);

	/**
	 * Get definition of the filter field
	 * 
	 * @return definition
	 */
	QFAbstractDefinition getDefinition();

}
