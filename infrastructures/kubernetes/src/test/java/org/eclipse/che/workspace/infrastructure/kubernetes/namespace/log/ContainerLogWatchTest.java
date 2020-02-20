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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.LogWatch;
import io.fabric8.kubernetes.client.dsl.internal.PodOperationsImpl;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(MockitoTestNGListener.class)
public class ContainerLogWatchTest {

  private final String namespace = "namespace123";
  private final String podname = "pod123";
  private final String container = "containre123";

  private final String logMessages = "first_line\nsecond_line";

  @Mock KubernetesClient client;

  @Mock LogWatch logWatch;

  @Mock PodLogHandler podLogHandler;

  @BeforeMethod
  public void setUp() throws IOException {
    PodOperationsImpl pods = mock(PodOperationsImpl.class);
    when(client.pods()).thenReturn(pods);
    when(pods.inNamespace(namespace)).thenReturn(pods);
    when(pods.withName(podname)).thenReturn(pods);
    when(pods.inContainer(container)).thenReturn(pods);
    when(pods.watchLog()).thenReturn(logWatch);
  }

  @Test
  public void testSuccessfulFinishedContainerLogWatch() throws IOException {
    PipedInputStream inputStream = new PipedInputStream();
    PipedOutputStream outputStream = new PipedOutputStream(inputStream);
    outputStream.write(logMessages.getBytes());
    outputStream.close();
    when(logWatch.getOutput()).thenReturn(inputStream);

    ContainerLogWatch clw =
        new ContainerLogWatch(client, namespace, podname, container, podLogHandler);
    clw.run();

    verify(podLogHandler).handle("first_line", container);
    verify(podLogHandler).handle("second_line", container);
  }
}
