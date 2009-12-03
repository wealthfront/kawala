package com.kaching.platform.converters;

public interface FromString<T> {

  /**
   * Converts a textual representation into a value.
   * @param the textual representation to convert.
   * @return the value represented by the textual representation.
   */
  T fromString(String representation);

}
