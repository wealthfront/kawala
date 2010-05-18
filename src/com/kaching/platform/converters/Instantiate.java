package com.kaching.platform.converters;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotates a constructor to mark it as the target constructor to use when
 * instantiating the object.
 */
@Target(CONSTRUCTOR)
@Retention(RUNTIME)
public @interface Instantiate {
}
