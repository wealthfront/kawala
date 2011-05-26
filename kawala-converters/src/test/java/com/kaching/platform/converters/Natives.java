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
package com.kaching.platform.converters;

class Natives {
  final int i;
  final double d;
  final short s;
  final char c;
  final long l;
  final boolean b;
  final float f;
  final byte y;

  public Natives(
      int i, double d, short s, char c, long l, boolean b, float f, byte y) {
    this.i = i;
    this.d = d;
    this.s = s;
    this.c = c;
    this.l = l;
    this.b = b;
    this.f = f;
    this.y = y;
  }
}
