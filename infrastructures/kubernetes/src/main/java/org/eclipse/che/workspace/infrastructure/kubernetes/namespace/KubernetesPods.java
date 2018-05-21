/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.kubernetes.namespace;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.concurrent.CompletableFuture.allOf;
import static org.eclipse.che.workspace.infrastructure.kubernetes.Constants.CHE_WORKSPACE_ID_LABEL;
import static org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesObjectUtil.putLabel;

import io.fabric8.kubernetes.api.model.DoneablePod;
import io.fabric8.kubernetes.api.model.Event;
import io.fabric8.kubernetes.api.model.ObjectReference;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.dsl.ExecListener;
import io.fabric8.kubernetes.client.dsl.ExecWatch;
import io.fabric8.kubernetes.client.dsl.PodResource;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import okhttp3.Response;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.kubernetes.KubernetesClientFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.KubernetesInfrastructureException;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.event.ContainerEvent;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.event.ContainerEventHandler;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.event.PodActionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Defines an internal API for managing {@link Pod} instances in {@link KubernetesPods#namespace
 * predefined namespace}.
 *
 * @author Sergii Leshchenko
 * @author Anton Korneta
 */
public class KubernetesPods {

  private static final Logger LOG = LoggerFactory.getLogger(KubernetesPods.class);

  private static final String CONTAINER_NAME_GROUP = "name";
  // when event is related to container `fieldPath` field contains
  // information in the following format: `spec.container{web}`, where `web` is container name
  private static final Pattern CONTAINER_FIELD_PATH_PATTERN =
      Pattern.compile("spec.containers\\{(?<" + CONTAINER_NAME_GROUP + ">.*)}");

  // TODO https://github.com/eclipse/che/issues/7656
  public static final int POD_REMOVAL_TIMEOUT_MIN = 5;

  private static final String POD_OBJECT_KIND = "Pod";
  // error stream data initial capacity
  public static final int ERROR_BUFF_INITIAL_CAP = 2048;
  public static final String STDOUT = "stdout";
  public static final String STDERR = "stderr";

  private final String namespace;
  private final KubernetesClientFactory clientFactory;
  private final ConcurrentLinkedQueue<PodActionHandler> podActionHandlers;
  private final ConcurrentLinkedQueue<ContainerEventHandler> containerEventsHandlers;
  private final String workspaceId;
  private Watch podWatch;
  private Watch containerWatch;

  KubernetesPods(String namespace, String workspaceId, KubernetesClientFactory clientFactory) {
    this.namespace = namespace;
    this.workspaceId = workspaceId;
    this.clientFactory = clientFactory;
    this.containerEventsHandlers = new ConcurrentLinkedQueue<>();
    this.podActionHandlers = new ConcurrentLinkedQueue<>();
  }

  /**
   * Creates specified pod.
   *
   * @param pod pod to create
   * @return created pod
   * @throws InfrastructureException when any exception occurs
   */
  public Pod create(Pod pod) throws InfrastructureException {
    putLabel(pod, CHE_WORKSPACE_ID_LABEL, workspaceId);
    try {
      return clientFactory.create(workspaceId).pods().inNamespace(namespace).create(pod);
    } catch (KubernetesClientException e) {
      throw new KubernetesInfrastructureException(e);
    }
  }

  /**
   * Returns all existing pods.
   *
   * @throws InfrastructureException when any exception occurs
   */
  public List<Pod> get() throws InfrastructureException {
    try {
      return clientFactory
          .create(workspaceId)
          .pods()
          .inNamespace(namespace)
          .withLabel(CHE_WORKSPACE_ID_LABEL, workspaceId)
          .list()
          .getItems();
    } catch (KubernetesClientException e) {
      throw new KubernetesInfrastructureException(e);
    }
  }

  /**
   * Returns optional with pod that have specified name.
   *
   * @throws InfrastructureException when any exception occurs
   */
  public Optional<Pod> get(String name) throws InfrastructureException {
    try {
      return Optional.ofNullable(
          clientFactory.create(workspaceId).pods().inNamespace(namespace).withName(name).get());
    } catch (KubernetesClientException e) {
      throw new KubernetesInfrastructureException(e);
    }
  }

  /**
   * Waits until pod state will suit for specified predicate.
   *
   * @param name name of pod that should be watched
   * @param timeoutMin waiting timeout in minutes
   * @param predicate predicate to perform state check
   * @return pod that suit for specified predicate
   * @throws InfrastructureException when specified timeout is reached
   * @throws InfrastructureException when {@link Thread} is interrupted while waiting
   * @throws InfrastructureException when any other exception occurs
   */
  public Pod wait(String name, int timeoutMin, Predicate<Pod> predicate)
      throws InfrastructureException {
    CompletableFuture<Pod> future = new CompletableFuture<>();
    Watch watch = null;
    try {

      PodResource<Pod, DoneablePod> podResource =
          clientFactory.create(workspaceId).pods().inNamespace(namespace).withName(name);

      watch =
          podResource.watch(
              new Watcher<Pod>() {
                @Override
                public void eventReceived(Action action, Pod pod) {
                  if (predicate.test(pod)) {
                    future.complete(pod);
                  }
                }

                @Override
                public void onClose(KubernetesClientException cause) {
                  future.completeExceptionally(
                      new InfrastructureException(
                          "Waiting for pod '" + name + "' was interrupted"));
                }
              });

      Pod actualPod = podResource.get();
      if (actualPod == null) {
        throw new InfrastructureException("Specified pod " + name + " doesn't exist");
      }
      if (predicate.test(actualPod)) {
        return actualPod;
      }
      try {
        return future.get(timeoutMin, TimeUnit.MINUTES);
      } catch (ExecutionException e) {
        throw new InfrastructureException(e.getCause().getMessage(), e);
      } catch (TimeoutException e) {
        throw new InfrastructureException("Waiting for pod '" + name + "' reached timeout");
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new InfrastructureException("Waiting for pod '" + name + "' was interrupted");
      }
    } catch (KubernetesClientException e) {
      throw new KubernetesInfrastructureException(e);
    } finally {
      if (watch != null) {
        watch.close();
      }
    }
  }

  /**
   * Subscribes to pod events and returns the resulting future, which ends when a pod event that
   * satisfies the predicate is received.
   *
   * <p>Note that the resulting future must be explicitly cancelled when its completion no longer
   * important because of finalization allocated resources.
   *
   * @param name the pod name that should be watched
   * @param predicate a function that performs pod state check
   * @return completable future that is completed when one of the following conditions is met:
   *     <ul>
   *       <li>an event that satisfies predicate is received
   *       <li>exception while getting pod resource occurred
   *       <li>connection problem occurred
   *     </ul>
   *     otherwise, it must be explicitly closed
   */
  public CompletableFuture<Void> waitAsync(String name, Predicate<Pod> predicate) {
    final CompletableFuture<Void> podRunningFuture = new CompletableFuture<>();
    try {
      final PodResource<Pod, DoneablePod> podResource =
          clientFactory.create().pods().inNamespace(namespace).withName(name);
      final Watch watch =
          podResource.watch(
              new Watcher<Pod>() {
                @Override
                public void eventReceived(Action action, Pod pod) {
                  if (predicate.test(pod)) {
                    podRunningFuture.complete(null);
                  }
                }

                @Override
                public void onClose(KubernetesClientException cause) {
                  podRunningFuture.completeExceptionally(
                      new InfrastructureException(
                          "Waiting for pod '" + name + "' was interrupted"));
                }
              });

      podRunningFuture.whenComplete((ok, ex) -> watch.close());
      final Pod pod = podResource.get();
      if (pod == null) {
        podRunningFuture.completeExceptionally(
            new InfrastructureException("Specified pod " + name + " doesn't exist"));
      }
      if (predicate.test(pod)) {
        podRunningFuture.complete(null);
      }
    } catch (KubernetesClientException | InfrastructureException ex) {
      podRunningFuture.completeExceptionally(ex);
    }
    return podRunningFuture;
  }

  /**
   * Starts watching the pods inside Kubernetes namespace and registers a specified handler for such
   * events. Note that watcher can be started only once so two times invocation of this method will
   * not produce new watcher and just register the event handlers.
   *
   * @param handler pod action events handler
   * @throws InfrastructureException if any error occurs while watcher starting
   */
  public void watch(PodActionHandler handler) throws InfrastructureException {
    if (podWatch == null) {
      final Watcher<Pod> watcher =
          new Watcher<Pod>() {
            @Override
            public void eventReceived(Action action, Pod pod) {
              podActionHandlers.forEach(h -> h.handle(action, pod));
            }

            @Override
            public void onClose(KubernetesClientException ignored) {}
          };
      try {
        podWatch =
            clientFactory
                .create(workspaceId)
                .pods()
                .inNamespace(namespace)
                .withLabel(CHE_WORKSPACE_ID_LABEL, workspaceId)
                .watch(watcher);
      } catch (KubernetesClientException ex) {
        throw new KubernetesInfrastructureException(ex);
      }
    }
    podActionHandlers.add(handler);
  }

  /**
   * Registers a specified handler for handling events about changes in pods containers.
   *
   * @param handler pod container events handler
   * @throws InfrastructureException if any error occurs while watcher starting
   */
  public void watchContainers(ContainerEventHandler handler) throws InfrastructureException {
    if (containerWatch == null) {
      final Watcher<Event> watcher =
          new Watcher<Event>() {
            @Override
            public void eventReceived(Action action, Event event) {
              ObjectReference involvedObject = event.getInvolvedObject();
              String fieldPath = involvedObject.getFieldPath();

              // check that event related to
              if (POD_OBJECT_KIND.equals(involvedObject.getKind()) && fieldPath != null) {
                Matcher containerFieldMatcher = CONTAINER_FIELD_PATH_PATTERN.matcher(fieldPath);
                if (containerFieldMatcher.matches()) {

                  String podName = involvedObject.getName();
                  String containerName = containerFieldMatcher.group(CONTAINER_NAME_GROUP);

                  ContainerEvent containerEvent =
                      new ContainerEvent(
                          podName,
                          containerName,
                          event.getReason(),
                          event.getMessage(),
                          event.getMetadata().getCreationTimestamp(),
                          event.getLastTimestamp());

                  containerEventsHandlers.forEach(h -> h.handle(containerEvent));
                }
              }
            }

            @Override
            public void onClose(KubernetesClientException ignored) {}
          };
      try {
        containerWatch =
            clientFactory.create(workspaceId).events().inNamespace(namespace).watch(watcher);
      } catch (KubernetesClientException ex) {
        throw new KubernetesInfrastructureException(ex);
      }
    }
    containerEventsHandlers.add(handler);
  }

  /** Stops watching the pods inside Kubernetes namespace. */
  public void stopWatch() {
    try {
      if (podWatch != null) {
        podWatch.close();
      }
    } catch (KubernetesClientException ex) {
      LOG.error(
          "Failed to stop pod watcher for namespace '{}' cause '{}'", namespace, ex.getMessage());
    }
    podActionHandlers.clear();

    try {
      if (containerWatch != null) {
        containerWatch.close();
      }
    } catch (KubernetesClientException ex) {
      LOG.error(
          "Failed to stop pods containers watcher for namespace '{}' cause '{}'",
          namespace,
          ex.getMessage());
    }
    containerEventsHandlers.clear();
  }

  /**
   * Executes command in specified container.
   *
   * @param podName pod name where command will be executed
   * @param containerName container name where command will be executed
   * @param timeoutMin timeout to wait until process will be done
   * @param command command to execute
   * @param outputConsumer command output biconsumer, that is accepts stream type and message
   * @throws InfrastructureException when specified timeout is reached
   * @throws InfrastructureException when {@link Thread} is interrupted while command executing
   * @throws InfrastructureException when command error stream is not empty
   * @throws InfrastructureException when any other exception occurs
   */
  public void exec(
      String podName,
      String containerName,
      int timeoutMin,
      String[] command,
      BiConsumer<String, String> outputConsumer)
      throws InfrastructureException {
    final ExecWatchdog watchdog = new ExecWatchdog();
    final ByteArrayOutputStream errStream = new ByteArrayOutputStream(ERROR_BUFF_INITIAL_CAP);
    try (ExecWatch watch =
        clientFactory
            .create(workspaceId)
            .pods()
            .inNamespace(namespace)
            .withName(podName)
            .inContainer(containerName)
            .writingError(errStream)
            .usingListener(watchdog)
            .exec(encode(command))) {
      try {
        watchdog.wait(timeoutMin, TimeUnit.MINUTES);
        final byte[] error = errStream.toByteArray();
        if (error.length > 0) {
          final String cmd = Arrays.stream(command).collect(Collectors.joining(" ", "", "\n"));
          final String err = new String(error, UTF_8);
          outputConsumer.accept(STDOUT, cmd);
          outputConsumer.accept(STDERR, err);
          throw new InfrastructureException(err);
        }
      } catch (InterruptedException ex) {
        Thread.currentThread().interrupt();
        throw new InfrastructureException(ex.getMessage(), ex);
      }
    } catch (KubernetesClientException ex) {
      throw new KubernetesInfrastructureException(ex);
    }
  }

  /**
   * Executes command in specified container.
   *
   * @param podName pod name where command will be executed
   * @param containerName container name where command will be executed
   * @param timeoutMin timeout to wait until process will be done
   * @param command command to execute
   * @throws InfrastructureException when specified timeout is reached
   * @throws InfrastructureException when {@link Thread} is interrupted while command executing
   * @throws InfrastructureException when any other exception occurs
   */
  public void exec(String podName, String containerName, int timeoutMin, String[] command)
      throws InfrastructureException {
    final ExecWatchdog watchdog = new ExecWatchdog();
    try (ExecWatch watch =
        clientFactory
            .create(workspaceId)
            .pods()
            .inNamespace(namespace)
            .withName(podName)
            .inContainer(containerName)
            // redirecting error output to exec watch out stream
            .redirectingError()
            .usingListener(watchdog)
            .exec(encode(command))) {
      try {
        watchdog.wait(timeoutMin, TimeUnit.MINUTES);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new InfrastructureException(e.getMessage(), e);
      }
    } catch (KubernetesClientException e) {
      throw new KubernetesInfrastructureException(e);
    }
  }

  /**
   * Deletes pod with given name.
   *
   * <p>Note that this method will mark Kubernetes pod as interrupted and then will wait until pod
   * will be killed.
   *
   * @param name name of pod to remove
   * @throws InfrastructureException when {@link Thread} is interrupted while command executing
   * @throws InfrastructureException when pod removal timeout is reached
   * @throws InfrastructureException when any other exception occurs
   */
  public void delete(String name) throws InfrastructureException {
    try {
      doDelete(name).get(POD_REMOVAL_TIMEOUT_MIN, TimeUnit.MINUTES);
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
      throw new InfrastructureException(
          "Interrupted while waiting for pod removal. " + ex.getMessage());
    } catch (ExecutionException ex) {
      throw new InfrastructureException(
          "Error occurred while waiting for pod removal. " + ex.getMessage());
    } catch (TimeoutException ex) {
      throw new InfrastructureException("Pod removal timeout reached " + ex.getMessage());
    }
  }

  /**
   * Deletes all existing pods.
   *
   * <p>Note that this method will mark Kubernetes pods as interrupted and then will wait until all
   * pods will be killed.
   *
   * @throws InfrastructureException when {@link Thread} is interrupted while command executing
   * @throws InfrastructureException when pods removal timeout is reached
   * @throws InfrastructureException when any other exception occurs
   */
  public void delete() throws InfrastructureException {
    try {
      // pods are removed with some delay related to stopping of containers. It is need to wait them
      List<Pod> pods =
          clientFactory
              .create(workspaceId)
              .pods()
              .inNamespace(namespace)
              .withLabel(CHE_WORKSPACE_ID_LABEL, workspaceId)
              .list()
              .getItems();
      final List<CompletableFuture> deleteFutures = new ArrayList<>();
      for (Pod pod : pods) {
        deleteFutures.add(doDelete(pod.getMetadata().getName()));
      }
      final CompletableFuture<Void> removed =
          allOf(deleteFutures.toArray(new CompletableFuture[deleteFutures.size()]));
      try {
        removed.get(POD_REMOVAL_TIMEOUT_MIN, TimeUnit.MINUTES);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new InfrastructureException(
            "Interrupted while waiting for pod removal. " + e.getMessage());
      } catch (ExecutionException e) {
        throw new InfrastructureException(
            "Error occurred while waiting for pod removing. " + e.getMessage());
      } catch (TimeoutException ex) {
        throw new InfrastructureException("Pods removal timeout reached " + ex.getMessage());
      }
    } catch (KubernetesClientException e) {
      throw new KubernetesInfrastructureException(e);
    }
  }

  private CompletableFuture<Void> doDelete(String name) throws InfrastructureException {
    try {
      final PodResource<Pod, DoneablePod> podResource =
          clientFactory.create(workspaceId).pods().inNamespace(namespace).withName(name);
      final CompletableFuture<Void> deleteFuture = new CompletableFuture<>();
      final Watch watch = podResource.watch(new DeleteWatcher(deleteFuture));

      podResource.delete();
      return deleteFuture.whenComplete(
          (v, e) -> {
            if (e != null) {
              LOG.warn("Failed to remove pod {} cause {}", name, e.getMessage());
            }
            watch.close();
          });
    } catch (KubernetesClientException ex) {
      throw new KubernetesInfrastructureException(ex);
    }
  }

  private String[] encode(String[] toEncode) throws InfrastructureException {
    String[] encoded = new String[toEncode.length];
    for (int i = 0; i < toEncode.length; i++) {
      try {
        encoded[i] = URLEncoder.encode(toEncode[i], "UTF-8");
      } catch (UnsupportedEncodingException e) {
        throw new InfrastructureException(e.getMessage(), e);
      }
    }
    return encoded;
  }

  private static class DeleteWatcher implements Watcher<Pod> {

    private final CompletableFuture<Void> future;

    private DeleteWatcher(CompletableFuture<Void> future) {
      this.future = future;
    }

    @Override
    public void eventReceived(Action action, Pod hasMetadata) {
      if (action == Action.DELETED) {
        future.complete(null);
      }
    }

    @Override
    public void onClose(KubernetesClientException e) {
      // if event about removing is received then this completion has no effect
      future.completeExceptionally(
          new RuntimeException(
              "Webscoket connection is closed. But event about removing is not received.", e));
    }
  }

  private class ExecWatchdog implements ExecListener {

    private final CountDownLatch latch;

    private ExecWatchdog() {
      this.latch = new CountDownLatch(1);
    }

    @Override
    public void onOpen(Response response) {}

    @Override
    public void onFailure(Throwable t, Response response) {
      latch.countDown();
    }

    @Override
    public void onClose(int code, String reason) {
      latch.countDown();
    }

    public void wait(long timeout, TimeUnit timeUnit)
        throws InterruptedException, InfrastructureException {
      boolean isDone = latch.await(timeout, timeUnit);
      if (!isDone) {
        throw new InfrastructureException("Timeout reached while execution of command");
      }
    }
  }
}
