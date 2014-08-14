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

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.unmodifiableList;

import java.io.File;
import java.util.List;

public class ClassTree {
  
  private final File root;

  public ClassTree(File root) {
    this.root = root;
  }

  public List<File> getClassFiles() {
    return getClassFiles(root);
  }

  private List<File> getClassFiles(File directory) {
    List<File> classFiles = newArrayList();
    File[] allFiles = directory.listFiles();
    if (allFiles != null) {
      for (File file : allFiles) {
        if (file.isDirectory()) {
          classFiles.addAll(getClassFiles(file));
        } else if (file.getName().endsWith(".class")) {
          classFiles.add(file);
        }
      }
    }
    return unmodifiableList(classFiles);
  }

}
