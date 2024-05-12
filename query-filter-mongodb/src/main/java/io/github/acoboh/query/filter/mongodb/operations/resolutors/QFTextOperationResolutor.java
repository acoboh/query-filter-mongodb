package io.github.acoboh.query.filter.mongodb.operations.resolutors;

import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.util.MultiValueMap;

import io.github.acoboh.query.filter.mongodb.processor.match.QFTextMatch;

/**
 * Interface to resolve all operations of TextMatch type
 *
 * @author Adrián Cobo
 * 
 */
public interface QFTextOperationResolutor {

	/**
	 * Create a custom criteria for any query filter element of QFTextMatch type
	 * 
	 * @param match     element
	 * @param mlContext context of SpEL
	 * @return criteria created
	 */
	Criteria generateCriteriaFilter(QFTextMatch match, MultiValueMap<String, Object> mlContext);

	/**
	 * Get the operation string value
	 *
	 * @return string operation
	 */
	String getOperation();

}
