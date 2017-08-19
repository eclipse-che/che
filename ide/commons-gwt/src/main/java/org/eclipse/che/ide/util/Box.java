/*
 * Copyright 2009 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.eclipse.che.ide.util;

/**
 * Boxes a value. Mainly useful as a workaround for the finality requirement of values in a java
 * closure's scope.
 *
 * @param <T>
 * @author danilatos@google.com (Daniel Danilatos)
 */
public class Box<T> {

  /** Settable value. */
  public T boxed;

  /** Convenience factory method. */
  public static <T> Box<T> create() {
    return new Box<T>();
  }

  /** Convenience factory method. */
  public static <T> Box<T> create(T initial) {
    return new Box<T>(initial);
  }

  /** No initial value. */
  public Box() {
    this(null);
  }

  /** @param boxed initial value. */
  public Box(T boxed) {
    this.boxed = boxed;
  }

  /** Sets the boxed value to the given new value. */
  public void set(T newVal) {
    this.boxed = newVal;
  }

  /** @return the boxed value. */
  public T get() {
    return this.boxed;
  }

  /** Sets the boxed value to null. */
  public void clear() {
    this.boxed = null;
  }
}
