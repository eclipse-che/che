/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.kubernetes.namespace.log;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.eclipse.che.api.workspace.shared.Constants.DEBUG_WORKSPACE_START;
import static org.eclipse.che.workspace.infrastructure.kubernetes.namespace.log.LogWatcher.DEFAULT_LOG_LIMIT_BYTES;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertEquals;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.shared.Constants;
import org.eclipse.che.workspace.infrastructure.kubernetes.KubernetesClientFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.event.PodEvent;
import org.eclipse.che.workspace.infrastructure.kubernetes.util.RuntimeEventsPublisher;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(MockitoTestNGListener.class)
public class LogWatcherTest {

  private final String POD = "pod123";
  private final Set<String> PODNAMES = Collections.singleton(POD);
  private final String WORKSPACE_ID = "workspace123";
  private final String NAMESPACE = "namespace123";
  private final LogWatchTimeouts TIMEOUTS = new LogWatchTimeouts(100, 0, 0);
  private final long LIMIT_BYTES = 64;

  @Mock private PodLogHandler handler;
  @Mock private KubernetesClientFactory clientFactory;
  @Mock private RuntimeEventsPublisher eventsPublisher;
  @Mock private Executor executor;

  @BeforeMethod
  public void setUp() {}

  @Test
  public void executorIsNotCalledWhenContainerIsNull() throws InfrastructureException {
    // given
    LogWatcher logWatcher =
        new LogWatcher(
            clientFactory,
            eventsPublisher,
            WORKSPACE_ID,
            NAMESPACE,
            PODNAMES,
            executor,
            TIMEOUTS,
            LIMIT_BYTES);
    logWatcher.addLogHandler(handler);
    PodEvent podEvent =
        new PodEvent(
            POD, null, "somereallygoodreason", "someevenbettermessage", "123456789", "987654321");

    // when
    logWatcher.handle(podEvent);

    // then
    verify(executor, times(0)).execute(any());
  }

  @Test
  public void executorIsNotCalledWhenPodNameDontMatch() throws InfrastructureException {
    // given
    LogWatcher logWatcher =
        new LogWatcher(
            clientFactory,
            eventsPublisher,
            WORKSPACE_ID,
            NAMESPACE,
            PODNAMES,
            executor,
            TIMEOUTS,
            LIMIT_BYTES);
    logWatcher.addLogHandler(handler);
    PodEvent podEvent =
        new PodEvent(
            "someOtherPod",
            "container123",
            "Started",
            "someevenbettermessage",
            "123456789",
            "987654321");

    // when
    logWatcher.handle(podEvent);

    // then
    verify(executor, times(0)).execute(any());
  }

  @Test
  public void executorIsNotCalledWhenReasonIsNotStarted() throws InfrastructureException {
    // given
    LogWatcher logWatcher =
        new LogWatcher(
            clientFactory,
            eventsPublisher,
            WORKSPACE_ID,
            NAMESPACE,
            PODNAMES,
            executor,
            TIMEOUTS,
            LIMIT_BYTES);
    logWatcher.addLogHandler(handler);
    PodEvent podEvent =
        new PodEvent(
            POD, "container123", "NotStarted", "someevenbettermessage", "123456789", "987654321");

    // when
    logWatcher.handle(podEvent);

    // then
    verify(executor, times(0)).execute(any());
  }

  @Test
  public void executorIsCalledWhenAllIsSet() throws InfrastructureException {
    // given
    LogWatcher logWatcher =
        new LogWatcher(
            clientFactory,
            eventsPublisher,
            WORKSPACE_ID,
            NAMESPACE,
            PODNAMES,
            executor,
            TIMEOUTS,
            LIMIT_BYTES);
    logWatcher.addLogHandler(handler);
    PodEvent podEvent =
        new PodEvent(
            POD, "container123", "Started", "someevenbettermessage", "123456789", "987654321");

    // when
    logWatcher.handle(podEvent);

    // then
    verify(executor, times(1)).execute(any());
  }

  @Test
  public void executorIsCalledJustOnceWhenSameEventArriveAgain() throws InfrastructureException {
    // given
    LogWatcher logWatcher =
        new LogWatcher(
            clientFactory,
            eventsPublisher,
            WORKSPACE_ID,
            NAMESPACE,
            PODNAMES,
            executor,
            TIMEOUTS,
            LIMIT_BYTES);
    logWatcher.addLogHandler(handler);
    PodEvent podEvent =
        new PodEvent(
            POD, "container123", "Started", "someevenbettermessage", "123456789", "987654321");

    // when
    logWatcher.handle(podEvent);
    logWatcher.handle(podEvent);

    // then
    verify(executor, times(1)).execute(any());
  }

  @Test
  public void executorIsNotCalledAgainAfterCleanup() throws InfrastructureException {
    // given
    LogWatcher logWatcher =
        new LogWatcher(
            clientFactory,
            eventsPublisher,
            WORKSPACE_ID,
            NAMESPACE,
            PODNAMES,
            executor,
            TIMEOUTS,
            LIMIT_BYTES);
    logWatcher.addLogHandler(handler);
    PodEvent podEvent =
        new PodEvent(
            POD, "container123", "Started", "someevenbettermessage", "123456789", "987654321");

    // when
    logWatcher.handle(podEvent);
    logWatcher.close();
    logWatcher.handle(podEvent);

    // then
    verify(executor, times(1)).execute(any());
  }

  @DataProvider
  public Object[][] logLimitData() {
    return new Object[][] {
      {null, DEFAULT_LOG_LIMIT_BYTES},
      {emptyMap(), DEFAULT_LOG_LIMIT_BYTES},
      {singletonMap("bla", "bol"), DEFAULT_LOG_LIMIT_BYTES},
      {
        ImmutableMap.of(Constants.DEBUG_WORKSPACE_START_LOG_LIMIT_BYTES, "123", "other", "value"),
        123
      }
    };
  }

  @Test(dataProvider = "logLimitData")
  public void testGettingLogLimitBytes(Map<String, String> startOptions, long expectedLimit) {
    assertEquals(LogWatcher.getLogLimitBytes(startOptions), expectedLimit);
  }

  @DataProvider
  public Object[][] shouldWatchLogsData() {
    return new Object[][] {
      {null, false},
      {emptyMap(), false},
      {singletonMap("bla", "bol"), false},
      {singletonMap(DEBUG_WORKSPACE_START, "blbost"), false},
      {singletonMap(DEBUG_WORKSPACE_START, "false"), false},
      {singletonMap(DEBUG_WORKSPACE_START, "true"), true},
      {ImmutableMap.of(DEBUG_WORKSPACE_START, "true", "bla", "bol"), true},
      {ImmutableMap.of(DEBUG_WORKSPACE_START, "tttt", "bla", "bol"), false},
    };
  }

  @Test(dataProvider = "shouldWatchLogsData")
  public void testShouldWatchLogsFromStartOptions(
      Map<String, String> startOptions, boolean expected) {
    assertEquals(LogWatcher.shouldWatchLogs(startOptions), expected);
  }
}
