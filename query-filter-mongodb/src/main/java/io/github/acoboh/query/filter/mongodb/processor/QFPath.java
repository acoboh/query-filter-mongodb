package io.github.acoboh.query.filter.mongodb.processor;

import java.lang.reflect.Field;

import io.github.acoboh.query.filter.mongodb.utils.MongoUtils;

/**
 * This class is used to represent a path in the query filter
 */
public class QFPath {

	/**
	 * The type of the element definition
	 */
	public enum QFElementDefType {

		/**
		 * Type is property
		 */
		PROPERTY,

		/**
		 * Type is list
		 */
		LIST,

		/**
		 * Type is a set
		 */
		SET,

		/**
		 * Type is an enumeration
		 */
		ENUM
	}

	private final Field field;
	private final String path;

	private final QFElementDefType type;

	private final Class<?> fieldClass;

	private final String mappingName;

	private boolean isFinal;

	/**
	 * Default constructor
	 * 
	 * @param field      The field of the path
	 * @param path       The path
	 * @param type       The type of the element definition
	 * @param fieldClass The class of the field
	 * @param isFinal    If the field is final
	 */
	public QFPath(Field field, String path, QFElementDefType type, Class<?> fieldClass, boolean isFinal) {
		this.field = field;
		this.path = path;
		this.type = type;
		this.fieldClass = fieldClass;
		this.isFinal = isFinal;

		this.mappingName = MongoUtils.getMongoFieldName(field);

	}

	/**
	 * Get the field
	 * 
	 * @return The field
	 */
	public Field getField() {
		return field;
	}

	/**
	 * Get the path
	 * 
	 * @return The path
	 */
	public String getPath() {
		return path;
	}

	/**
	 * Get the type of the element definition
	 * 
	 * @return The type of the element definition
	 */
	public QFElementDefType getType() {
		return type;
	}

	/**
	 * Get the class of the field
	 * 
	 * @return The class of the field
	 */
	public Class<?> getFieldClass() {
		return fieldClass;
	}

	/**
	 * Get if the field is final
	 * 
	 * @return If the field is final
	 */
	public boolean isFinal() {
		return isFinal;
	}

	/**
	 * Set if the field is final
	 * 
	 * @param isFinal If the field is final
	 */
	public void setFinal(boolean isFinal) {
		this.isFinal = isFinal;
	}

	/**
	 * Get the mapping name
	 * 
	 * @return The mapping name
	 */
	public String getMappingName() {
		return mappingName;
	}
}
