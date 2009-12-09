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
package com.kaching.platform.guice;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.isEmpty;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.inject.Provider;

public class Providers {

  public static <T> Provider<T> provider(final T value) {
    checkNotNull(value);
    return new Provider<T>() {
      public T get() {
        return value;
      }
    };
  }

  public static <T> Provider<T> random(Iterable<T> values) {
    checkArgument(!isEmpty(values));
    final List<T> list = ImmutableList.copyOf(values);
    return new Provider<T>() {
      public T get() {
        return list.get(((int) (Math.random() * list.size())));
      }
    };
  }

}
