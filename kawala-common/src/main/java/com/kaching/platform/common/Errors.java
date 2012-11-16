/**
 * Copyright 2010 Wealthfront Inc. Licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package com.kaching.platform.common;

import static com.google.common.collect.Lists.newArrayList;
import static java.lang.String.format;
import static java.util.Collections.EMPTY_LIST;
import static java.util.Collections.emptyList;

import java.util.List;

import com.google.common.collect.ImmutableList;

/**
 * Object helping with capturing and propagating errors.
 */
public class Errors {

  // null or [E :: L] where L is any list
  private List<String> messages;

  public Errors addMessage(String message, Object... values) {
    if (messages == null) {
      messages = newArrayList();
    }
    String formattedMessage = format(message, values);
    if (!messages.contains(formattedMessage)) {
      messages.add(formattedMessage);
    }
    return this;
  }

  public Errors addErrors(Errors errors) {
    if (errors.messages == null) {
      return this;
    }
    for (String message : errors.messages) {
      addMessage(message);
    }
    return this;
  }

  public List<String> getMessages() {
    if (messages == null) {
      return emptyList();
    }
    return ImmutableList.copyOf(messages);
  }

  public void throwIfHasErrors() {
    if (messages != null) {
      throw new RuntimeException(toString());
    }
  }

  public int size() {
    return messages == null ? 0 : messages.size();
  }

  public boolean hasErrors() {
    return 0 < size();
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
