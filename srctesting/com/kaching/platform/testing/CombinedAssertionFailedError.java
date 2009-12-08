package com.kaching.platform.testing;

import static com.google.common.collect.Lists.newArrayList;

import java.util.Iterator;
import java.util.List;

/**
 * Thrown when an assertion failed for multiple reasons.
 */
public class CombinedAssertionFailedError extends AssertionError {

  private static final long serialVersionUID = 5967202290583940192L;

  private final String message;
  private final List<String> errors = newArrayList();

  public CombinedAssertionFailedError(String message) {
    this.message = message;
  }

  public void addError(String error) {
    errors.add(error);
  }

  @Override
  public String getMessage() {
    return toString();
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    if (message != null) {
      builder.append(message);
    }
    int i = 1;
    Iterator<String> iterator = errors.iterator();
    while (iterator.hasNext()) {
      String error = iterator.next();
      builder.append('\n');
      builder.append(i++);
      builder.append(") ");
      builder.append(error);
    }
    return builder.toString();
  }

  public boolean hasErrors() {
    return !errors.isEmpty();
  }

}