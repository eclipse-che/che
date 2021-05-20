/*
 * Copyright (c) 2012-2020 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.kubernetes.util;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableSet;
import java.util.Date;
import java.util.Set;
import java.util.function.Consumer;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.event.PodEvent;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests {@link UnrecoverablePodEventListener}.
 *
 * @author Sergii Leshchenko
 * @author Ilya Buziuk
 */
@Listeners(MockitoTestNGListener.class)
public class UnrecoverablePodEventListenerTest {

  private static final String WORKSPACE_POD_NAME = "app";
  private static final String CONTAINER_NAME_1 = "test1";
  private static final String EVENT_CREATION_TIMESTAMP = "2018-05-15T16:17:54Z";

  /* Pods created by a deployment are created with a random suffix, so Pod names won't match
  exactly. */
  private static final String POD_NAME_RANDOM_SUFFIX = "-12345";

  private static final Set<String> UNRECOVERABLE_EVENTS =
      ImmutableSet.of("Failed Mount", "Failed Scheduling", "Failed to pull image");

  @Mock private Consumer<PodEvent> unrecoverableEventConsumer;

  private UnrecoverablePodEventListener unrecoverableEventListener;

  @BeforeMethod
  public void setUp() {
    unrecoverableEventListener =
        new UnrecoverablePodEventListener(
            UNRECOVERABLE_EVENTS, ImmutableSet.of(WORKSPACE_POD_NAME), unrecoverableEventConsumer);
  }

  @Test
  public void testHandleUnrecoverableEventByReason() throws Exception {
    // given
    String unrecoverableEventReason = "Failed Mount";
    PodEvent unrecoverableEvent =
        mockContainerEvent(
            WORKSPACE_POD_NAME,
            unrecoverableEventReason,
            "Failed to mount volume 'claim-che-workspace'",
            EVENT_CREATION_TIMESTAMP,
            getCurrentTimestampWithOneHourShiftAhead());

    // when
    unrecoverableEventListener.handle(unrecoverableEvent);

    // then
    verify(unrecoverableEventConsumer).accept(unrecoverableEvent);
  }

  @Test
  public void testHandleUnrecoverableEventByMessage() throws Exception {
    // given
    String unrecoverableEventMessage = "Failed to pull image eclipse/che-server:nightly-centos";
    PodEvent unrecoverableEvent =
        mockContainerEvent(
            WORKSPACE_POD_NAME,
            "Pulling",
            unrecoverableEventMessage,
            EVENT_CREATION_TIMESTAMP,
            getCurrentTimestampWithOneHourShiftAhead());

    // when
    unrecoverableEventListener.handle(unrecoverableEvent);

    // then
    verify(unrecoverableEventConsumer).accept(unrecoverableEvent);
  }

  @Test
  public void testDoNotHandleUnrecoverableEventFromNonWorkspacePod() throws Exception {
    // given
    final String unrecoverableEventMessage =
        "Failed to pull image eclipse/che-server:nightly-centos";
    final PodEvent unrecoverableEvent =
        mockContainerEvent(
            "NonWorkspacePod",
            "Pulling",
            unrecoverableEventMessage,
            EVENT_CREATION_TIMESTAMP,
            getCurrentTimestampWithOneHourShiftAhead());

    // when
    unrecoverableEventListener.handle(unrecoverableEvent);

    // then
    verify(unrecoverableEventConsumer, never()).accept(any());
  }

  @Test
  public void testHandleRegularEvent() throws Exception {
    // given
    final PodEvent regularEvent =
        mockContainerEvent(
            WORKSPACE_POD_NAME,
            "Pulling",
            "pulling image",
            EVENT_CREATION_TIMESTAMP,
            getCurrentTimestampWithOneHourShiftAhead());

    // when
    unrecoverableEventListener.handle(regularEvent);

    // then
    verify(unrecoverableEventConsumer, never()).accept(any());
  }

  @Test
  public void testFailedContainersInWorkspacePodAlwaysHandled() {
    // given
    PodEvent ev =
        mockContainerEvent(
            WORKSPACE_POD_NAME,
            "Failed",
            "bah",
            EVENT_CREATION_TIMESTAMP,
            getCurrentTimestampWithOneHourShiftAhead());

    // when
    unrecoverableEventListener.handle(ev);

    // then
    verify(unrecoverableEventConsumer).accept(any());
  }

  /**
   * Mock a container event, as though it was triggered by the OpenShift API. As workspace Pods are
   * created indirectly through deployments, they are given generated names with the provided name
   * as a root. <br>
   * Use this method in a test to ensure that tested code manages this fact correctly. For example,
   * code such as unrecoverable events handling cannot directly look at an event's pod name and
   * compare it to the internal representation, and so must confirm the event is relevant in some
   * other way.
   */
  private static PodEvent mockContainerEvent(
      String podName,
      String reason,
      String message,
      String creationTimestamp,
      String lastTimestamp) {
    final PodEvent event = mock(PodEvent.class);
    when(event.getPodName()).thenReturn(podName + POD_NAME_RANDOM_SUFFIX);
    lenient().when(event.getContainerName()).thenReturn(CONTAINER_NAME_1);
    lenient().when(event.getReason()).thenReturn(reason);
    lenient().when(event.getMessage()).thenReturn(message);
    lenient().when(event.getCreationTimeStamp()).thenReturn(creationTimestamp);
    lenient().when(event.getLastTimestamp()).thenReturn(lastTimestamp);
    return event;
  }

  private String getCurrentTimestampWithOneHourShiftAhead() {
    Date currentTimestampWithOneHourShiftAhead = new Date(new Date().getTime() + 3600 * 1000);
    return PodEvents.convertDateToEventTimestamp(currentTimestampWithOneHourShiftAhead);
  }
}
