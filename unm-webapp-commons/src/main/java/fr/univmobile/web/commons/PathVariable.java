package fr.univmobile.web.commons;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The part of the path to map to a variable (actually a Java getter).
 * For instance, if <code>"pois/${id}"</code> is declared in the
 * {@link Paths} annotation, use <code>"${id}"</code>
 * for the <code>PathVariable</code> annotation.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface PathVariable {

	String value();
}
