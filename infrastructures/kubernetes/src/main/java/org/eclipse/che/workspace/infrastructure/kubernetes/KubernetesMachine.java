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
package org.eclipse.che.workspace.infrastructure.kubernetes;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import org.eclipse.che.api.core.model.workspace.runtime.Machine;
import org.eclipse.che.api.core.model.workspace.runtime.MachineStatus;
import org.eclipse.che.api.core.model.workspace.runtime.Server;
import org.eclipse.che.api.workspace.server.model.impl.ServerImpl;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesNamespace;

/** @author Sergii Leshchenko */
public class KubernetesMachine implements Machine {

  private static final String KUBERNETES_POD_STATUS_RUNNING = "Running";
  // TODO Make timeout configurable
  private static final int EXEC_TIMEOUT_MIN = 5;

  private final String machineName;
  private final String podName;
  private final String containerName;
  private final Map<String, String> attributes;
  private final Map<String, ServerImpl> servers;
  private final MachineStatus status;
  private final KubernetesNamespace namespace;

  public KubernetesMachine(
      String machineName,
      String podName,
      String containerName,
      Map<String, String> attributes,
      Map<String, ServerImpl> servers,
      MachineStatus status,
      KubernetesNamespace kubernetesNamespace) {
    this.machineName = machineName;
    this.podName = podName;
    this.containerName = containerName;
    this.attributes = attributes;
    this.servers = servers;
    this.status = status;
    this.namespace = kubernetesNamespace;
  }

  public String getName() {
    return machineName;
  }

  public String getPodName() {
    return podName;
  }

  public String getContainerName() {
    return containerName;
  }

  @Override
  public Map<String, String> getAttributes() {
    return attributes;
  }

  @Override
  public Map<String, ? extends Server> getServers() {
    return servers;
  }

  @Override
  public MachineStatus getStatus() {
    return status;
  }

  /**
   * Executes command in this machine.
   *
   * @param outputConsumer command output consumer that accepts stream and output message
   * @param command command to execute
   * @throws InfrastructureException when exec timeout is reached
   * @throws InfrastructureException when {@link Thread} is interrupted while command executing
   * @throws InfrastructureException when command error stream is not empty
   * @throws InfrastructureException when any other exception occurs
   */
  public void exec(BiConsumer<String, String> outputConsumer, String... command)
      throws InfrastructureException {
    namespace.pods().exec(podName, containerName, EXEC_TIMEOUT_MIN, command, outputConsumer);
  }

  public void exec(String... command) throws InfrastructureException {
    namespace.pods().exec(podName, containerName, EXEC_TIMEOUT_MIN, command);
  }

  /**
   * Returns the future, which ends when machine is considered as running.
   *
   * <p>Note that the resulting future must be explicitly cancelled when its completion no longer
   * important because of finalization allocated resources.
   */
  public CompletableFuture<Void> waitRunningAsync() {
    return namespace
        .pods()
        .waitAsync(podName, p -> (KUBERNETES_POD_STATUS_RUNNING.equals(p.getStatus().getPhase())));
  }
}
