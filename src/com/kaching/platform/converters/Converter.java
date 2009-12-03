package com.kaching.platform.converters;

import java.net.URL;


/**
 * <p>A converter is responsible for converting to and from a textual
 * representation of a value object. For instance, a {@link URL} can be
 * represented as the string {@code "http://www.kaching.com"} and a converter
 * would create a {@link URL} object from this representation and produce this
 * string from a {@link URL} instance.</p>
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
