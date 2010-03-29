package com.kaching.platform.hibernate.types;

import static org.apache.commons.logging.LogFactory.getLog;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.hibernate.HibernateException;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.usertype.CompositeUserType;
import org.hibernate.usertype.UserType;

/**
 * Common implementation of {@link UserType} and {@link CompositeUserType} for
 * immutable values. This class provides default implementation for boiler plate
 * methods.
 */
public abstract class AbstractImmutableType extends AbstractType {

  private final static Log log;
  private final static boolean IS_TRACE_ENABLED;
  static {
    log = getLog(AbstractImmutableType.class);
    IS_TRACE_ENABLED = log.isTraceEnabled();
  }

  /**
   * Since the entity is immutable, the deep code can safely return
   * the value given in argument.
   * @param value the value to deeply copy
   * @return <tt>value</tt>
   */
  public final Object deepCopy(Object value) {
    return value;
  }

  /**
   * Returns <tt>false</tt> indicating that the entity class is immutable.
   * @return <tt>false</tt>
   */
  public final boolean isMutable() {
    return false;
  }

  /** Returns the value, since this object is immutable.
   */
  public final Serializable disassemble(Object value) throws HibernateException {
    return (Serializable) value;
  }

  /** Returns the value, since this object is immutable.
   */
  public Serializable disassemble(Object value, SessionImplementor session)
      throws HibernateException {
    return (Serializable) value;
  }

  /** Returns the cached value.
   */
  public Object assemble(
      Serializable cached, SessionImplementor session, Object owner)
      throws HibernateException {
    return cached;
  }

  /** Returns the cached value.
   */
  public final Object assemble(Serializable cached, Object owner)
        throws HibernateException {
    if (cached != null) {
      log.trace("assemble " + cached + " (" + cached.getClass() + "), owner is " + owner);
    }
    return cached;
  }

  public final Object replace(Object original, Object target, Object owner)
        throws HibernateException {
    return original;
  }

  public Object replace(Object original, Object target,
        SessionImplementor session, Object owner) throws HibernateException {
    return original;
  }

  public void nullSafeSet(PreparedStatement st, Object value, int index)
        throws HibernateException, SQLException {
    if (value == null) {
      if (IS_TRACE_ENABLED) {
        log.trace("binding 'null' to parameter: " + index);
      }
    } else {
      if (IS_TRACE_ENABLED) {
        log.trace("binding '" + value.toString()+ "' to parameter: " + index);
      }
    }
  }

  public void nullSafeSet(PreparedStatement st, Object value, int index,
        SessionImplementor session) throws HibernateException, SQLException {
    nullSafeSet(st, value, index);
  }

  public void setPropertyValue(Object component, int property, Object value)
        throws HibernateException {
    throw new UnsupportedOperationException("immutable type");
  }

}