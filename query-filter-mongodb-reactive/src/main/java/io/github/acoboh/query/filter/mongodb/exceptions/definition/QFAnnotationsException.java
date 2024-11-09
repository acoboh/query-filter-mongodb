package io.github.acoboh.query.filter.mongodb.exceptions.definition;

import java.lang.reflect.Field;

/**
 * Exception thrown when the same element contains multiple type annotations
 *
 * @author Adrián Cobo
 * 
 */
public class QFAnnotationsException extends QueryFilterDefinitionException {

	private static final long serialVersionUID = 1L;

	/**
	 * Default constructor
	 * 
	 * @param field        Field with multiple annotations
	 * @param filterClass  Filter class
	 * @param isQFElement  true if is QFElement
	 * @param isQFSortable true if is QFSortable
	 * @param isQFText     true if is QFText
	 * 
	 */
	public QFAnnotationsException(Field field, Class<?> filterClass, boolean isQFElement, boolean isQFSortable,
			boolean isQFText) {
		super("Can not define different element annotations on the same field {} on class {}. QFElement? {},  QFSortable? {}, QFText? {}",
				field, filterClass, isQFElement, isQFSortable, isQFText);
	}

}
