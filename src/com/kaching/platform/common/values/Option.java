package com.kaching.platform.common.values;

import static java.lang.String.format;

import java.util.Iterator;

import com.google.common.base.Preconditions;
import com.google.common.collect.AbstractIterator;

/**
 * Option<T> := None | Some<T>.
 */
public abstract class Option<T> implements Iterable<T> {

  /** The singleton representing none.
   */
  private final static Option<?> NONE = new Option<Object>() {

    @Override
    public Iterator<Object> iterator() {
      return new AbstractIterator<Object>() {
        @Override
        protected Object computeNext() {
          return endOfData();
        }
      };
    }

    public int hashCode() {
      return 0;
    };

    public boolean equals(Object that) {
      return this == that;
    };

    @Override
    public String toString() {
      return "Option.None";
    }

  };

  /** The object representing some result.
   */
  private final static class Some<U> extends Option<U> {

    private final U u;

    private Some(U u) {
      Preconditions.checkNotNull(u);
      this.u = u;
    }

    @Override
    public Iterator<U> iterator() {
      return new AbstractIterator<U>() {
        private boolean produce = true;
        @Override
        protected U computeNext() {
          if (produce) {
            produce = false;
            return u;
          } else {
            return endOfData();
          }
        }
      };
    }

    @Override
    public int hashCode() {
      return u.hashCode();
    }

    @Override
    public boolean equals(Object that) {
      if (this == that) {
        return true;
      }
      if (!(that instanceof Some<?>)) {
        return false;
      }
      return this.u.equals(((Some<?>) that).u);
    }

    @Override
    public String toString() {
      return format("Option.Some(%s)", u);
    }

  }

  /**
   * Get the none object for the given type.<br />
   * This method is needed to have only one uncheck warning in order to cope
   * with the lack of a bottom type in Java.
   * @param <T> the type parameter
   * @return a type safe none value
   */
  @SuppressWarnings("unchecked")
  public static <T> Option<T> none() {
    return (Option<T>) NONE;
  }

  /**
   * Get the some object wrapping the given object.
   * @param <T> the type parameter
   * @param t the object to wrap
   * @return the option object wrapping <tt>t</tt>
   */
  public static <T> Option<T> some(T t) {
    return new Option.Some<T>(t);
  }

}
