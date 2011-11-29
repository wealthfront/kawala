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

import static com.kaching.platform.common.Strings.format;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import com.google.common.base.Objects;

class ParsedElements {

  static class ParsedClass implements ParsedElement {

    private final String name;

    ParsedClass(String name) {
      this.name = name.replace("/", ".");
    }

    String getName() {
      return name;
    }

    String getPackageName() {
      return name.substring(0, name.split("\\$")[0].lastIndexOf('.'));
    }

    Class<?> load() {
      try {
        return Class.forName(name);
      } catch (ClassNotFoundException e) {
        return null;
      }
    }

    Annotation loadAnnotation(Class<? extends Annotation> annotation) {
      Class<?> clazz = load();
      if (clazz == null) {
        throw new NullPointerException(format("class %s not found", name));
      }
      return clazz.getAnnotation(annotation);
    }

    String getOwner() {
      return name.split("\\$")[0];
    }

    @Override
    public String toString() {
      return name;
    }

    @Override
    public <T> T visit(ParsedElementVisitor<T> visitor) {
      return visitor.caseClass(this);
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(name);
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      ParsedClass that = (ParsedClass) obj;
      return name.equals(that.name);
    }

  }

  static class ParsedConstructor implements ParsedElement {

    // TODO

    @Override
    public <T> T visit(ParsedElementVisitor<T> visitor) {
      return visitor.caseConstructor(this);
    }

  }

  static class ParsedField implements ParsedElement {

    private final ParsedClass owner;
    private final String name;

    ParsedField(ParsedClass owner, String name) {
      this.owner = owner;
      this.name = name;
    }

    Annotation loadAnnotation(Class<? extends Annotation> annotation) {
      Field field = load();
      if (field == null) {
        throw new NullPointerException(format("field %s not found in %s", name, owner));
      }
      return field.getAnnotation(annotation);
    }

    ParsedClass getOwner() {
      return owner;
    }

    String getName() {
      return name;
    }

    Field load() {
      Class<?> clazz = owner.load();
      Field[] fields = clazz.getDeclaredFields();
      for (Field field : fields) {
        if (field.getName().equals(name)) {
          return field;
        }
      }
      return null;
    }

    @Override
    public String toString() {
      return owner.toString() + "#" + name;
    }

    @Override
    public <T> T visit(ParsedElementVisitor<T> visitor) {
      return visitor.caseField(this);
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(owner, name);
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      ParsedField that = (ParsedField) obj;
      return
          owner.equals(that.owner) &&
          name.equals(that.name);
    }

  }

  static class ParsedMethod implements ParsedElement {

    private final ParsedClass owner;
    private final String name;
    private final String signature;

    ParsedMethod(ParsedClass owner, String name, String signature) {
      this.owner = owner;
      this.name = name;
      this.signature = signature;
    }

    ParsedClass getOwner() {
      return owner;
    }

    String getName() {
      return name;
    }

    String getSignature() {
      return signature;
    }

    Method load() {
      Class<?> clazz = owner.load();
      Method[] methods = clazz.getDeclaredMethods();
      for (Method method : methods) {
        if (method.getName().equals(name)) {
          // TODO check signature, otherwise we don't distinguish between overloaded methods
          return method;
        }
      }
      return null;
    }

    Annotation loadAnnotation(Class<? extends Annotation> annotation) {
      Method method = load();
      if (method == null) {
        throw new NullPointerException(
            format("method %s with signature %s not found in %s", name, signature, owner));
      }
      return method.getAnnotation(annotation);
    }

    @Override
    public String toString() {
      return owner + "#" + name + signature;
    }

    @Override
    public <T> T visit(ParsedElementVisitor<T> visitor) {
      return visitor.caseMethod(this);
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(owner, name, signature);
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      ParsedMethod that = (ParsedMethod) obj;
      return
          owner.equals(that.owner) &&
          name.equals(that.name) &&
          signature.equals(that.signature);
    }

  }

}