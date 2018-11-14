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
package org.eclipse.che.commons.tracing;

import io.opentracing.Scope;
import io.opentracing.Tracer;
import io.opentracing.Tracer.SpanBuilder;

/**
 * A utility class for creating simple traces.
 *
 * <pre>{@code
 * Traces.using(tracer)
 *     .create("operation", "tag1", "tag1value", "tag2", "tag2value")
 *     .calling(() -> {
 *       ... code that is now traced ...
 *     });
 * }</pre>
 */
public class Traces {

  private Traces() {
    throw new AssertionError("This class cannot be instantiated.");
  }

  public static TraceBuilder using(Tracer tracer) {
    return new TraceBuilder(tracer);
  }

  public interface ThrowingRunnable<E extends Throwable> {

    void run() throws E;
  }

  public interface ThrowingSupplier<T, E extends Throwable> {

    T get() throws E;
  }

  public static final class TraceBuilder {

    private final Tracer tracer;

    private TraceBuilder(Tracer tracer) {
      this.tracer = tracer;
    }

    public CallAcceptor create(String operationName, String... tags) {
      return new CallAcceptor(tracer, operationName, tags);
    }
  }

  public static final class CallAcceptor {

    private final Tracer tracer;
    private final String operationName;
    private final String[] tags;

    public CallAcceptor(Tracer tracer, String operationName, String[] tags) {
      this.tracer = tracer;
      this.operationName = operationName;
      this.tags = tags;
    }

    public <E extends Throwable> void calling(ThrowingRunnable<E> action) throws E {
      if (tracer == null) {
        action.run();
        return;
      }

      SpanBuilder bld = tracer.buildSpan(operationName).asChildOf(tracer.activeSpan());
      addTags(bld, tags);

      try (Scope ignored = bld.startActive(true)) {
        action.run();
      }
    }

    public <T, E extends Throwable> T calling(ThrowingSupplier<T, E> call) throws E {
      if (tracer == null) {
        return call.get();
      }

      SpanBuilder bld = tracer.buildSpan(operationName).asChildOf(tracer.activeSpan());
      addTags(bld, tags);

      try (Scope ignored = bld.startActive(true)) {
        return call.get();
      }
    }

    private static void addTags(SpanBuilder spanBuilder, String[] tags) {
      if (tags.length % 2 != 0) {
        throw new IllegalArgumentException("Unbalanced tags definition");
      }

      for (int i = 0; i < tags.length; i += 2) {
        spanBuilder.withTag(tags[i], tags[i + 1]);
      }
    }
  }
}
