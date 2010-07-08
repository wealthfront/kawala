package com.kaching.platform.hibernate;

/**
 * An entity.
 */
public interface HibernateEntity {

  /**
   * Returns the entity's identifier.
   */
  public Id<? extends HibernateEntity> getId();

}
