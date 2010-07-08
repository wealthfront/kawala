package com.kaching.platform.hibernate;

import static com.google.common.collect.Lists.newArrayListWithCapacity;
import static com.google.common.collect.Lists.transform;

import java.util.Collection;
import java.util.List;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.kaching.platform.common.AbstractIdentifier;

/**
 * An {@link Entity} identifier.
 *
 * @param <E> the kind of {@code Entity} referenced by this kind of {@code Id}
 */
public final class Id<E extends HibernateEntity> extends AbstractIdentifier<Long> {

  private static final long serialVersionUID = 404144686720487896L;

  // @VisibleForTesting
  public Id(long id) {
    super(id);
  }

  /**
   * Creates an ID.
   */
  public static <T extends HibernateEntity> Id<T> of(long i) {
    return new Id<T>(i);
  }

  /**
   * Creates a list of IDs from longs.
   * Should not be used in production code (pass along ids, not longs)
   */
  @VisibleForTesting
  public static <T extends HibernateEntity> List<Id<T>> list(long... ids) {
    List<Id<T>> list = newArrayListWithCapacity(ids.length);
    for (long id : ids) {
      list.add(Id.<T>of(id));
    }
    return list;
  }

  /**
   * Creates a list of IDs from a collection of non-null longs.
   */
  public static <T extends HibernateEntity> List<Id<T>> list(Collection<Long> ids) {
    List<Id<T>> list = newArrayListWithCapacity(ids.size());
    for (Long id : ids) {
      list.add(Id.<T>of(id));
    }
    return list;
  }

  /**
   * Creates a list of longs from a list of IDs.
   */
  public static <T extends HibernateEntity> List<Long> toLongs(List<Id<T>> ids) {
    return transform(ids, new Function<Id<?>, Long>() {
      public Long apply(Id<?> id) {
        return id.getId();
      }});
  }

}
