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
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.ContainerResource;
import io.fabric8.kubernetes.client.dsl.ExecWatch;
import io.fabric8.kubernetes.client.dsl.LogWatch;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.PodResource;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.eclipse.che.workspace.infrastructure.kubernetes.util.RuntimeEventsPublisher;
import org.mockito.ArgumentCaptor;
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
  private final LogWatchTimeouts TIMEOUTS = new LogWatchTimeouts(100, 0, 0);
  private final long LOG_LIMIT_BYTES = 1024;

  @Mock KubernetesClient client;
  @Mock RuntimeEventsPublisher eventsPublisher;

  @Mock PodLogHandler podLogHandler;

  @Mock MixedOperation<Pod, PodList, PodResource<Pod>> pods;
  @Mock PodResource<Pod> podResource;

  @Mock
  ContainerResource<
          LogWatch,
          InputStream,
          PipedOutputStream,
          OutputStream,
          PipedInputStream,
          String,
          ExecWatch,
          Boolean,
          InputStream,
          Boolean>
      containerResource;

  LogWatchMock logWatch;

  @BeforeMethod
  public void setUp() throws IOException {
    logWatch = new LogWatchMock();

    when(client.pods()).thenReturn(pods);
    when(pods.inNamespace(namespace)).thenReturn(pods);
    when(pods.withName(podname)).thenReturn(podResource);
    when(podResource.inContainer(container)).thenReturn(containerResource);
    when(containerResource.watchLog()).thenReturn(logWatch);
  }

  @Test
  public void testSuccessfulFinishedContainerLogWatch() throws IOException {
    PipedInputStream inputStream = new PipedInputStream();
    PipedOutputStream outputStream = new PipedOutputStream(inputStream);
    outputStream.write("first\nsecond".getBytes());
    outputStream.write("\nthird".getBytes());
    outputStream.close();
    logWatch.setInputStream(inputStream);

    ContainerLogWatch clw =
        new ContainerLogWatch(
            client,
            eventsPublisher,
            namespace,
            podname,
            container,
            podLogHandler,
            TIMEOUTS,
            LOG_LIMIT_BYTES);
    clw.run();

    verify(podLogHandler).handle("first", container);
    verify(podLogHandler).handle("second", container);
    verify(podLogHandler).handle("third", container);
    assertTrue(logWatch.isClosed);

    // verify events were properly fired
    verify(eventsPublisher, times(1)).sendWatchLogStartedEvent(any(String.class));
    verify(eventsPublisher, times(1)).sendWatchLogStoppedEvent(any(String.class));
  }

  @Test
  public void testLimitInputStreamBytes() throws IOException {
    PipedInputStream inputStream = new PipedInputStream();
    PipedOutputStream outputStream = new PipedOutputStream(inputStream);
    outputStream.write("This is long message that won't fit into the limit.".getBytes());
    outputStream.close();
    logWatch.setInputStream(inputStream);

    ContainerLogWatch clw =
        new ContainerLogWatch(
            client, eventsPublisher, namespace, podname, container, podLogHandler, TIMEOUTS, 4);
    clw.run();

    ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<String> containerCaptor = ArgumentCaptor.forClass(String.class);
    verify(podLogHandler, times(1)).handle(messageCaptor.capture(), containerCaptor.capture());
    assertEquals(messageCaptor.getValue(), "This");
    assertEquals(containerCaptor.getValue(), container);
    assertTrue(logWatch.isClosed);

    // verify events were properly fired
    verify(eventsPublisher, times(1)).sendWatchLogStartedEvent(any(String.class));
    verify(eventsPublisher, times(1)).sendWatchLogStoppedEvent(any(String.class));
  }

  @Test
  public void testCloseFromOutside() throws IOException, InterruptedException {
    PipedInputStream inputStream = new PipedInputStream();
    PipedOutputStream outputStream = new PipedOutputStream(inputStream);
    outputStream.write("message\n".getBytes());
    logWatch.setInputStream(inputStream);

    CountDownLatch latch = new CountDownLatch(1);
    doAnswer(
            (a) -> {
              outputStream.write("nextMessage\n".getBytes());
              latch.countDown();
              return null;
            })
        .when(podLogHandler)
        .handle(any(), any());

    ContainerLogWatch clw =
        new ContainerLogWatch(
            client,
            eventsPublisher,
            namespace,
            podname,
            container,
            podLogHandler,
            TIMEOUTS,
            LOG_LIMIT_BYTES);
    new Thread(clw).start();

    latch.await(1, TimeUnit.SECONDS);
    clw.close();

    assertTrue(logWatch.isClosed);

    // verify events were properly fired
    verify(eventsPublisher, times(1)).sendWatchLogStartedEvent(any(String.class));
    verify(eventsPublisher, timeout(1000).times(1)).sendWatchLogStoppedEvent(any(String.class));
  }

  @Test
  public void testCloseOfOutputStream() throws IOException, InterruptedException {
    PipedInputStream inputStream = new PipedInputStream();
    PipedOutputStream outputStream = new PipedOutputStream(inputStream);
    outputStream.write("message\n".getBytes());
    logWatch.setInputStream(inputStream);

    CountDownLatch messageHandleLatch = new CountDownLatch(1);
    doAnswer(
            (a) -> {
              messageHandleLatch.countDown();
              return null;
            })
        .when(podLogHandler)
        .handle("message", container);

    ContainerLogWatch clw =
        new ContainerLogWatch(
            client,
            eventsPublisher,
            namespace,
            podname,
            container,
            podLogHandler,
            TIMEOUTS,
            LOG_LIMIT_BYTES);
    new Thread(clw).start();

    messageHandleLatch.await(1, TimeUnit.SECONDS);
    outputStream.close();

    CountDownLatch closeLatch = new CountDownLatch(1);
    logWatch.setLatch(closeLatch);
    closeLatch.await(1, TimeUnit.SECONDS);
    assertTrue(logWatch.isClosed);

    // verify events were properly fired
    verify(eventsPublisher, times(1)).sendWatchLogStartedEvent(any(String.class));
    verify(eventsPublisher, timeout(1000).times(1)).sendWatchLogStoppedEvent(any(String.class));
  }

  @Test
  public void shouldRetryWhenErrorMessageReceived() throws IOException, InterruptedException {
    // prepare error message logWatch
    String podInitializingMessage =
        "{\"kind\":\"Status\","
            + "\"apiVersion\":\"v1\","
            + "\"metadata\":{},"
            + "\"status\":\"Failure\","
            + "\"message\":\"container \\\""
            + container
            + "\\\" in pod \\\""
            + podname
            + "\\\" is waiting to start: ContainerCreating\","
            + "\"reason\":\"BadRequest\","
            + "\"code\":400}";
    PipedInputStream inputStream = new PipedInputStream();
    PipedOutputStream outputStream = new PipedOutputStream(inputStream);
    outputStream.write((podInitializingMessage + "\n").getBytes());
    outputStream.close();
    logWatch.setInputStream(inputStream);

    LogWatchMock logWatchRegularMessage = new LogWatchMock();
    // prepare regular message logwatch
    inputStream = new PipedInputStream();
    outputStream = new PipedOutputStream(inputStream);
    outputStream.write("message\n".getBytes());
    outputStream.close();
    logWatchRegularMessage.setInputStream(inputStream);
    CountDownLatch messageHandleLatch = new CountDownLatch(2);
    logWatchRegularMessage.setLatch(messageHandleLatch);
    doAnswer(
            (a) -> {
              messageHandleLatch.countDown();
              return null;
            })
        .when(podLogHandler)
        .handle("message", container);

    // return error message logwatch first and regular message logwatch on second call
    when(containerResource.watchLog()).thenReturn(logWatch).thenReturn(logWatchRegularMessage);

    ContainerLogWatch clw =
        new ContainerLogWatch(
            client,
            eventsPublisher,
            namespace,
            podname,
            container,
            podLogHandler,
            TIMEOUTS,
            LOG_LIMIT_BYTES);
    new Thread(clw).start();

    // wait for logwatch close, it means that error message was processed
    CountDownLatch closeLatch = new CountDownLatch(1);
    logWatch.setLatch(closeLatch);
    closeLatch.await(1, TimeUnit.SECONDS);
    assertTrue(logWatch.isClosed);

    // wait for regular message
    messageHandleLatch.await(1, TimeUnit.SECONDS);

    // message was processed
    verify(podLogHandler).handle("message", container);
    assertTrue(logWatchRegularMessage.isClosed);

    // verify events were properly fired
    verify(eventsPublisher, times(2)).sendWatchLogStartedEvent(any(String.class));
    verify(eventsPublisher, timeout(1000).times(2)).sendWatchLogStoppedEvent(any(String.class));
  }

  @Test
  public void shouldRetryWhenOutputIsNullFirst() throws IOException, InterruptedException {
    logWatch.setInputStream(null);

    LogWatchMock logWatchRegularMessage = new LogWatchMock();
    // prepare regular message logwatch
    PipedInputStream inputStream = new PipedInputStream();
    PipedOutputStream outputStream = new PipedOutputStream(inputStream);
    outputStream.write("message\n".getBytes());
    outputStream.close();
    logWatchRegularMessage.setInputStream(inputStream);
    CountDownLatch messageHandleLatch = new CountDownLatch(2);
    logWatchRegularMessage.setLatch(messageHandleLatch);
    doAnswer(
            (a) -> {
              messageHandleLatch.countDown();
              return null;
            })
        .when(podLogHandler)
        .handle("message", container);

    // return null stream first and regular message stream on second call
    when(containerResource.watchLog()).thenReturn(logWatch).thenReturn(logWatchRegularMessage);

    ContainerLogWatch clw =
        new ContainerLogWatch(
            client,
            eventsPublisher,
            namespace,
            podname,
            container,
            podLogHandler,
            TIMEOUTS,
            LOG_LIMIT_BYTES);
    new Thread(clw).start();

    // wait for logwatch close, it means that error message was processed
    CountDownLatch closeLatch = new CountDownLatch(1);
    logWatch.setLatch(closeLatch);
    closeLatch.await(1, TimeUnit.SECONDS);
    assertTrue(logWatch.isClosed);

    // wait for regular message
    messageHandleLatch.await(1, TimeUnit.SECONDS);

    // message was processed
    verify(podLogHandler).handle("message", container);
    assertTrue(logWatchRegularMessage.isClosed);

    // verify events were properly fired
    verify(eventsPublisher, times(2)).sendWatchLogStartedEvent(any(String.class));
    verify(eventsPublisher, timeout(1000).times(2)).sendWatchLogStoppedEvent(any(String.class));
  }

  private class LogWatchMock implements LogWatch {

    private InputStream inputStream;
    private CountDownLatch latch;
    private boolean isClosed = false;

    private LogWatchMock() {}

    public void setLatch(CountDownLatch latch) {
      this.latch = latch;
    }

    public void setInputStream(InputStream inputStream) {
      this.inputStream = inputStream;
    }

    @Override
    public InputStream getOutput() {
      return inputStream;
    }

    @Override
    public void close() {
      isClosed = true;
      if (inputStream != null) {
        try {
          inputStream.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
      if (latch != null) {
        latch.countDown();
      }
    }
  }
}
