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
 * with this annotation may manipulate network sockets. {@link #endpoints()} is
 * the list of allowed IP endpoints (both local and remote) in the
 * "[hostname]:[port]" format (sans-quotes). [hostname] or [port] may be "*",
 * indicating that accessing any hostname, or port, is allowed.
 *
 * This annotation is used to express both:
 * <ul>
 * <li>the permission to receive an incoming connection from a remote endpoint,
 * and</li>
 * <li>the permission to initiate an outgoing connection to a remote endpoint.</li>
 *
 * Note that due to performance restrictions, the {@link LessIOSecurityManager}
 * only performs textual matches, and does not resolve hostnames or IP
 * addresses.
 */
@Retention(RUNTIME)
@Target(TYPE)
public @interface AllowNetworkAccess {
  String[] endpoints();
}
