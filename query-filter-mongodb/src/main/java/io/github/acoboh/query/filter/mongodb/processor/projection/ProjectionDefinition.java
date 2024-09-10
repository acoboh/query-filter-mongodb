package io.github.acoboh.query.filter.mongodb.processor.projection;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.Fields;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;

import io.github.acoboh.query.filter.mongodb.exceptions.definition.QFTypeException;
import io.github.acoboh.query.filter.mongodb.utils.ClassUtils;
import io.github.acoboh.query.filter.mongodb.utils.MongoUtils;

/**
 * This class is used to represent a projection definition
 */
public class ProjectionDefinition {

	private final Class<?> returnType;

	private final ProjectionOperation projectionOperation;
	private final Set<String> fieldKeys;

	/**
	 * Default constructor
	 * 
	 * @param returnType The return type of the projection
	 * @throws QFTypeException If the return type is not valid
	 */
	public ProjectionDefinition(Class<?> returnType) throws QFTypeException {
		this.returnType = returnType;

		var fieldsList = getAllFieldNames(returnType, "");

		var fields = fieldsList.toArray(new String[fieldsList.size()]);

		var fieldsAggregation = Fields.from();
		for (var f : fields) {
			fieldsAggregation = fieldsAggregation.and(f, f);
		}

		projectionOperation = Aggregation.project(fieldsAggregation);
		fieldKeys = Set.of(fields);
	}

	/**
	 * Get the return type of the projection
	 * 
	 * @return The return type of the projection
	 */
	public Class<?> getReturnType() {
		return returnType;
	}

	/**
	 * Get the projection operation
	 * 
	 * @return The projection operation
	 */
	public ProjectionOperation getProjectionOperation() {
		return projectionOperation;
	}

	/**
	 * Get the field keys
	 * 
	 * @return The field keys
	 */
	public Set<String> getFieldKeys() {
		return fieldKeys;
	}

	private static List<String> getAllFieldNames(Class<?> returnType, String prefix) throws QFTypeException {

		List<String> fields = new ArrayList<>();

		for (Field field : getAllFieldSuperclass(returnType)) {

			String fieldName = MongoUtils.getMongoFieldName(field);

			Class<?> ret = field.getType();
			if (ClassUtils.isPrimitiveOrBasic(ret)) {
				fields.add(prefix + fieldName);
			} else if (ClassUtils.isListArrayOrSet(ret)) {
				Class<?> listClass = ClassUtils.getClassOfList(field);

				if (ClassUtils.isPrimitiveOrBasic(listClass)) {
					fields.add(prefix + fieldName);
				} else {
					fields.addAll(getAllFieldNames(listClass, prefix + fieldName + "."));
				}
			} else { // Is an object
				fields.addAll(getAllFieldNames(ret, prefix + fieldName + "."));
			}

		}

		return fields;
	}

	private static List<Field> getAllFieldSuperclass(Class<?> clazz) {

		List<Field> allFields = new ArrayList<>();
		while (clazz != null) {
			Collections.addAll(allFields, clazz.getDeclaredFields());
			clazz = clazz.getSuperclass();
		}
		return allFields;

	}

}
