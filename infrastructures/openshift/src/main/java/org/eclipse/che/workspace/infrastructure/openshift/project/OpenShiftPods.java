/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.openshift.project;

import static java.util.concurrent.CompletableFuture.allOf;
import static org.eclipse.che.workspace.infrastructure.openshift.Constants.CHE_WORKSPACE_ID_LABEL;
import static org.eclipse.che.workspace.infrastructure.openshift.project.OpenShiftObjectUtil.putLabel;

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
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import okhttp3.Response;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.openshift.OpenShiftClientFactory;
import org.eclipse.che.workspace.infrastructure.openshift.project.event.ContainerEvent;
import org.eclipse.che.workspace.infrastructure.openshift.project.event.ContainerEventHandler;
import org.eclipse.che.workspace.infrastructure.openshift.project.event.PodActionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Defines an internal API for managing {@link Pod} instances in {@link OpenShiftPods#namespace
 * predefined namespace}.
 *
 * @author Sergii Leshchenko
 * @author Anton Korneta
 */
public class OpenShiftPods {

  private static final Logger LOG = LoggerFactory.getLogger(OpenShiftPods.class);

  private static final String CONTAINER_NAME_GROUP = "name";
  // when event is related to container `fieldPath` field contains
  // information in the following format: `spec.container{web}`, where `web` is container name
  private static final Pattern CONTAINER_FIELD_PATH_PATTERN =
      Pattern.compile("spec.containers\\{(?<" + CONTAINER_NAME_GROUP + ">.*)}");

  // TODO https://github.com/eclipse/che/issues/7656
  public static final int POD_REMOVAL_TIMEOUT_MIN = 5;

  private static final String POD_OBJECT_KIND = "Pod";

  private final String namespace;
  private final OpenShiftClientFactory clientFactory;
  private final ConcurrentLinkedQueue<PodActionHandler> podActionHandlers;
  private final ConcurrentLinkedQueue<ContainerEventHandler> containerEventsHandlers;
  private final String workspaceId;
  private Watch podWatch;
  private Watch containerWatch;

  OpenShiftPods(String namespace, String workspaceId, OpenShiftClientFactory clientFactory) {
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
      return clientFactory.create().pods().inNamespace(namespace).create(pod);
    } catch (KubernetesClientException e) {
      throw new InfrastructureException(e.getMessage(), e);
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
          .create()
          .pods()
          .inNamespace(namespace)
          .withLabel(CHE_WORKSPACE_ID_LABEL, workspaceId)
          .list()
          .getItems();
    } catch (KubernetesClientException e) {
      throw new InfrastructureException(e.getMessage(), e);
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
          clientFactory.create().pods().inNamespace(namespace).withName(name).get());
    } catch (KubernetesClientException e) {
      throw new InfrastructureException(e.getMessage(), e);
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
          clientFactory.create().pods().inNamespace(namespace).withName(name);

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
      throw new InfrastructureException(e.getMessage(), e);
    } finally {
      if (watch != null) {
        watch.close();
      }
    }
  }

  /**
   * Starts watching the pods inside OpenShift namespace and registers a specified handler for such
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
                .create()
                .pods()
                .inNamespace(namespace)
                .withLabel(CHE_WORKSPACE_ID_LABEL, workspaceId)
                .watch(watcher);
      } catch (KubernetesClientException ex) {
        throw new InfrastructureException(ex.getMessage());
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
                          event.getMessage(),
                          event.getMetadata().getCreationTimestamp());
                  containerEventsHandlers.forEach(h -> h.handle(containerEvent));
                }
              }
            }

            @Override
            public void onClose(KubernetesClientException ignored) {}
          };
      try {
        containerWatch = clientFactory.create().events().inNamespace(namespace).watch(watcher);
      } catch (KubernetesClientException ex) {
        throw new InfrastructureException(ex.getMessage());
      }
    }
    containerEventsHandlers.add(handler);
  }

  /** Stops watching the pods inside OpenShift namespace. */
  void stopWatch() {
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
   * @throws InfrastructureException when specified timeout is reached
   * @throws InfrastructureException when {@link Thread} is interrupted while command executing
   * @throws InfrastructureException when any other exception occurs
   */
  public void exec(String podName, String containerName, int timeoutMin, String[] command)
      throws InfrastructureException {
    final ExecWatchdog watchdog = new ExecWatchdog();
    try (ExecWatch watch =
        clientFactory
            .create()
            .pods()
            .inNamespace(namespace)
            .withName(podName)
            .inContainer(containerName)
            .usingListener(watchdog)
            .exec(encode(command))) {
      try {
        watchdog.wait(timeoutMin, TimeUnit.MINUTES);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new InfrastructureException(e.getMessage(), e);
      }
    } catch (KubernetesClientException e) {
      throw new InfrastructureException(e.getMessage());
    }
  }

  /**
   * Deletes pod with given name.
   *
   * <p>Note that this method will mark OpenShift pod as interrupted and then will wait until pod
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
   * <p>Note that this method will mark OpenShift pods as interrupted and then will wait until all
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
              .create()
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
      throw new InfrastructureException(e.getMessage(), e);
    }
  }

  private CompletableFuture<Void> doDelete(String name) throws InfrastructureException {
    try {
      final PodResource<Pod, DoneablePod> podResource =
          clientFactory.create().pods().inNamespace(namespace).withName(name);
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
      throw new InfrastructureException(ex.getMessage(), ex);
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
