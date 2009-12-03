package com.kaching.platform.converters;

public interface ToString<T> {

  /**
   * Converts a value to a string representation.
   * @param value the value to convert.
   * @return the string representation of the value.
   */
  String toString(T value);
  
}
