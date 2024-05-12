package io.github.acoboh.query.filter.mongodb.annotations;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Allows the user to create multiple QFProjection annotations
 */
@Documented
@Retention(RUNTIME)
@Target({ TYPE, ANNOTATION_TYPE })
public @interface QFProjections {

	/**
	 * All {@linkplain QFElement} annotations
	 * 
	 * @return {@linkplain QFElement} annotations
	 */
	QFProjection[] value();
}
