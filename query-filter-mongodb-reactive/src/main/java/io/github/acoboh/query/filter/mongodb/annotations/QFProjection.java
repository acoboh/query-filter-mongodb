package io.github.acoboh.query.filter.mongodb.annotations;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Allows the user to create new Projection instances on a specific query filter
 * class.
 * <p>
 * Now, the user use a return class of the type configured inside this
 * annotation, the aggregate will project only the available fields on the
 * class.
 * <p>
 * It will create a new tree of projection, so only the fields on the class and
 * nested objects will be returned.
 */
@Documented
@Retention(RUNTIME)
@Target({ TYPE, ANNOTATION_TYPE })
@Repeatable(QFProjections.class)
public @interface QFProjection {

	/**
	 * Entity class to be used as return type
	 *
	 * @return class
	 */
	Class<?> value();

}
