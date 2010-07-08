package com.kaching.platform.hibernate;

import com.google.common.base.Function;

public class Entities {

  public static <T extends HibernateEntity> Function<T, Id<T>> getId(Class<T> entity) {

	return new Function<T, Id<T>>() {
      @Override
      @SuppressWarnings("unchecked")
      public Id<T> apply(T from) {
        return (Id<T>) from.getId();
      }
    };
  }

}
