/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.commons.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

/** Annotates a method as traced. This means that the method */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Traced {

  /**
   * The name of the span generated for the method. Defaults to the "classSimpleName#methodName".
   */
  String name() default "";

  /**
   * Use this class to statically set the tags on the span generated for a {@code @Traced} method.
   * Note that the tags are applied <b>after</b> the method is invoked no matter where in the method
   * they are set.
   */
  final class Tags {

    /** Adds a new tag. If the tag already exists, it is NOT updated. */
    public static void add(String tagName, String value) {
      Map<String, String> tags = TagsStack.TAGS.get().peek();
      if (tags != null) {
        tags.putIfAbsent(tagName, value);
      }
    }

    /** Adds new tags. The values of existing tags are not updated. */
    public static void addAll(Map<String, String> keyValues) {
      Map<String, String> map = TagsStack.TAGS.get().peek();
      if (map != null) {
        for (Map.Entry<String, String> e : keyValues.entrySet()) {
          map.putIfAbsent(e.getKey(), e.getValue());
        }
      }
    }
  }

  /**
   * This class is not meant for use in methods annotated with {@code @Traced}, rather it is used by
   * the interceptor wrapping those methods setting up tags storage for each {@code @Traced} method.
   *
   * <p>This class supports {@link Tags} so that it can be correctly and easily used from the
   * annotated methods.
   */
  final class TagsStack {
    private static final ThreadLocal<Deque<Map<String, String>>> TAGS =
        ThreadLocal.withInitial(ArrayDeque::new);

    private TagsStack() {
      throw new AssertionError("I shall not be instantiated.");
    }

    public static Map<String, String> pop() {
      Deque<Map<String, String>> tagsStack = TAGS.get();
      if (tagsStack.isEmpty()) {
        return Collections.emptyMap();
      }

      Map<String, String> tags = tagsStack.pop();

      return Collections.unmodifiableMap(tags);
    }

    public static void push() {
      TAGS.get().push(new HashMap<>(4));
    }
  }
}
