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
package org.eclipse.che.workspace.infrastructure.kubernetes.namespace;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.concurrent.CompletableFuture.allOf;
import static org.eclipse.che.workspace.infrastructure.kubernetes.Constants.CHE_DEPLOYMENT_NAME_LABEL;
import static org.eclipse.che.workspace.infrastructure.kubernetes.Constants.CHE_WORKSPACE_ID_LABEL;
import static org.eclipse.che.workspace.infrastructure.kubernetes.Constants.POD_STATUS_PHASE_FAILED;
import static org.eclipse.che.workspace.infrastructure.kubernetes.Constants.POD_STATUS_PHASE_RUNNING;
import static org.eclipse.che.workspace.infrastructure.kubernetes.Constants.POD_STATUS_PHASE_SUCCEEDED;
import static org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesObjectUtil.putLabel;

import com.google.common.base.Strings;
import io.fabric8.kubernetes.api.model.DoneablePod;
import io.fabric8.kubernetes.api.model.Event;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.ObjectReference;
import io.fabric8.kubernetes.api.model.OwnerReference;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodSpec;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DoneableDeployment;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.dsl.ExecListener;
import io.fabric8.kubernetes.client.dsl.ExecWatch;
import io.fabric8.kubernetes.client.dsl.PodResource;
import io.fabric8.kubernetes.client.dsl.ScalableResource;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
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
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.event.PodActionHandler;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.event.PodEvent;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.event.PodEventHandler;
import org.eclipse.che.workspace.infrastructure.kubernetes.util.PodEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Defines an internal API for managing {@link Pod} and {@link Deployment} instances in {@link
 * KubernetesDeployments#namespace predefined namespace}.
 *
 * @author Sergii Leshchenko
 * @author Anton Korneta
 * @author Angel Misevski
 */
public class KubernetesDeployments {

  private static final Logger LOG = LoggerFactory.getLogger(KubernetesDeployments.class);

  private static final String CONTAINER_NAME_GROUP = "name";
  // when event is related to container `fieldPath` field contains
  // information in the following format: `spec.container{web}`, where `web` is container name
  private static final Pattern CONTAINER_FIELD_PATH_PATTERN =
      Pattern.compile("spec.containers\\{(?<" + CONTAINER_NAME_GROUP + ">.*)}");

  // TODO https://github.com/eclipse/che/issues/7656
  public static final int POD_REMOVAL_TIMEOUT_MIN = 5;
  public static final int POD_CREATION_TIMEOUT_MIN = 1;

  private static final String POD_OBJECT_KIND = "Pod";
  // error stream data initial capacity
  public static final int ERROR_BUFF_INITIAL_CAP = 2048;
  public static final String STDOUT = "stdout";
  public static final String STDERR = "stderr";

  protected final String namespace;
  protected final String workspaceId;
  private final KubernetesClientFactory clientFactory;
  private final ConcurrentLinkedQueue<PodActionHandler> podActionHandlers;
  private final ConcurrentLinkedQueue<PodEventHandler> containerEventsHandlers;
  private Watch podWatch;
  private Watch containerWatch;
  private Date watcherInitializationDate;

  protected KubernetesDeployments(
      String namespace, String workspaceId, KubernetesClientFactory clientFactory) {
    this.namespace = namespace;
    this.workspaceId = workspaceId;
    this.clientFactory = clientFactory;
    this.containerEventsHandlers = new ConcurrentLinkedQueue<>();
    this.podActionHandlers = new ConcurrentLinkedQueue<>();
  }

  /**
   * Starts the specified Pod via a Deployment.
   *
   * @param pod pod to deploy
   * @return created pod
   * @throws InfrastructureException when any exception occurs
   */
  public Pod deploy(Pod pod) throws InfrastructureException {
    putLabel(pod, CHE_WORKSPACE_ID_LABEL, workspaceId);

    ObjectMeta metadata = pod.getMetadata();
    // Note: metadata.name will be changed as for pods it is set by the deployment.
    String originalName = metadata.getName();
    putLabel(pod, CHE_DEPLOYMENT_NAME_LABEL, originalName);

    PodSpec podSpec = pod.getSpec();
    podSpec.setRestartPolicy("Always"); // Only allowable value
    final CompletableFuture<Pod> createFuture = new CompletableFuture<>();
    final Watch createWatch =
        clientFactory
            .create(workspaceId)
            .pods()
            .inNamespace(namespace)
            .watch(new CreateWatcher(createFuture, workspaceId, originalName));
    try {
      clientFactory
          .create(workspaceId)
          .apps()
          .deployments()
          .inNamespace(namespace)
          .createNew()
          .withMetadata(metadata)
          .withNewSpec()
          .withNewSelector()
          .withMatchLabels(metadata.getLabels())
          .endSelector()
          .withReplicas(1)
          .withNewTemplate()
          .withMetadata(metadata)
          .withSpec(podSpec)
          .endTemplate()
          .endSpec()
          .done();
      return createFuture.get(POD_CREATION_TIMEOUT_MIN, TimeUnit.MINUTES);
    } catch (KubernetesClientException e) {
      throw new KubernetesInfrastructureException(e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new InfrastructureException(
          String.format(
              "Interrupted while waiting for Pod creation. -id: %s -message: %s",
              metadata.getName(), e.getMessage()));
    } catch (ExecutionException e) {
      throw new InfrastructureException(
          String.format(
              "Error occured while waiting for Pod creation. -id: %s -message: %s",
              metadata.getName(), e.getCause().getMessage()));
    } catch (TimeoutException e) {
      throw new InfrastructureException(
          String.format(
              "Pod creation timeout exceeded. -id: %s -message: %s",
              metadata.getName(), e.getMessage()));
    } finally {
      createWatch.close();
    }
  }

  /**
   * Create a terminating pod that is not part of a Deployment.
   *
   * @param pod the Pod to create
   * @return the created pod
   * @throws InfrastructureException when any error occurs
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
   * Returns optional with pod that either has specified name or is controlled by Deployment with
   * specified name.
   *
   * @param name name of the Pod or Deployment
   * @throws InfrastructureException when any exception occurs
   */
  public Optional<Pod> get(String name) throws InfrastructureException {
    String podName = getPodName(name);
    try {
      return Optional.ofNullable(
          clientFactory.create(workspaceId).pods().inNamespace(namespace).withName(podName).get());
    } catch (KubernetesClientException e) {
      throw new KubernetesInfrastructureException(e);
    }
  }

  /**
   * Waits until pod state will suit for specified predicate.
   *
   * @param name name of pod or deployment containing pod that should be watched
   * @param timeoutMin waiting timeout in minutes
   * @param predicate predicate to perform state check
   * @return pod that satisfies the specified predicate
   * @throws InfrastructureException when specified timeout is reached
   * @throws InfrastructureException when {@link Thread} is interrupted while waiting
   * @throws InfrastructureException when any other exception occurs
   */
  public Pod wait(String name, int timeoutMin, Predicate<Pod> predicate)
      throws InfrastructureException {
    String podName = getPodName(name);
    CompletableFuture<Pod> future = new CompletableFuture<>();
    Watch watch = null;
    try {

      PodResource<Pod, DoneablePod> podResource =
          clientFactory.create(workspaceId).pods().inNamespace(namespace).withName(podName);

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
                          "Waiting for pod '" + podName + "' was interrupted"));
                }
              });

      Pod actualPod = podResource.get();
      if (actualPod == null) {
        if (name.equals(podName)) { // `name` refers to a bare pod
          throw new InfrastructureException("Specified pod " + podName + " doesn't exist");
        } else { // `name` refers to a deployment
          throw new InfrastructureException("No pod in deployment " + name + " found.");
        }
      }
      if (predicate.test(actualPod)) {
        return actualPod;
      }
      try {
        return future.get(timeoutMin, TimeUnit.MINUTES);
      } catch (ExecutionException e) {
        throw new InfrastructureException(e.getCause().getMessage(), e);
      } catch (TimeoutException e) {
        throw new InfrastructureException("Waiting for pod '" + podName + "' reached timeout");
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new InfrastructureException("Waiting for pod '" + podName + "' was interrupted");
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
   * Subscribes to pod events and returns the resulting future, which completes when pod becomes
   * running.
   *
   * <p>Note that the resulting future must be explicitly cancelled when its completion no longer
   * important because of finalization allocated resources.
   *
   * @param name the pod or deployment (that contains pod) name that should be watched
   * @return completable future that is completed when one of the following conditions is met:
   *     <ul>
   *       <li>complete successfully in case of "Running" pod state.
   *       <li>complete exceptionally in case of "Failed" pod state. Exception will contain pod
   *           status reason value, or if absent, it will attempt to retrieve pod logs.
   *       <li>complete exceptionally in case of "Succeeded" pod state. (workspace container has
   *           been terminated).
   *       <li>complete exceptionally when exception while getting pod resource occurred.
   *       <li>complete exceptionally when connection problem occurred.
   *     </ul>
   *     otherwise, it must be explicitly closed
   */
  public CompletableFuture<Void> waitRunningAsync(String name) {
    final CompletableFuture<Void> podRunningFuture = new CompletableFuture<>();
    try {
      final String podName = getPodName(name);
      final PodResource<Pod, DoneablePod> podResource =
          clientFactory.create().pods().inNamespace(namespace).withName(podName);
      final Watch watch =
          podResource.watch(
              new Watcher<Pod>() {
                @Override
                public void eventReceived(Action action, Pod pod) {
                  handleStartingPodStatus(podRunningFuture, pod);
                }

                @Override
                public void onClose(KubernetesClientException cause) {
                  podRunningFuture.completeExceptionally(
                      new InfrastructureException(
                          "Waiting for pod '" + podName + "' was interrupted"));
                }
              });

      podRunningFuture.whenComplete((ok, ex) -> watch.close());
      final Pod pod = podResource.get();
      if (pod == null) {
        InfrastructureException ex;
        if (name.equals(podName)) { // `name` refers to bare pod
          ex = new InfrastructureException("Specified pod " + podName + " doesn't exist");
        } else {
          ex = new InfrastructureException("No pod in deployment " + name + " found.");
        }
        podRunningFuture.completeExceptionally(ex);
      } else {
        handleStartingPodStatus(podRunningFuture, pod);
      }
    } catch (KubernetesClientException | InfrastructureException ex) {
      podRunningFuture.completeExceptionally(ex);
    }
    return podRunningFuture;
  }

  private void handleStartingPodStatus(CompletableFuture<Void> podRunningFuture, Pod pod) {
    if (POD_STATUS_PHASE_RUNNING.equals(pod.getStatus().getPhase())) {
      podRunningFuture.complete(null);
      return;
    }

    if (POD_STATUS_PHASE_SUCCEEDED.equals(pod.getStatus().getPhase())) {
      podRunningFuture.completeExceptionally(
          new InfrastructureException(
              "Pod container has been terminated. Container must be configured to use a non-terminating command."));
      return;
    }

    if (POD_STATUS_PHASE_FAILED.equals(pod.getStatus().getPhase())) {
      String exceptionMessage = "Pod '" + pod.getMetadata().getName() + "' failed to start.";
      String reason = pod.getStatus().getReason();
      if (Strings.isNullOrEmpty(reason)) {
        try {
          String podLog =
              clientFactory
                  .create()
                  .pods()
                  .inNamespace(namespace)
                  .withName(pod.getMetadata().getName())
                  .getLog();
          exceptionMessage = exceptionMessage.concat(" Pod logs: ").concat(podLog);

        } catch (InfrastructureException e) {
          exceptionMessage = exceptionMessage.concat(" Error occurred while fetching pod logs.");
        }
      } else {
        exceptionMessage = exceptionMessage.concat(" Reason: ").concat(reason);
      }
      podRunningFuture.completeExceptionally(new InfrastructureException(exceptionMessage));
      LOG.warn(exceptionMessage);
    }
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
  public void watchEvents(PodEventHandler handler) throws InfrastructureException {
    if (containerWatch == null) {
      final Watcher<Event> watcher =
          new Watcher<Event>() {
            @Override
            public void eventReceived(Action action, Event event) {
              ObjectReference involvedObject = event.getInvolvedObject();

              if (POD_OBJECT_KIND.equals(involvedObject.getKind())) {

                String podName = involvedObject.getName();

                PodEvent podEvent =
                    new PodEvent(
                        podName,
                        getContainerName(involvedObject.getFieldPath()),
                        event.getReason(),
                        event.getMessage(),
                        event.getMetadata().getCreationTimestamp(),
                        event.getLastTimestamp());

                try {
                  if (happenedAfterWatcherInitialization(podEvent)) {
                    containerEventsHandlers.forEach(h -> h.handle(podEvent));
                  }
                } catch (ParseException | IllegalArgumentException e) {
                  LOG.error(
                      "Failed to parse last timestamp of the event. Cause: {}. Event: {}",
                      e.getMessage(),
                      podEvent);
                }
              }
            }

            @Override
            public void onClose(KubernetesClientException ignored) {}

            /**
             * Returns the container name if the event is related to container. When the event is
             * related to container `fieldPath` field contain information in the following format:
             * `spec.container{web}`, where `web` is container name
             */
            private String getContainerName(String fieldPath) {
              String containerName = null;
              if (fieldPath != null) {
                Matcher containerFieldMatcher = CONTAINER_FIELD_PATH_PATTERN.matcher(fieldPath);
                if (containerFieldMatcher.matches()) {
                  containerName = containerFieldMatcher.group(CONTAINER_NAME_GROUP);
                }
              }
              return containerName;
            }

            /**
             * Returns true if 'lastTimestamp' of the event is *after* the time of the watcher
             * initialization
             */
            private boolean happenedAfterWatcherInitialization(PodEvent event)
                throws ParseException {
              String eventLastTimestamp = event.getLastTimestamp();
              Date eventLastTimestampDate =
                  PodEvents.convertEventTimestampToDate(eventLastTimestamp);
              return eventLastTimestampDate.after(watcherInitializationDate);
            }
          };
      try {
        watcherInitializationDate = new Date();
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
   * @param name pod name (or name of deployment containing pod) where command will be executed
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
      String name,
      String containerName,
      int timeoutMin,
      String[] command,
      BiConsumer<String, String> outputConsumer)
      throws InfrastructureException {
    final String podName = getPodName(name);
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
   * @param name pod name (or name of deployment containing pod) where command will be executed
   * @param containerName container name where command will be executed
   * @param timeoutMin timeout to wait until process will be done
   * @param command command to execute
   * @throws InfrastructureException when specified timeout is reached
   * @throws InfrastructureException when {@link Thread} is interrupted while command executing
   * @throws InfrastructureException when any other exception occurs
   */
  public void exec(String name, String containerName, int timeoutMin, String[] command)
      throws InfrastructureException {
    final String podName = getPodName(name);
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
   * Deletes pod or deployment with given name. If a Pod controlled by a Deployment is specified,
   * the owning Deployment will be deleted instead.
   *
   * <p>Note that this method will mark Kubernetes pod as interrupted and then will wait until pod
   * will be killed.
   *
   * @param name name of pod or deployment to remove
   * @throws InfrastructureException when {@link Thread} is interrupted while command executing
   * @throws InfrastructureException when pod removal timeout is reached
   * @throws InfrastructureException when any other exception occurs
   */
  public void delete(String name) throws InfrastructureException {
    try {
      Pod pod =
          clientFactory.create(workspaceId).pods().inNamespace(namespace).withName(name).get();
      if (pod != null) {
        doDeletePod(name).get(POD_REMOVAL_TIMEOUT_MIN, TimeUnit.MINUTES);
      } else {
        doDeleteDeployment(name).get(POD_REMOVAL_TIMEOUT_MIN, TimeUnit.MINUTES);
      }
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
   * Deletes all existing pods and the Deployments that control them.
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
      final List<CompletableFuture<Void>> deleteFutures = new ArrayList<>();
      // We first delete all deployments, then clean up any bare Pods.
      List<Deployment> deployments =
          clientFactory
              .create(workspaceId)
              .apps()
              .deployments()
              .inNamespace(namespace)
              .withLabel(CHE_WORKSPACE_ID_LABEL, workspaceId)
              .list()
              .getItems();
      for (Deployment deployment : deployments) {
        deleteFutures.add(doDeleteDeployment(deployment.getMetadata().getName()));
      }
      // We have to be careful to not include pods that are controlled by a deployment
      List<Pod> pods =
          clientFactory
              .create(workspaceId)
              .pods()
              .inNamespace(namespace)
              .withLabel(CHE_WORKSPACE_ID_LABEL, workspaceId)
              .withoutLabel(CHE_DEPLOYMENT_NAME_LABEL)
              .list()
              .getItems();
      for (Pod pod : pods) {
        List<OwnerReference> ownerReferences = pod.getMetadata().getOwnerReferences();
        if (ownerReferences == null || ownerReferences.isEmpty()) {
          deleteFutures.add(doDeletePod(pod.getMetadata().getName()));
        }
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

  protected CompletableFuture<Void> doDeleteDeployment(String deploymentName)
      throws InfrastructureException {
    // Try to get pod name if it exists (it may not, if e.g. workspace config refers to
    // nonexistent service account).
    String podName;
    try {
      podName = getPodName(deploymentName);
    } catch (InfrastructureException e) {
      // Not an error, just means the Deployment has failed to create a pod.
      podName = null;
    }

    Watch toCloseOnException = null;
    try {
      ScalableResource<Deployment, DoneableDeployment> deploymentResource =
          clientFactory
              .create(workspaceId)
              .apps()
              .deployments()
              .inNamespace(namespace)
              .withName(deploymentName);
      if (deploymentResource.get() == null) {
        throw new InfrastructureException(
            String.format("No deployment foud to delete for name %s", deploymentName));
      }

      final CompletableFuture<Void> deleteFuture = new CompletableFuture<>();
      final Watch watch;
      // If we have a Pod, we have to watch to make sure it is deleted, otherwise, we watch the
      // Deployment we are deleting.
      if (!Strings.isNullOrEmpty(podName)) {
        PodResource<Pod, DoneablePod> podResource =
            clientFactory.create(workspaceId).pods().inNamespace(namespace).withName(podName);
        watch = podResource.watch(new DeleteWatcher<Pod>(deleteFuture));
        toCloseOnException = watch;
      } else {
        watch = deploymentResource.watch(new DeleteWatcher<Deployment>(deleteFuture));
        toCloseOnException = watch;
      }

      Boolean deleteSucceeded = deploymentResource.delete();

      if (deleteSucceeded == null || !deleteSucceeded) {
        deleteFuture.complete(null);
      }
      return deleteFuture.whenComplete(
          (v, e) -> {
            if (e != null) {
              LOG.warn("Failed to remove deployment {} cause {}", deploymentName, e.getMessage());
            }
            watch.close();
          });
    } catch (KubernetesClientException e) {
      if (toCloseOnException != null) {
        toCloseOnException.close();
      }
      throw new KubernetesInfrastructureException(e);
    } catch (Exception e) {
      if (toCloseOnException != null) {
        toCloseOnException.close();
      }
      throw e;
    }
  }

  protected CompletableFuture<Void> doDeletePod(String podName) throws InfrastructureException {
    Watch toCloseOnException = null;
    try {
      PodResource<Pod, DoneablePod> podResource =
          clientFactory.create(workspaceId).pods().inNamespace(namespace).withName(podName);
      if (podResource.get() == null) {
        throw new InfrastructureException(
            String.format("No pod found to delete for name %s", podName));
      }

      final CompletableFuture<Void> deleteFuture = new CompletableFuture<>();
      final Watch watch = podResource.watch(new DeleteWatcher<Pod>(deleteFuture));
      toCloseOnException = watch;

      Boolean deleteSucceeded = podResource.delete();
      if (deleteSucceeded == null || !deleteSucceeded) {
        deleteFuture.complete(null);
      }
      return deleteFuture.whenComplete(
          (v, e) -> {
            if (e != null) {
              LOG.warn("Failed to remove pod {} cause {}", podName, e.getMessage());
            }
            watch.close();
          });
    } catch (KubernetesClientException e) {
      if (toCloseOnException != null) {
        toCloseOnException.close();
      }
      throw new KubernetesInfrastructureException(e);
    } catch (Exception e) {
      if (toCloseOnException != null) {
        toCloseOnException.close();
      }
      throw e;
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

  /**
   * Returns the name of a specified Pod given either the actual Pod name or the name of the
   * DeploymentConfig that controls it. <br>
   * This is necessary because we are trying to transparently wrap Pods in DeploymentConfigs;
   * attempting to create a Pod named {@code testPod} will result in a DeploymentConfig with name
   * {@code testPod}, which will in turn create a Pod named e.g {@code testPod-1-xxxxx}.
   *
   * @param name Pod or DeploymentConfig name
   * @return the name of the intended pod.
   * @see
   */
  private String getPodName(String name) throws InfrastructureException {
    if (clientFactory.create(workspaceId).pods().inNamespace(namespace).withName(name).get()
        != null) {
      return name;
    }
    Deployment deployment =
        clientFactory
            .create(workspaceId)
            .apps()
            .deployments()
            .inNamespace(namespace)
            .withName(name)
            .get();
    if (deployment == null) {
      throw new InfrastructureException("Failed to get deployment for pod");
    }
    Map<String, String> selector = deployment.getSpec().getSelector().getMatchLabels();
    List<Pod> pods =
        clientFactory
            .create(workspaceId)
            .pods()
            .inNamespace(namespace)
            .withLabels(selector)
            .list()
            .getItems();
    if (pods.isEmpty()) {
      throw new InfrastructureException(String.format("Failed to find pod with name %s", name));
    } else if (pods.size() > 1) {
      throw new InfrastructureException(
          String.format("Found multiple pods in DeploymentConfig %s", name));
    }

    return pods.get(0).getMetadata().getName();
  }

  private static class CreateWatcher implements Watcher<Pod> {

    private final CompletableFuture<Pod> future;
    private final String workspaceId;
    private final String originalName;

    private CreateWatcher(CompletableFuture<Pod> future, String workspaceId, String originalName) {
      this.future = future;
      this.workspaceId = workspaceId;
      this.originalName = originalName;
    }

    @Override
    public void eventReceived(Action action, Pod resource) {
      Map<String, String> labels = resource.getMetadata().getLabels();
      if (workspaceId.equals(labels.get(CHE_WORKSPACE_ID_LABEL))
          && originalName.equals(labels.get(CHE_DEPLOYMENT_NAME_LABEL))) {
        future.complete(resource);
      }
    }

    @Override
    public void onClose(KubernetesClientException cause) {
      future.completeExceptionally(
          new RuntimeException("Websocket connection closed before Pod creation event received"));
    }
  }

  private static class DeleteWatcher<T> implements Watcher<T> {

    private final CompletableFuture<Void> future;

    private DeleteWatcher(CompletableFuture<Void> future) {
      this.future = future;
    }

    @Override
    public void eventReceived(Action action, T hasMetadata) {
      if (action == Action.DELETED) {
        future.complete(null);
      }
    }

    @Override
    public void onClose(KubernetesClientException e) {
      // if event about removing is received then this completion has no effect
      future.completeExceptionally(
          new RuntimeException(
              "Websocket connection is closed. But event about removing is not received.", e));
    }
  }

  private class ExecWatchdog implements ExecListener {

    private final CompletableFuture<Void> completionFuture;

    private ExecWatchdog() {
      this.completionFuture = new CompletableFuture<>();
    }

    @Override
    public void onOpen(Response response) {}

    @Override
    public void onFailure(Throwable t, Response response) {
      completionFuture.completeExceptionally(t);
    }

    @Override
    public void onClose(int code, String reason) {
      completionFuture.complete(null);
    }

    public void wait(long timeout, TimeUnit timeUnit)
        throws InterruptedException, InfrastructureException {
      try {
        completionFuture.get(timeout, timeUnit);
      } catch (ExecutionException e) {
        throw new InfrastructureException(
            "Error occured while executing command in pod: " + e.getMessage(), e);
      } catch (TimeoutException e) {
        throw new InfrastructureException("Timeout reached while execution of command");
      }
    }
  }
}
