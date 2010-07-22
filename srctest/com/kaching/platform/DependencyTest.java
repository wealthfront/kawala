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
package com.kaching.platform;

import org.junit.runner.RunWith;

import com.kaching.platform.testing.DependencyTestRunner;
import com.kaching.platform.testing.DependencyTestRunner.CheckPackage;
import com.kaching.platform.testing.DependencyTestRunner.Dependencies;

@RunWith(DependencyTestRunner.class)
@Dependencies(
    minClasses = 10,
    forPackages = {
        "com.kaching.platform.*"
    },
    ensure = {
      @CheckPackage(name = "com.kaching.platform.*", mayDependOn = {
          "java.*",
          "com.google.common.*"
      }),
      @CheckPackage(name = "com.kaching.platform.converters", mayDependOn = {
          "org.objectweb.asm.*",
          "org.apache.commons.logging", // TODO(adam): remove
          "com.google.inject.*",
          "com.kaching.platform.common.*"
      }),
      @CheckPackage(name = "com.kaching.platform.common.values", mayDependOn = {
          "com.kaching.platform.common"
      }),
      @CheckPackage(name = "com.kaching.platform.guice", mayDependOn = {
          "com.google.inject.*",
          "com.kaching.platform.common.types"
      }),
      @CheckPackage(name = "com.kaching.platform.hibernate.types", mayDependOn = {
          "org.hibernate.*",
          "org.apache.commons.logging", // TODO(adam): remove

          // internal
          "com.kaching.platform.converters"
      }),
      @CheckPackage(name = "com.kaching.platform.testing", mayDependOn = {
          "jdepend.framework",
          "junit.*",
          "org.junit.*",

          // internal
          "com.kaching.platform.common"
      }),
      @CheckPackage(name = "com.kaching.platform.util", mayDependOn = {
          // nothing
      })
    },
    binDirectories = { "bin", "target/bin", "target/bin-testing" }
)
public class DependencyTest {
}
