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

import static com.google.common.base.Strings.isNullOrEmpty;

import com.google.inject.Inject;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.Tracer.SpanBuilder;
import org.eclipse.che.commons.annotation.Nullable;

/**
 * Null-safe utilities to aid in manually adding tracing to methods. Methods are no-op if tracing is
 * disabled.
 *
 * @author amisevsk
 */
public class TracerUtil {

  private final Tracer tracer;

  @Inject
  public TracerUtil(@Nullable OptionalTracer tracer) {
    this.tracer = OptionalTracer.fromNullable(tracer);
  }

  /**
   * Build a span. If tracer is {@code null}, returns {@code null}. Meant to be used with other
   * methods for finishing spans
   *
   * @param spanName name of the span
   * @param activeSpan optional parent span to created span. If null, active span is used
   * @param workspaceId optional workspace id to add as tag on span
   * @param machineName optional machine name to add as tag on span
   * @return created span if tracing is enabled, null otherwise.
   */
  @Nullable
  public Span buildSpan(
      String spanName,
      @Nullable Span activeSpan,
      @Nullable String workspaceId,
      @Nullable String machineName) {
    if (tracer == null) {
      return null;
    }

    // By default span is child of tracer.activeSpan(). If `activeSpan` is null,
    // asChildOf() below is a noop.
    SpanBuilder builder = tracer.buildSpan(spanName).asChildOf(activeSpan);
    if (!isNullOrEmpty(workspaceId)) {
      builder.withTag(TracingTags.WORKSPACE_ID.getKey(), workspaceId);
    }
    if (!isNullOrEmpty(machineName)) {
      builder.withTag(TracingTags.MACHINE_NAME.getKey(), machineName);
    }
    return builder.start();
  }

  /**
   * Build a new Span, setting is as the currently active span. Returns null if tracing is disabled.
   *
   * <p>This method is useful for grouping further spans that would otherwise fall under a
   * too-general span.
   *
   * @param spanName name of the span
   * @param workspaceId current workspace id
   * @return the new scope, or null if tracing is disabled.
   */
  @Nullable
  public Scope buildScope(String spanName, @Nullable String workspaceId) {
    if (tracer == null) {
      return null;
    }
    SpanBuilder spanBuilder = tracer.buildSpan(spanName);
    if (!isNullOrEmpty(workspaceId)) {
      spanBuilder.withTag(TracingTags.WORKSPACE_ID.getKey(), workspaceId);
    }
    return spanBuilder.startActive(true);
  }

  /**
   * Convenience method for getting the currently active span.
   *
   * @return the active span, or null if tracing is disabled.
   */
  @Nullable
  public Span getActiveSpan() {
    if (tracer == null) {
      return null;
    }
    return tracer.activeSpan();
  }

  /** If span is not {@code null}, finish span. No-op otherwise. */
  public void finishSpan(@Nullable Span span) {
    if (span != null) {
      span.finish();
    }
  }

  /** If span is not {@code null}, finish span with error reason. No-op otherwise. */
  public void finishSpanAsFailure(@Nullable Span span, String reason) {
    if (span != null) {
      // record the startup as a failure and set the priority so that this span is not throttled
      TracingTags.ERROR.set(span, true);
      TracingTags.SAMPLING_PRIORITY.set(span, 1);
      TracingTags.ERROR_REASON.set(span, reason);
      span.finish();
    }
  }

  /** If scope is non-null, finish it. No-op otherwise. */
  public void finishScope(@Nullable Scope scope) {
    if (scope != null) {
      scope.close();
    }
  }

  /**
   * If scope is not {@code null}, add error status and reason. No-op otherwise.
   *
   * <p>Note this method <b>does not</b> close the scope, and {@link #finishScope(Scope)} must be
   * called to finalize this scope.
   */
  public void addErrorStatusToScope(@Nullable Scope scope, String reason) {
    if (scope != null) {
      // record the startup as a failure and set the priority so that this span is not throttled
      TracingTags.ERROR.set(scope.span(), true);
      TracingTags.SAMPLING_PRIORITY.set(scope.span(), 1);
      TracingTags.ERROR_REASON.set(scope.span(), reason);
    }
  }
}
