package fr.univmobile.web.commons;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The URI paths the {@link AbstractController} class will handle. e.g.
 * "about", "about/", "regions/", etc.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Paths {

	String[] value();
}
