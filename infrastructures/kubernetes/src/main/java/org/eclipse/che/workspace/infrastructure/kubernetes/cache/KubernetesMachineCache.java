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
package org.eclipse.che.workspace.infrastructure.kubernetes.cache;

import java.util.Map;
import org.eclipse.che.api.core.model.workspace.runtime.MachineStatus;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.core.model.workspace.runtime.ServerStatus;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.kubernetes.model.KubernetesMachineImpl;
import org.eclipse.che.workspace.infrastructure.kubernetes.model.KubernetesServerImpl;

/**
 * Caches kubernetes machines.
 *
 * @author Sergii Leshchenko
 */
public interface KubernetesMachineCache {

  /**
   * Put machine state into cache.
   *
   * <p>Note that this method MUST NOT be used for machine state updating.
   *
   * @param runtimeIdentity runtime identifier
   * @param machine machine to cache
   * @throws InfrastructureException if machine with specified runtime id and name is already cached
   * @throws InfrastructureException if any exception occurs during machine caching
   */
  void put(RuntimeIdentity runtimeIdentity, KubernetesMachineImpl machine)
      throws InfrastructureException;

  /**
   * Returns cached machine which belong to the specified runtime id.
   *
   * @param runtimeIdentity runtime identifier.
   * @throws InfrastructureException if any exception occurs during machines fetching
   */
  Map<String, KubernetesMachineImpl> getMachines(RuntimeIdentity runtimeIdentity)
      throws InfrastructureException;

  /**
   * Returns cached server.
   *
   * @param runtimeIdentity runtime identifier
   * @param machineName machine name to which the server belong to
   * @param serverName server name
   * @throws InfrastructureException if any exception occurs during server fetching
   */
  KubernetesServerImpl getServer(
      RuntimeIdentity runtimeIdentity, String machineName, String serverName)
      throws InfrastructureException;

  /**
   * Updates machine status.
   *
   * @param runtimeIdentity runtime identifier
   * @param machineName machine name to which the server belong to
   * @param newStatus status to update
   * @throws InfrastructureException if any exception occurs during machine status updating
   */
  void updateMachineStatus(
      RuntimeIdentity runtimeIdentity, String machineName, MachineStatus newStatus)
      throws InfrastructureException;

  /**
   * Updates server status.
   *
   * @param runtimeIdentity runtime identifier
   * @param machineName machine name to which the server belong to
   * @param serverName server name
   * @param newStatus status to update
   * @return true if status is update, false if the server already has the same status.
   * @throws InfrastructureException if any exception occurs during server status updating
   */
  boolean updateServerStatus(
      RuntimeIdentity runtimeIdentity,
      String machineName,
      String serverName,
      ServerStatus newStatus)
      throws InfrastructureException;

  /**
   * Returns cached machine which belong to the specified runtime id.
   *
   * @param identity runtime identity
   * @throws InfrastructureException if any exception occurs during machines removing
   */
  void remove(RuntimeIdentity identity) throws InfrastructureException;
}
