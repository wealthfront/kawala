/**
 * Copyright 2009 Wealthfront Inc. Licensed under the Apache License,
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

import com.kaching.platform.testing.BadCodeSnippetsRunner;
import com.kaching.platform.testing.BadCodeSnippetsRunner.Check;
import com.kaching.platform.testing.BadCodeSnippetsRunner.CodeSnippets;
import com.kaching.platform.testing.BadCodeSnippetsRunner.Snippet;

@RunWith(BadCodeSnippetsRunner.class)
@CodeSnippets({
    @Check(paths = "src/com/kaching/platform", snippets = {

        // no uses of java.net.URL
        @Snippet("\\bjava\\.net\\.URL\\b"),

        // no System.out.print
        @Snippet("\\bSystem\\.out\\.print"),

        // never use System to get current time
        @Snippet("currentTimeMillis"),

        // never call default super constructor
        @Snippet("super\\(\\)"),

        // never compare strings by reference
        @Snippet("==\\s*\""),
        @Snippet("\"\\s*== "),

        // no sleeping
        @Snippet("Thread.sleep\\("),
        @Snippet("import\\s*static\\s*java.lang.Thread.sleep")
    })
})
public class BadCodeSnippetTest {
}
