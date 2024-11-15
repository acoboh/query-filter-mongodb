package io.github.acoboh.query.filter.mongodb.annotations;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.time.temporal.ChronoField;

/**
 * Annotation used to set any query filter element as a date
 * <p>
 * You can specify a format
 * </p>
 *
 * @author Adrián Cobo
 * 
 */
@Documented
@Retention(RUNTIME)
@Target(FIELD)
public @interface QFDate {

	/**
	 * Default date time format
	 */
	String DEFAULT_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

	/**
	 * Default zone offset
	 */
	String DEFAULT_ZONEOFFSET = "UTC";

	/**
	 * Used to set a format time. If it is empty, a timestamp default format will be
	 * used
	 *
	 * @return Time format used
	 */
	String timeFormat() default DEFAULT_DATE_FORMAT;

	/**
	 * Timezone to be used on parsing
	 * 
	 * @return timezone
	 */
	String zoneOffset() default DEFAULT_ZONEOFFSET;

	/**
	 * Default parts of date for default formating
	 * 
	 * @return array of date parts
	 */
	QFDateDefault[] parseDefaulting() default {};

	/**
	 * Allows the user to create default parsing parts.
	 * <p>
	 * 
	 * An example of usage is when the user only filter by date but the actual model
	 * field is a timestamp. With this functionality you can set the hours, minutes
	 * and seconds of the full timestamp
	 * 
	 * @author Adrián Cobo
	 *
	 */
	@interface QFDateDefault {

		/**
		 * Chrono field
		 * 
		 * @return Chrono field
		 */
		ChronoField chronoField();

		/**
		 * Default value
		 * 
		 * @return default value
		 */
		long value();

	}

}
