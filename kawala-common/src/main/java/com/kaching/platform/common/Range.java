package com.kaching.platform.common;

import java.util.Iterator;

/**
 * An immutable range.
 */
public class Range implements Iterable<Integer> {

  public static Range range(int start, int end) {
    return new Range(start, end);
  }

  public final int start;
  public final int end;

  /**
   * A range between {@code start} (inclusive) and {@code end} (exclusive).
   */
  public Range(int start, int end) {
    this.start = start;
    this.end = end;
  }

  public int getStart() {
    return start;
  }

  public int getEnd() {
    return end;
  }

  public boolean isEmpty() {
    return end <= start;
  }

  public boolean contains(int index) {
    return start <= index && end > index;
  }

  public boolean contains(Range that) {
    return start <= that.start && end >= that.end;
  }

  public boolean overlaps(Range that) {
    return this.contains(that)
        || that.contains(this)
        || (start <= that.start && that.start < end)
        || (start < that.end && that.end <= end);
  }

  @Override
  public String toString() {
    return "[" + start + "," + end + ")";
  }

  @Override
  public int hashCode() {
    final int prime = 96643;
    int result = 1;
    result = prime * result + end;
    result = prime * result + start;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    Range other = (Range) obj;
    return end == other.end && start == other.start;
  }

  @Override
  public Iterator<Integer> iterator() {
    return new Iterator<Integer>() {
      private int i = start;

      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }

      @Override
      public Integer next() {
        return ((start < end) ? i++ : i--);
      }

      @Override
      public boolean hasNext() {
        return ((start < end) ? (i < end) : (i > end));
      }
    };
  }

}
