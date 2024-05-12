package io.github.acoboh.query.filter.mongodb.operations.resolutors;

import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.util.MultiValueMap;

import io.github.acoboh.query.filter.mongodb.processor.match.QFElementMatch;

/**
 * Interface to resolve all operations
 *
 * @author Adrián Cobo
 * 
 */
public interface QFOperationResolutor {

	/**
	 * Create a custom criteria for any query filter element of QFElementMatch type
	 * 
	 * @param match     element
	 * @param index     index of the element
	 * @param mlContext context of SpEL
	 * @return criteria created
	 */
	Criteria generateCriteria(QFElementMatch match, int index, MultiValueMap<String, Object> mlContext);

	/**
	 * Get the operation string value
	 *
	 * @return string operation
	 */
	String getOperation();

}
