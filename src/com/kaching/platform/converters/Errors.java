/**
 * Copyright 2010 KaChing Group Inc. Licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package com.kaching.platform.converters;

import static com.google.common.collect.Lists.newArrayList;
import static java.lang.String.format;
import static java.util.Collections.EMPTY_LIST;

import java.lang.reflect.Type;
import java.util.List;

/**
 * Object helping with capturing and propagating errors.
 */
class Errors {

  // null or [E :: L] where L is any list
  private List<String> messages;

  private Errors addMessage(String message, Object... values) {
    if (messages == null) {
      messages = newArrayList();
    }
    messages.add(format(message, values));
    return this;
  }

  void throwIfHasErrors() {
    if (messages != null) {
      throw new RuntimeException(toString());
    }
  }

  @SuppressWarnings("unchecked")
  Errors incorrectBoundForConverter(
      Class<?> targetClass,
      Class<? extends Converter> converterClass,
      Type producedType) {
    return addMessage(
        "the converter %2$s, mentioned on %1$s using @%4$s, does not produce " +
        "instances of %1$s. It produces %3$s.",
        targetClass,
        converterClass,
        producedType,
        ConvertedBy.class.getSimpleName());
  }

  Errors moreThanOnceConstructorWithInstantiate(Class<?> klass) {
    return addMessage(
        "%s has more than one constructor annotated with @%s",
        klass,
        Instantiate.class.getSimpleName());
  }

  Errors unableToInstantiate(Class<?> klass, InstantiationException e) {
    return addMessage(
        "unable to instantiate %s due to %s",
        klass,
        e.getCause());
  }

  Errors unableToInstantiate(Class<?> klass, IllegalAccessException e) {
    return addMessage(
        "unable to instantiate %s because of lack of access to the definition of the constructor",
        klass);
  }

  @Override
  public int hashCode() {
    return messages == null ? EMPTY_LIST.hashCode() : messages.hashCode();
  }

  @Override
  public boolean equals(Object that) {
    if (this == that) {
      return true;
    }
    if (!(that instanceof Errors)) {
      return false;
    }
    return this.messages == null ?
        ((Errors) that).messages == null :
          this.messages.equals(((Errors) that).messages);
  }

  @Override
  public String toString() {
    if (messages == null) {
      return "no errors";
    }
    StringBuilder buf = new StringBuilder();
    int num = 1;
    String separator = "";
    for (String message : messages) {
      buf.append(separator);
      buf.append(num);
      buf.append(") ");
      buf.append(message);
      num++;
      separator = "\n\n";
    }
    return buf.toString();
  }

}
