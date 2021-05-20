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
package org.eclipse.che.workspace.infrastructure.kubernetes.namespace.log;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.*;

import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.workspace.infrastructure.kubernetes.util.RuntimeEventsPublisher;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(MockitoTestNGListener.class)
public class PodLogToEventPublisherTest {

  @Mock RuntimeEventsPublisher eventsPublisher;

  @Mock RuntimeIdentity identity;

  @Test
  public void sendMessageToPublisher() {
    PodLogHandler handler = new PodLogToEventPublisher(eventsPublisher, identity);

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
