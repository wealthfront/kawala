/**
 * Copyright 2010 Wealthfront Inc. Licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
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

  static final String VALUE_DEFAULT = "fba751a996cb6315d4fd41afacc2978b";

  /**
   * Used to provide a default value.
   */
  String value() default VALUE_DEFAULT;

  /**
   * Provides the reference to a constant ({@code static final} field). The
   * constant can be in the same class as the annotated parameter in which case
   * the name is sufficient, or it can be in another class and it must be fully
   * qualified such as {@code com.kaching.myexample.MyConstructor#MY_CONSTANT}.
   */
  String constant() default "";

}
