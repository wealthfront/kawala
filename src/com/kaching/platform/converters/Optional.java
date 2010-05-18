package com.kaching.platform.converters;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotates optional parameters in constructors.
 */
@Retention(RUNTIME)
@Target({ PARAMETER })
public @interface Optional {
  
  /**
   * Used to provide a default value.
   */
  String value() default "";
  
}
