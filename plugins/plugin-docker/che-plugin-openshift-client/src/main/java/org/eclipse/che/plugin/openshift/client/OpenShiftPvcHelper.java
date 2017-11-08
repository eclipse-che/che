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
package org.eclipse.che.plugin.openshift.client;

import static org.eclipse.che.plugin.openshift.client.OpenShiftConnector.WORKSPACE_LOGS_FOLDER_SUFFIX;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaimVolumeSource;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaimVolumeSourceBuilder;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.api.model.Volume;
import io.fabric8.kubernetes.api.model.VolumeBuilder;
import io.fabric8.kubernetes.api.model.VolumeMount;
import io.fabric8.kubernetes.api.model.VolumeMountBuilder;
import io.fabric8.openshift.client.DefaultOpenShiftClient;
import io.fabric8.openshift.client.OpenShiftClient;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.plugin.openshift.client.exception.OpenShiftException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class for executing simple commands in a Persistent Volume on Openshift.
 *
 * <p>Creates a short-lived Pod using a CentOS image which mounts a specified PVC and executes a
 * command (either {@code mkdir -p <path>} or {@code rm -rf <path}). Reports back whether the pod
 * succeeded or failed. Supports multiple paths for one command.
 *
 * <p>For mkdir commands, an in-memory list of created workspaces is stored and used to avoid
 * calling mkdir unnecessarily. However, this list is not persisted, so dir creation is not tracked
 * between restarts.
 *
 * @author amisevsk
 */
@Singleton
public class OpenShiftPvcHelper {

  private static final Logger LOG = LoggerFactory.getLogger(OpenShiftPvcHelper.class);

  private static final String POD_PHASE_SUCCEEDED = "Succeeded";
  private static final String POD_PHASE_FAILED = "Failed";
  private static final String[] MKDIR_WORKSPACE_COMMAND = new String[] {"mkdir", "-p"};
  private static final String[] RMDIR_WORKSPACE_COMMAND = new String[] {"rm", "-rf"};

  private static final Set<String> createdWorkspaces = ConcurrentHashMap.newKeySet();

  private final String jobImage;
  private final String jobMemoryLimit;

  @Inject private OpenshiftWorkspaceEnvironmentProvider openshiftUserAccountProvider;

  protected enum Command {
    REMOVE,
    MAKE
  }

  @Inject
  protected OpenShiftPvcHelper(
      @Named("che.openshift.jobs.image") String jobImage,
      @Named("che.openshift.jobs.memorylimit") String jobMemoryLimit) {
    this.jobImage = jobImage;
    this.jobMemoryLimit = jobMemoryLimit;
  }

  /**
   * Creates a pod with {@code command} and reports whether it succeeded
   *
   * @param workspacesPvcName name of the PVC to mount
   * @param projectNamespace OpenShift namespace
   * @param jobNamePrefix prefix used for pod metadata name. Name structure will normally be {@code
   *     <prefix><workspaceDirs>} if only one path is passed, or {@code <prefix>batch} if multiple
   *     paths are provided
   * @param command command to execute in PVC.
   * @param workspaceDirs list of arguments attached to command. A list of directories to
   *     create/delete.
   * @return true if Pod terminates with phase "Succeeded" or mkdir command issued for already
   *     created worksapce, false otherwise.
   * @throws OpenShiftException
   * @see Command
   */
  protected boolean createJobPod(
      Subject subject,
      String workspacesPvcName,
      String projectNamespace,
      String jobNamePrefix,
      Command command,
      String... workspaceDirs)
      throws OpenShiftException {

    if (workspaceDirs.length == 0) {
      return true;
    }

    List<String> logsDirs = Arrays.asList(workspaceDirs);
    logsDirs =
        logsDirs
            .stream()
            .map(dir -> dir + WORKSPACE_LOGS_FOLDER_SUFFIX)
            .collect(Collectors.toList());

    List<String> allDirs = new ArrayList<>();
    allDirs.addAll(Arrays.asList(workspaceDirs));
    allDirs.addAll(logsDirs);
    String[] allDirsArray = allDirs.toArray(new String[allDirs.size()]);

    if (Command.MAKE.equals(command)) {
      String[] dirsToCreate = filterDirsToCreate(allDirsArray);
      if (dirsToCreate.length == 0) {
        return true;
      }
      allDirsArray = dirsToCreate;
    }

    VolumeMount vm =
        new VolumeMountBuilder().withMountPath("/projects").withName(workspacesPvcName).build();

    PersistentVolumeClaimVolumeSource pvcs =
        new PersistentVolumeClaimVolumeSourceBuilder().withClaimName(workspacesPvcName).build();

    Volume volume =
        new VolumeBuilder().withPersistentVolumeClaim(pvcs).withName(workspacesPvcName).build();

    String[] jobCommand = getCommand(command, "/projects/", allDirsArray);
    LOG.info(
        "Executing command {} in PVC {} for {} dirs",
        jobCommand[0],
        workspacesPvcName,
        allDirs.size());

    Map<String, Quantity> limit = Collections.singletonMap("memory", new Quantity(jobMemoryLimit));

    String podName =
        workspaceDirs.length > 1 ? jobNamePrefix + "batch" : jobNamePrefix + workspaceDirs[0];

    Container container =
        new ContainerBuilder()
            .withName(podName)
            .withImage(jobImage)
            .withImagePullPolicy("IfNotPresent")
            .withNewSecurityContext()
            .withPrivileged(false)
            .endSecurityContext()
            .withCommand(jobCommand)
            .withVolumeMounts(vm)
            .withNewResources()
            .withLimits(limit)
            .endResources()
            .build();

    Pod podSpec =
        new PodBuilder()
            .withNewMetadata()
            .withName(podName)
            .endMetadata()
            .withNewSpec()
            .withContainers(container)
            .withVolumes(volume)
            .withRestartPolicy("Never")
            .endSpec()
            .build();

    try (OpenShiftClient openShiftClient =
        new DefaultOpenShiftClient(openshiftUserAccountProvider.getWorkspacesOpenshiftConfig())) {
      openShiftClient.pods().inNamespace(projectNamespace).create(podSpec);
      boolean completed = false;
      while (!completed) {
        Pod pod = openShiftClient.pods().inNamespace(projectNamespace).withName(podName).get();
        String phase = pod.getStatus().getPhase();
        switch (phase) {
          case POD_PHASE_FAILED:
            LOG.info("Pod command {} failed", Arrays.toString(jobCommand));
            // fall through
          case POD_PHASE_SUCCEEDED:
            openShiftClient.resource(pod).delete();
            updateCreatedDirs(command, phase, allDirsArray);
            return POD_PHASE_SUCCEEDED.equals(phase);
          default:
            Thread.sleep(1000);
        }
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
    return false;
  }

  private String[] getCommand(Command commandType, String mountPath, String... dirs) {
    String[] command = new String[0];
    switch (commandType) {
      case MAKE:
        command = MKDIR_WORKSPACE_COMMAND;
        break;
      case REMOVE:
        command = RMDIR_WORKSPACE_COMMAND;
        break;
    }

    String[] dirsWithPath =
        Arrays.asList(dirs).stream().map(dir -> mountPath + dir).toArray(String[]::new);

    String[] fullCommand = new String[command.length + dirsWithPath.length];

    System.arraycopy(command, 0, fullCommand, 0, command.length);
    System.arraycopy(dirsWithPath, 0, fullCommand, command.length, dirsWithPath.length);
    return fullCommand;
  }

  private void updateCreatedDirs(Command command, String phase, String... workspaceDirs) {
    if (!POD_PHASE_SUCCEEDED.equals(phase)) {
      return;
    }
    List<String> dirs = Arrays.asList(workspaceDirs);
    switch (command) {
      case MAKE:
        createdWorkspaces.addAll(dirs);
        break;
      case REMOVE:
        createdWorkspaces.removeAll(dirs);
        break;
    }
  }

  private String[] filterDirsToCreate(String[] allDirs) {
    List<String> dirs = Arrays.asList(allDirs);
    List<String> dirsToCreate = new ArrayList<>();
    for (String dir : dirs) {
      if (!createdWorkspaces.contains(dir)) {
        dirsToCreate.add(dir);
      }
    }
    return dirsToCreate.toArray(new String[dirsToCreate.size()]);
  }
}
