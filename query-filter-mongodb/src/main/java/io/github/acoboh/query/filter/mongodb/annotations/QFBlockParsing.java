package io.github.acoboh.query.filter.mongodb.annotations;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Used to force hide from parsing phase.
 * <p>
 * You can specify internal parsing like roles or administrator fields you do
 * not want to set visible for external usage. After the creation, you can add
 * the field with methods of class
 * {@link io.github.acoboh.query.filter.mongodb.processor.QueryFilter}
 * <p>
 *
 * @author Adrián Cobo
 * 
 */
@Retention(RUNTIME)
@Target(FIELD)
@Documented
public @interface QFBlockParsing {

}
