package io.github.acoboh.query.filter.mongodb.annotations;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Allows the user to create a field of type text to use text search on MongoDB
 * 
 * @see <a href=
 *      "https://www.mongodb.com/docs/manual/core/link-text-indexes/">https://www.mongodb.com/docs/manual/core/link-text-indexes/</a>
 */
@Documented
@Retention(RUNTIME)
@Target({ FIELD, ANNOTATION_TYPE })
public @interface QFText {

	/**
	 * Name to use in the input filter. If it is not specified, it will be used the name of the variable associated
	 *
	 * @return name
	 */
	String name() default "";

	/**
	 * Allows the user to use case sensitive text search
	 * 
	 * @return true if the query must be case sensitive
	 */
	boolean caseSensitive() default false;

	/**
	 * Allow the user to use diacritic sensitive text search
	 * 
	 * @return true if the query must be diacritics sensitive
	 */
	boolean diacriticSensitive() default false;

	/**
	 * Allows the user to enable language text search
	 * 
	 * @return selected language (Ex: en/es)
	 */
	String language() default "";

}
