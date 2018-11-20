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

import com.google.common.annotations.Beta;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Annotates a method as traced.
 *
 * <p>If the method is declared in a Guice-managed class and the Guice environment is equipped with
 * an interceptor for handling this annotation (which should be the case at least in the workspace
 * server), each call of such method will create a new span declared as a child of some current
 * span, if any, with the provided name (or the default name).
 *
 * <p>The method can declare additional tags in its body that will be applied to the span, see
 * {@link Tags}.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Beta
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
    public static void addString(String tagName, Supplier<String> valueSupplier) {
      internalAdd(tagName, valueSupplier);
    }

    /** Adds a new tag. If the tag already exists, it is NOT updated. */
    public static void addBoolean(String tagName, Supplier<Boolean> valueSupplier) {
      internalAdd(tagName, valueSupplier);
    }

    /** Adds a new tag. If the tag already exists, it is NOT updated. */
    public static void addInteger(String tagName, Supplier<Integer> valueSupplier) {
      internalAdd(tagName, valueSupplier);
    }

    private static void internalAdd(String tagName, Supplier<?> value) {
      Map<String, Supplier<?>> tags = TagsStack.TAGS.get().peek();
      if (tags != null) {
        tags.putIfAbsent(tagName, value);
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

    private static final ThreadLocal<Deque<Map<String, Supplier<?>>>> TAGS =
        ThreadLocal.withInitial(ArrayDeque::new);

    private TagsStack() {
      throw new AssertionError("I shall not be instantiated.");
    }

    public static Map<String, Supplier<?>> pop() {
      Deque<Map<String, Supplier<?>>> tagsStack = TAGS.get();
      if (tagsStack.isEmpty()) {
        return Collections.emptyMap();
      }

      Map<String, Supplier<?>> tags = tagsStack.pop();

      return Collections.unmodifiableMap(tags);
    }

    public static void push() {
      // we're assuming max 4 tags per span is gonna be the usual case. Saving 12 entries per method
      // invocation will make a GC difference. If there are more than 4 tags, we're adding runtime
      // overhead of enlarging the hash map but that's not gonna be a usual occurrence (currently,
      // we have at most 3 tags on a span).
      TAGS.get().push(new HashMap<>(4));
    }
  }
}
