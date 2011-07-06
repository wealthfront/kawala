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
package com.kaching.platform.testing;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Under the regime of {@link LessIOSecurityManager}, only classes annotated
 * with this annotation may perform IO on local files. Such annotated classes
 * will only be allowed to access the paths specified in {@link #paths()}.
 *
 * The following values are recognized for {@link #paths()}:
 * <ul>
 * <li>%TMP_DIR%, allows IO to anything inside the JVM's temporary directory, as
 * per the <i>java.io.tmpdir system property</i> as retrieved via
 * {@link System#getProperty(String)}</li>
 * <li>%FILE_DESCRIPTORS%, allows IO via any file descriptor</li>
 * <li>*, allows IO to any file or directory (path)</li>
 * <li>*[part]*, allows IO to any path which contains [part]</li>
 * <li>*[part], allows IO to any path which ends with [part]</li>
 * <li>[part]*, allows IO to any path which starts with [part]</li>
 * <li>all other values, allow IO to any path which exactly matches the value</li>
 * </ul>
 *
 * Note that due to performance restrictions, the {@link LessIOSecurityManager}
 * only performs textual matches, and does not recognize symbolic links or hard
 * links.
 */
@Retention(RUNTIME)
@Target(TYPE)
public @interface AllowLocalFileAccess {
  String[] paths();
}
