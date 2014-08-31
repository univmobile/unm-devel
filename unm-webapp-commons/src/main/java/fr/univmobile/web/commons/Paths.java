package fr.univmobile.web.commons;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The URI paths the {@link AbstractController} class will handle. e.g.
 * <code>"about"</code>, <code>"about/"</code>, 
 * <code>"regions/"</code>, <code>"pois/${id}"</code>, etc.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Paths {

	String[] value();
}
