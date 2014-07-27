package fr.univmobile.web.commons;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Tells that a HTTP Parameter is required to be present in the request
 * for a given {@link HttpInputs}
 * sub-interface to be valid.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface HttpRequired {

}
