package io.github.acoboh.query.filter.mongodb.utils;

import java.lang.reflect.Field;

import org.springframework.data.annotation.Id;

/**
 * Mongo utilities
 */
public class MongoUtils {

	private static final String ID_MAPPING_NAME = "_id";

	private MongoUtils() {
		// Private constructor
	}

	/**
	 * Get the mongo field name of a field
	 *
	 * @param field Field to get the name
	 * @return Mongo field name
	 */
	public static String getMongoFieldName(Field field) {

		org.springframework.data.mongodb.core.mapping.Field fieldAnnotation = field
				.getAnnotation(org.springframework.data.mongodb.core.mapping.Field.class);

		String fieldName;

		if (fieldAnnotation != null && !fieldAnnotation.value().isBlank()) {
			fieldName = fieldAnnotation.value();
		} else {
			fieldName = field.getName();
		}

		if (field.isAnnotationPresent(Id.class) || fieldName.equals("id")) {
			return ID_MAPPING_NAME;
		} else {
			return fieldName;
		}
	}

}
