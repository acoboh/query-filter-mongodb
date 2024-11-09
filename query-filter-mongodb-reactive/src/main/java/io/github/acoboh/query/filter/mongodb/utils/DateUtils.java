package io.github.acoboh.query.filter.mongodb.utils;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

import io.github.acoboh.query.filter.mongodb.annotations.QFDate;
import io.github.acoboh.query.filter.mongodb.annotations.QFDate.QFDateDefault;

/**
 * Date utilities
 */
public class DateUtils {

	private DateUtils() {
		// Private constructor
	}

	/**
	 * Get the formatter for any {@linkplain QFDate} annotation
	 *
	 * @param dateAnnotation Annotation
	 * @return new formatter
	 */
	public static DateTimeFormatter getFormatter(QFDate dateAnnotation) {

		DateTimeFormatter formatter;

		ZoneId zone;
		if (dateAnnotation == null) {
			formatter = DateTimeFormatter.ofPattern(QFDate.DEFAULT_DATE_FORMAT);
			zone = ZoneId.of(QFDate.DEFAULT_ZONEOFFSET);
		} else {

			if (dateAnnotation.parseDefaulting() != null && dateAnnotation.parseDefaulting().length > 0) {
				DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder()
						.appendPattern(dateAnnotation.timeFormat());

				for (QFDateDefault def : dateAnnotation.parseDefaulting()) {
					builder.parseDefaulting(def.chronoField(), def.value());
				}

				formatter = builder.toFormatter();

			} else {
				formatter = DateTimeFormatter.ofPattern(dateAnnotation.timeFormat());
			}

			zone = ZoneId.of(dateAnnotation.zoneOffset());

		}

		formatter.withZone(zone);
		return formatter;
	}

	/**
	 * Parse any date for a custom {@linkplain QFDate} annotation
	 *
	 * @param formatter      Date formatter
	 * @param value          original string value
	 * @param finalClass     final class to be parsed
	 * @param dateAnnotation annotation
	 * @return the date parsed
	 */
	public static Object parseDate(DateTimeFormatter formatter, String value, Class<?> finalClass,
			QFDate dateAnnotation) {
		if (Timestamp.class.isAssignableFrom(finalClass)) {
			LocalDateTime dt = LocalDateTime.parse(value, formatter);
			return Timestamp.valueOf(dt);
		} else if (LocalDateTime.class.isAssignableFrom(finalClass)) {
			return LocalDateTime.parse(value, formatter);
		} else if (LocalDate.class.isAssignableFrom(finalClass)) {
			return LocalDate.parse(value, formatter);
		} else if (ZonedDateTime.class.isAssignableFrom(finalClass)) {
			return ZonedDateTime.parse(value, formatter);
		} else if (Date.class.isAssignableFrom(finalClass) || Date.class.isAssignableFrom(finalClass)) {
			LocalDate ld = LocalDate.parse(value, formatter);
			return Date.valueOf(ld);
		} else if (java.util.Date.class.isAssignableFrom(finalClass)) {
			LocalDateTime dt = LocalDateTime.parse(value, formatter);
			return java.util.Date.from(dt.toInstant(ZoneOffset.of(dateAnnotation.zoneOffset())));
		}

		return null;

	}

	/**
	 * Check if a class is a date class
	 * 
	 * @param clazz class to check
	 * @return true if is a date class
	 */
	public static boolean classIsDate(Class<?> clazz) {
		return clazz.isAssignableFrom(Timestamp.class) || clazz.isAssignableFrom(LocalDateTime.class)
				|| clazz.isAssignableFrom(LocalDate.class) || clazz.isAssignableFrom(ZonedDateTime.class)
				|| clazz.isAssignableFrom(Date.class) || clazz.isAssignableFrom(Date.class)
				|| clazz.isAssignableFrom(java.util.Date.class);
	}
}
