package com.kaching.platform.hibernate.types;

import static java.sql.Types.VARCHAR;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.hibernate.HibernateException;
import org.hibernate.usertype.UserType;

import com.kaching.platform.converters.Converter;

/**
 * An abstract hibernate type providing marshalling an unmarshalling to a
 * string representation.
 */
public abstract class AbstractStringImmutableType<T>
    extends AbstractImmutableType implements UserType {

  private static final int[] SQL_TYPES = { VARCHAR };

  public final T nullSafeGet(ResultSet rs, String[] names, Object owner)
      throws HibernateException, SQLException {
    String representation = rs.getString(names[0]);
    // deferred call to wasNull
    // http://java.sun.com/j2se/1.5.0/docs/api/java/sql/ResultSet.html#wasNull()
    if (rs.wasNull() || representation == null) {
      return null;
    } else {
      return converter().fromString(representation);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public final void nullSafeSet(PreparedStatement st, Object value, int index)
      throws HibernateException, SQLException {
    super.nullSafeSet(st, value, index);
    if (value == null) {
      st.setNull(index, VARCHAR);
    } else {
      st.setString(index, converter().toString((T) value));
    }
  }

  /**
   * Gets the converter to use to transform values into strings and vice-versa.
   * @return the converter
   */
  protected abstract Converter<T> converter();

  public final int[] sqlTypes() {
    return SQL_TYPES;
  }

}
