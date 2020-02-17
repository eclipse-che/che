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
package org.eclipse.che.workspace.infrastructure.kubernetes.namespace.log;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.Executor;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.kubernetes.KubernetesClientFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.event.PodEvent;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(MockitoTestNGListener.class)
public class LogWatcherTest {

  private final String WORKSPACE_ID = "workspace123";
  private final String NAMESPACE = "namespace123";

  @Mock private PodLogHandler handler;
  @Mock private KubernetesClientFactory clientFactory;
  @Mock private Executor executor;

  @BeforeMethod
  public void setUp() {}

  @Test
  public void executorIsNotCalledWhenContainerIsNull() throws InfrastructureException {
    // given
    when(handler.matchPod(any())).thenReturn(true);

    LogWatcher logWatcher = new LogWatcher(clientFactory, WORKSPACE_ID, NAMESPACE, executor);
    logWatcher.addLogHandler(handler);
    PodEvent podEvent =
        new PodEvent(
            "pod123",
            null,
            "somereallygoodreason",
            "someevenbettermessage",
            "123456789",
            "987654321");

    // when
    logWatcher.handle(podEvent);

    // then
    verify(executor, times(0)).execute(any());
  }

  @Test
  public void executorIsNotCalledWhenPodNameDontMatch() throws InfrastructureException {
    // given
    String podName = "beautifulContainer";
    when(handler.matchPod(podName)).thenReturn(false);

    LogWatcher logWatcher = new LogWatcher(clientFactory, WORKSPACE_ID, NAMESPACE, executor);
    logWatcher.addLogHandler(handler);
    PodEvent podEvent =
        new PodEvent(
            podName, "container123", "Started", "someevenbettermessage", "123456789", "987654321");

    // when
    logWatcher.handle(podEvent);

    // then
    verify(executor, times(0)).execute(any());
  }

  @Test
  public void executorIsNotCalledWhenReasonIsNotStarted() throws InfrastructureException {
    // given
    String podName = "beautifulPod";
    when(handler.matchPod(podName)).thenReturn(true);

    LogWatcher logWatcher = new LogWatcher(clientFactory, WORKSPACE_ID, NAMESPACE, executor);
    logWatcher.addLogHandler(handler);
    PodEvent podEvent =
        new PodEvent(
            podName,
            "container123",
            "NotStarted",
            "someevenbettermessage",
            "123456789",
            "987654321");

    // when
    logWatcher.handle(podEvent);

    // then
    verify(executor, times(0)).execute(any());
  }

  @Test
  public void executorIsCalledWhenAllIsSet() throws InfrastructureException {
    // given
    String podName = "beautifulPod";
    PodLogHandler handler = mock(PodLogHandler.class);
    when(handler.matchPod(podName)).thenReturn(true);

    LogWatcher logWatcher = new LogWatcher(clientFactory, WORKSPACE_ID, NAMESPACE, executor);
    logWatcher.addLogHandler(handler);
    PodEvent podEvent =
        new PodEvent(
            podName, "container123", "Started", "someevenbettermessage", "123456789", "987654321");

    // when
    logWatcher.handle(podEvent);

    // then
    verify(executor, times(1)).execute(any());
  }

  @Test
  public void executorIsCalledJustOnceWhenSameEventArriveAgain() throws InfrastructureException {
    // given
    String podName = "beautifulPod";
    PodLogHandler handler = mock(PodLogHandler.class);
    when(handler.matchPod(podName)).thenReturn(true);

    LogWatcher logWatcher = new LogWatcher(clientFactory, WORKSPACE_ID, NAMESPACE, executor);
    logWatcher.addLogHandler(handler);
    PodEvent podEvent =
        new PodEvent(
            podName, "container123", "Started", "someevenbettermessage", "123456789", "987654321");

    // when
    logWatcher.handle(podEvent);
    logWatcher.handle(podEvent);

    // then
    verify(executor, times(1)).execute(any());
  }

  @Test
  public void executorIsCalledAgainAfterCleanup() throws InfrastructureException {
    // given
    String podName = "beautifulPod";
    PodLogHandler handler = mock(PodLogHandler.class);
    when(handler.matchPod(podName)).thenReturn(true);

    LogWatcher logWatcher = new LogWatcher(clientFactory, WORKSPACE_ID, NAMESPACE, executor);
    logWatcher.addLogHandler(handler);
    PodEvent podEvent =
        new PodEvent(
            podName, "container123", "Started", "someevenbettermessage", "123456789", "987654321");

    // when
    logWatcher.handle(podEvent);
    logWatcher.close();
    logWatcher.handle(podEvent);

    // then
    verify(executor, times(2)).execute(any());
  }
}
