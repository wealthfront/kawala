/**
 * Copyright 2009 KaChing Group Inc. Licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package com.kaching.platform.converters;

import java.net.URI;

/**
 * <p>A converter is responsible for converting to and from a textual
 * representation of a value object. For instance, a {@link URI} can be
 * represented as the string {@code "http://www.kaching.com"} and a converter
 * would create a {@link URI} object from this representation and produce this
 * string from a {@link URI} instance.</p>
 *
 * <p>Converters much ensure that the textual representation produced by
 * {@link #toString(Object)} is compatible with {@link #fromString(String)} such
 * that</p>
 * <pre>
 * T value1 = ...
 * T value2 = converter.fromString(converter.toString(value1));
 * assertEquals(value1, value2);</pre>
 * <p>always succeeds.</p>
 *
 * @param <T> the type this converter converts.
 */
public interface Converter<T> extends ToString<T>, FromString<T> {
}
