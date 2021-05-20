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
package org.eclipse.che.workspace.infrastructure.kubernetes.namespace.pvc;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesObjectUtil.newVolume;
import static org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesObjectUtil.newVolumeMount;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.api.model.PodStatus;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.commons.lang.concurrent.LoggingUncaughtExceptionHandler;
import org.eclipse.che.commons.lang.concurrent.ThreadLocalPropagateContext;
import org.eclipse.che.commons.observability.ExecutorServiceWrapper;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesDeployments;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesNamespaceFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.log.LogWatchTimeouts;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.log.LogWatcher;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.log.PodLogToEventPublisher;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.SecurityContextProvisioner;
import org.eclipse.che.workspace.infrastructure.kubernetes.util.Containers;
import org.eclipse.che.workspace.infrastructure.kubernetes.util.RuntimeEventsPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helps to execute commands needed for workspace PVC preparation and cleanup.
 *
 * <p>Creates a short-lived Pod based on CentOS image which mounts a specified PVC and executes a
 * command (either {@code mkdir -p <path>} or {@code rm -rf <path>}). Reports back whether the pod
 * succeeded or failed. Supports multiple paths for one command.
 *
 * <p>Note that the commands execution is needed only for {@link CommonPVCStrategy}.
 *
 * @author amisevsk
 * @author Anton Korneta
 */
@Singleton
public class PVCSubPathHelper {

  private static final Logger LOG = LoggerFactory.getLogger(PVCSubPathHelper.class);
  private static final JobFinishedPredicate POD_PREDICATE = new JobFinishedPredicate();

  static final int COUNT_THREADS = 4;
  static final int WAIT_POD_TIMEOUT_MIN = 5;

  static final String[] RM_COMMAND_BASE = new String[] {"rm", "-rf"};
  static final String[] MKDIR_COMMAND_BASE = new String[] {"mkdir", "-m", "777", "-p"};

  static final String POD_RESTART_POLICY = "Never";
  static final String POD_PHASE_SUCCEEDED = "Succeeded";
  static final String POD_PHASE_FAILED = "Failed";
  static final String JOB_MOUNT_PATH = "/tmp/job_mount";

  private final String jobImage;
  private final String jobMemoryLimit;
  private final String imagePullPolicy;
  private final KubernetesNamespaceFactory factory;
  private final ExecutorService executor;
  private final RuntimeEventsPublisher eventsPublisher;

  private final SecurityContextProvisioner securityContextProvisioner;

  @Inject
  PVCSubPathHelper(
      @Named("che.infra.kubernetes.pvc.jobs.memorylimit") String jobMemoryLimit,
      @Named("che.infra.kubernetes.pvc.jobs.image") String jobImage,
      @Named("che.infra.kubernetes.pvc.jobs.image.pull_policy") String imagePullPolicy,
      KubernetesNamespaceFactory factory,
      SecurityContextProvisioner securityContextProvisioner,
      ExecutorServiceWrapper executorServiceWrapper,
      RuntimeEventsPublisher eventPublisher) {
    this.jobMemoryLimit = jobMemoryLimit;
    this.jobImage = jobImage;
    this.imagePullPolicy = imagePullPolicy;
    this.factory = factory;
    this.securityContextProvisioner = securityContextProvisioner;
    this.eventsPublisher = eventPublisher;
    this.executor =
        executorServiceWrapper.wrap(
            Executors.newFixedThreadPool(
                COUNT_THREADS,
                new ThreadFactoryBuilder()
                    .setNameFormat("PVCSubPathHelper-ThreadPool-%d")
                    .setUncaughtExceptionHandler(LoggingUncaughtExceptionHandler.getInstance())
                    .setDaemon(false)
                    .build()),
            PVCSubPathHelper.class.getName());
  }

  /**
   * Performs create workspace directories job by given paths and waits until it finished.
   *
   * @param workspaceId workspace identifier
   * @param dirs workspace directories to create
   */
  void createDirs(
      RuntimeIdentity identity,
      String workspaceId,
      String pvcName,
      Map<String, String> startOptions,
      String... dirs) {
    LOG.debug(
        "Preparing PVC `{}` for workspace `{}`. Directories to create: {}",
        pvcName,
        workspaceId,
        Arrays.toString(dirs));
    execute(identity, workspaceId, pvcName, MKDIR_COMMAND_BASE, startOptions, dirs);
  }

  /**
   * Asynchronously starts a job for removing workspace directories by given paths.
   *
   * @param workspaceId workspace identifier
   * @param namespace
   * @param dirs workspace directories to remove
   */
  CompletableFuture<Void> removeDirsAsync(
      String workspaceId, String namespace, String pvcName, String... dirs) {
    LOG.debug(
        "Removing files in PVC `{}` of workspace `{}`. Directories to remove: {}",
        pvcName,
        workspaceId,
        Arrays.toString(dirs));
    return CompletableFuture.runAsync(
        ThreadLocalPropagateContext.wrap(
            () -> execute(workspaceId, namespace, pvcName, RM_COMMAND_BASE, dirs)),
        executor);
  }

  @VisibleForTesting
  void execute(
      RuntimeIdentity identity,
      String workspaceId,
      String pvcName,
      String[] commandBase,
      Map<String, String> startOptions,
      String... arguments) {
    execute(
        identity,
        workspaceId,
        identity.getInfrastructureNamespace(),
        pvcName,
        commandBase,
        startOptions,
        arguments);
  }

  /**
   * Executes the job with the specified arguments.
   *
   * @param namespace
   * @param commandBase the command base to execute
   * @param arguments the list of arguments for the specified job
   */
  @VisibleForTesting
  void execute(
      String workspaceId,
      String namespace,
      String pvcName,
      String[] commandBase,
      String... arguments) {
    execute(null, workspaceId, namespace, pvcName, commandBase, Collections.emptyMap(), arguments);
  }

  private void execute(
      RuntimeIdentity identity,
      String workspaceId,
      String namespace,
      String pvcName,
      String[] commandBase,
      Map<String, String> startOptions,
      String... arguments) {
    final String jobName = commandBase[0];
    final String podName = jobName + '-' + workspaceId;
    final String[] command = buildCommand(commandBase, arguments);
    final Pod pod = newPod(podName, pvcName, command);
    securityContextProvisioner.provision(pod.getSpec());

    KubernetesDeployments deployments = null;
    try {
      deployments = factory.access(workspaceId, namespace).deployments();
      deployments.create(pod);
      watchLogsIfDebugEnabled(deployments, pod, identity, startOptions);
      final Pod finished = deployments.wait(podName, WAIT_POD_TIMEOUT_MIN, POD_PREDICATE::apply);
      PodStatus finishedStatus = finished.getStatus();
      if (POD_PHASE_FAILED.equals(finishedStatus.getPhase())) {
        String logs = deployments.getPodLogs(podName);
        LOG.error(
            "Job command '{}' execution is failed. Logs: {}",
            Arrays.toString(command),
            Strings.nullToEmpty(logs).replace("\n", " \\n")); // Force logs onto one line
      }
    } catch (InfrastructureException ex) {
      LOG.error(
          "Unable to perform '{}' command for the workspace '{}' cause: '{}'",
          Arrays.toString(command),
          workspaceId,
          ex.getMessage());
      deployments.stopWatch(true);
    } finally {
      if (deployments != null) {
        deployments.stopWatch();
        try {
          deployments.delete(podName);
        } catch (InfrastructureException ignored) {
        }
      }
    }
  }

  private void watchLogsIfDebugEnabled(
      KubernetesDeployments deployment,
      Pod pod,
      RuntimeIdentity identity,
      Map<String, String> startOptions)
      throws InfrastructureException {
    if (LogWatcher.shouldWatchLogs(startOptions)) {
      deployment.watchLogs(
          new PodLogToEventPublisher(eventsPublisher, identity),
          eventsPublisher,
          LogWatchTimeouts.AGGRESSIVE,
          Collections.singleton(pod.getMetadata().getName()),
          LogWatcher.getLogLimitBytes(startOptions));
    }
  }

  /**
   * Builds the command by given base and paths.
   *
   * <p>Command is consists of base(e.g. rm -rf) and list of directories which are modified with
   * mount path.
   *
   * @param base command base
   * @param dirs the paths which are used as arguments for the command base
   * @return complete command with given arguments
   */
  @VisibleForTesting
  String[] buildCommand(String[] base, String... dirs) {
    return Stream.concat(
            Arrays.stream(base),
            Arrays.stream(dirs)
                .map(dir -> JOB_MOUNT_PATH + (dir.startsWith("/") ? dir : '/' + dir)))
        .toArray(String[]::new);
  }

  @PreDestroy
  void shutdown() {
    if (!executor.isShutdown()) {
      executor.shutdown();
      try {
        if (!executor.awaitTermination(30, SECONDS)) {
          executor.shutdownNow();
          if (!executor.awaitTermination(60, SECONDS))
            LOG.error("Couldn't shutdown PVCSubPathHelper thread pool");
        }
      } catch (InterruptedException ignored) {
        executor.shutdownNow();
        Thread.currentThread().interrupt();
      }
      LOG.info("PVCSubPathHelper thread pool is terminated");
    }
  }

  /** Returns new instance of {@link Pod} with given name and command. */
  private Pod newPod(String podName, String pvcName, String[] command) {
    final Container container =
        new ContainerBuilder()
            .withName(podName)
            .withImage(jobImage)
            .withImagePullPolicy(imagePullPolicy)
            .withCommand(command)
            .withVolumeMounts(newVolumeMount(pvcName, JOB_MOUNT_PATH, null))
            .withNewResources()
            .endResources()
            .build();
    Containers.addRamLimit(container, jobMemoryLimit);
    Containers.addRamRequest(container, jobMemoryLimit);
    return new PodBuilder()
        .withNewMetadata()
        .withName(podName)
        .endMetadata()
        .withNewSpec()
        .withContainers(container)
        .withVolumes(newVolume(pvcName, pvcName))
        .withRestartPolicy(POD_RESTART_POLICY)
        .endSpec()
        .build();
  }

  /** Checks whether pod is Failed or Successfully finished command execution */
  static class JobFinishedPredicate implements Predicate<Pod> {
    @Override
    public boolean apply(Pod pod) {
      if (pod.getStatus() == null) {
        return false;
      }
      switch (pod.getStatus().getPhase()) {
        case POD_PHASE_FAILED:
          // fall through
        case POD_PHASE_SUCCEEDED:
          // job is finished.
          return true;
        default:
          // job is not finished.
          return false;
      }
    }
  }
}
