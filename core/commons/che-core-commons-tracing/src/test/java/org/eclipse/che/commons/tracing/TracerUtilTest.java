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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.Tracer.SpanBuilder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(MockitoTestNGListener.class)
public class TracerUtilTest {

  private static final String SPAN_NAME = "spanName";
  private static final String WORKSPACE_ID = "workspaceId";
  private static final String MACHINE_NAME = "machineName";
  private static final String FAILURE_REASON = "testFailure";
  @Mock private Span activeSpan;
  @Mock private Span testSpan;
  @Mock private Scope testScope;

  // TODO: use MockTracer here (needs CQ)
  @Mock private Tracer tracer;
  @Mock private SpanBuilder spanBuilder;
  @InjectMocks private OptionalTracer optionalTracer;
  private TracerUtil tracerUtil;
  private TracerUtil disabledTracerUtil;

  @BeforeMethod
  public void setUp() throws Exception {
    lenient().when(tracer.buildSpan(anyString())).thenReturn(spanBuilder);
    lenient().when(spanBuilder.asChildOf(any(Span.class))).thenReturn(spanBuilder);
    lenient().when(spanBuilder.asChildOf(isNull(Span.class))).thenReturn(spanBuilder);
    lenient().when(spanBuilder.withTag(anyString(), anyString())).thenReturn(spanBuilder);
    lenient().when(spanBuilder.start()).thenReturn(testSpan);
    lenient().when(spanBuilder.startActive(anyBoolean())).thenReturn(testScope);
    tracerUtil = new TracerUtil(optionalTracer);
    disabledTracerUtil = new TracerUtil(null);
  }

  @Test
  public void buildsSpanCorrectly() throws Exception {
    // When
    Span actual = tracerUtil.buildSpan(SPAN_NAME, activeSpan, WORKSPACE_ID, MACHINE_NAME);

    // Then
    verify(tracer).buildSpan(SPAN_NAME);
    verify(spanBuilder).asChildOf(activeSpan);
    verify(spanBuilder).withTag(TracingTags.WORKSPACE_ID.getKey(), WORKSPACE_ID);
    verify(spanBuilder).withTag(TracingTags.MACHINE_NAME.getKey(), MACHINE_NAME);
    verify(spanBuilder).start();
    verifyNoMoreInteractions(spanBuilder);
    assertNotNull(actual, "Should return span");
  }

  @Test
  public void buildsSpanCorrectlyWithoutActiveSpan() throws Exception {
    // When
    Span actual = tracerUtil.buildSpan(SPAN_NAME, null, WORKSPACE_ID, MACHINE_NAME);

    // Then
    verify(tracer).buildSpan(SPAN_NAME);
    verify(spanBuilder).withTag(TracingTags.WORKSPACE_ID.getKey(), WORKSPACE_ID);
    verify(spanBuilder).withTag(TracingTags.MACHINE_NAME.getKey(), MACHINE_NAME);
    verify(spanBuilder).start();
    assertNotNull(actual, "Should return span");
  }

  @Test
  public void buildsSpanCorrectlyWithoutWorkspaceId() throws Exception {
    // When
    Span actual = tracerUtil.buildSpan(SPAN_NAME, activeSpan, null, MACHINE_NAME);

    // Then
    verify(tracer).buildSpan(SPAN_NAME);
    verify(spanBuilder).asChildOf(activeSpan);
    verify(spanBuilder).withTag(TracingTags.MACHINE_NAME.getKey(), MACHINE_NAME);
    verify(spanBuilder).start();
    verifyNoMoreInteractions(spanBuilder);
    assertNotNull(actual, "Should return span");
  }

  @Test
  public void buildsSpanCorrectlyWithoutMachineName() throws Exception {
    // When
    Span actual = tracerUtil.buildSpan(SPAN_NAME, activeSpan, WORKSPACE_ID, null);

    // Then
    verify(tracer).buildSpan(SPAN_NAME);
    verify(spanBuilder).asChildOf(activeSpan);
    verify(spanBuilder).withTag(TracingTags.WORKSPACE_ID.getKey(), WORKSPACE_ID);
    verify(spanBuilder).start();
    verifyNoMoreInteractions(spanBuilder);
    assertEquals(actual, testSpan);
  }

  @Test
  public void buildsScopeCorrectly() throws Exception {
    // When
    Scope actual = tracerUtil.buildScope(SPAN_NAME, WORKSPACE_ID);

    // Then
    verify(tracer).buildSpan(SPAN_NAME);
    verify(spanBuilder).withTag(TracingTags.WORKSPACE_ID.getKey(), WORKSPACE_ID);
    verify(spanBuilder).startActive(true);
    verifyNoMoreInteractions(spanBuilder);
    assertEquals(actual, testScope);
  }

  @Test
  public void buildScopeCorrectlyWithoutWorkspaceId() throws Exception {
    // When
    Scope actual = tracerUtil.buildScope(SPAN_NAME, null);

    // Then
    verify(tracer).buildSpan(SPAN_NAME);
    verify(spanBuilder).startActive(true);
    verifyNoMoreInteractions(spanBuilder);
    assertEquals(actual, testScope);
  }

  @Test
  public void finishSpanIfNotNull() throws Exception {
    // When
    tracerUtil.finishSpan(testSpan);

    // Then
    verify(testSpan).finish();
  }

  @Test
  public void finishSpanWithErrorIfFailed() throws Exception {
    // When
    tracerUtil.finishSpanAsFailure(testSpan, FAILURE_REASON);

    // Then
    verify(testSpan).setTag(TracingTags.ERROR.getKey(), true);
    verify(testSpan).setTag(TracingTags.ERROR_REASON.getKey(), FAILURE_REASON);
    verify(testSpan).finish();
  }

  @Test
  public void closeScopeIfNotNull() throws Exception {
    // When
    tracerUtil.finishScope(testScope);

    // Then
    verify(testScope).close();
  }

  @Test
  public void addErrorToScopeIfFailed() throws Exception {
    // Given
    when(testScope.span()).thenReturn(testSpan);

    // When
    tracerUtil.addErrorStatusToScope(testScope, FAILURE_REASON);

    // Then
    verify(testSpan).setTag(TracingTags.ERROR.getKey(), true);
    verify(testSpan).setTag(TracingTags.ERROR_REASON.getKey(), FAILURE_REASON);
    verify(testScope, never()).close();
  }

  @Test
  public void buildSpanShouldDoNothingIfTracingDisabled() throws Exception {
    // When
    Span actual = disabledTracerUtil.buildSpan(SPAN_NAME, null, WORKSPACE_ID, MACHINE_NAME);

    // Then
    assertEquals(actual, null);
  }

  @Test
  public void finishSpanShouldDoNothingIfSpanIsNull() throws Exception {
    // When
    disabledTracerUtil.finishSpan(null);
    tracerUtil.finishSpan(null);

    // Then (no exception thrown)
  }

  @Test
  public void finishSpanAsFailureShouldDoNothingIfSpanIsNull() throws Exception {
    // When
    disabledTracerUtil.finishSpanAsFailure(null, "test failure");
    tracerUtil.finishSpanAsFailure(null, "test failure");

    // Then (no exception thrown)
  }

  @Test
  public void getActiveSpanShouldDoNothingIfTracingDisabled() throws Exception {
    // When
    Span actual = disabledTracerUtil.getActiveSpan();

    // Then
    assertEquals(actual, null);
  }

  @Test
  public void buildScopeShouldDoNothingWhenTracingDisabled() throws Exception {
    // When
    Scope actual = disabledTracerUtil.buildScope(SPAN_NAME, WORKSPACE_ID);

    // Then
    assertEquals(actual, null);
  }

  @Test
  public void finishScopeShouldDoNothingWhenTracingDisabled() throws Exception {
    // When
    disabledTracerUtil.finishScope(null);
    tracerUtil.finishScope(null);

    // Then (no exception thrown)
  }

  @Test
  public void finishScopeAsFailureShouldDoNothingWhenTracingDisabled() throws Exception {
    // When
    disabledTracerUtil.addErrorStatusToScope(null, FAILURE_REASON);
    tracerUtil.addErrorStatusToScope(null, FAILURE_REASON);

    // Then (no exception thrown)
  }
}
