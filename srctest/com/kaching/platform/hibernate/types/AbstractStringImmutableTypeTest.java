/**
 * Copyright 2009 KaChing Group Inc. Licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package com.kaching.platform.hibernate.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.sql.ResultSet;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.Sequence;
import org.junit.Before;
import org.junit.Test;

import com.kaching.platform.converters.Converter;

public class AbstractStringImmutableTypeTest {

  private Mockery mockery;
  private AbstractStringImmutableType<Integer> type;

  @Before
  public void before() {
    mockery = new Mockery();
    type = new AbstractStringImmutableType<Integer>() {
      @Override
      protected Converter<Integer> converter() {
        return new Converter<Integer>() {
          @Override
          public String toString(Integer value) {
            return value.toString();
          }
          @Override
          public Integer fromString(String representation) {
            return Integer.parseInt(representation);
          }
        };
      }
      @Override
      public Class<?> returnedClass() {
        return null;
      }
    };
  }

  @Test
  public void getButWasNull() throws Exception {
    final ResultSet rs = mockery.mock(ResultSet.class);
    final Sequence execution = mockery.sequence("execution");
    mockery.checking(new Expectations() {{
      one(rs).getString(with(equal("name0here")));
          inSequence(execution);
          will(returnValue("78"));
      one(rs).wasNull();
          inSequence(execution);
          will(returnValue(true));
    }});

    assertNull(type.nullSafeGet(rs, new String[] { "name0here" }, null));

    mockery.assertIsSatisfied();
  }
  
  @Test
  public void getHadData() throws Exception {
    final ResultSet rs = mockery.mock(ResultSet.class);
    final Sequence execution = mockery.sequence("execution");
    mockery.checking(new Expectations() {{
      one(rs).getString(with(equal("name0here")));
          inSequence(execution);
          will(returnValue("98"));
      one(rs).wasNull();
          inSequence(execution);
          will(returnValue(false));
    }});

    Object value = type.nullSafeGet(rs, new String[] { "name0here" }, null);
    assertNotNull(value);
    assertEquals(98, value);

    mockery.assertIsSatisfied();
  }
  
}
