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
import static org.mockito.Mockito.verify;
import static org.testng.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.workspace.infrastructure.kubernetes.util.RuntimeEventsPublisher;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(MockitoTestNGListener.class)
public class PodLogHandlerToEventPublisherTest {

  @Mock RuntimeEventsPublisher eventsPublisher;

  @Mock RuntimeIdentity identity;

  @Test
  public void matchPodWhenInSet() {
    PodLogHandler handler =
        new PodLogHandlerToEventPublisher(
            eventsPublisher, identity, new HashSet<>(Arrays.asList("one", "two", "three")));

    assertTrue(handler.matchPod("one"));
    assertTrue(handler.matchPod("two"));
    assertTrue(handler.matchPod("three"));
    assertFalse(handler.matchPod("zero"));
  }

  @Test
  public void dontMatchPodWhenEmptySet() {
    PodLogHandler handler =
        new PodLogHandlerToEventPublisher(eventsPublisher, identity, Collections.emptySet());

    assertFalse(handler.matchPod("anything"));
  }

  @Test
  public void sendMessageToPublisher() {
    PodLogHandler handler =
        new PodLogHandlerToEventPublisher(eventsPublisher, identity, Collections.emptySet());

    handler.handle("message", "containerName");

    ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
    verify(eventsPublisher)
        .sendRuntimeLogEvent(
            messageCaptor.capture(), any(String.class), any(RuntimeIdentity.class));
    String capturedMessage = messageCaptor.getValue();
    assertTrue(capturedMessage.contains("message"));
    assertTrue(capturedMessage.contains("containerName"));
  }
}
