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
package org.eclipse.che.workspace.infrastructure.kubernetes.cache;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.core.model.workspace.config.Command;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.kubernetes.model.KubernetesRuntimeState;

/**
 * Caches runtime state.
 *
 * @author Sergii Leshchenko
 */
public interface KubernetesRuntimeStateCache {

  /**
   * Put runtime state into cache.
   *
   * @param state state to cache
   * @return true if state is put, false if state is already cached
   * @throws InfrastructureException if any exception occurs during entity putting
   */
  boolean putIfAbsent(KubernetesRuntimeState state) throws InfrastructureException;

  /**
   * Returns runtime identities of cached runtimes.
   *
   * @throws InfrastructureException if any exception occurs during entities fetching
   */
  Set<RuntimeIdentity> getIdentities() throws InfrastructureException;

  /**
   * Returns optional with status of the runtime with specified identifier or empty optional if
   * there is not cached state.
   *
   * @param runtimeId runtime identifier
   * @throws InfrastructureException if any exception occurs during status fetching
   */
  Optional<WorkspaceStatus> getStatus(RuntimeIdentity runtimeId) throws InfrastructureException;

  /**
   * Returns commands of the runtime with specified identifier or empty list when state is not
   * cached.
   *
   * @param runtimeId runtime identifier
   * @throws InfrastructureException if any exception occurs during commands fetching
   */
  List<? extends Command> getCommands(RuntimeIdentity runtimeId) throws InfrastructureException;

  /**
   * Returns optional with state of the runtime with specified identifier.
   *
   * @param runtimeId runtime identifier
   * @throws InfrastructureException if any exception occurs during state fetching
   */
  Optional<KubernetesRuntimeState> get(RuntimeIdentity runtimeId) throws InfrastructureException;

  /**
   * Updates status of cached runtime.
   *
   * @param runtimeId runtime identifier
   * @param newStatus status to update
   * @throws InfrastructureException if any exception occurs during status updating
   */
  void updateStatus(RuntimeIdentity runtimeId, WorkspaceStatus newStatus)
      throws InfrastructureException;

  /**
   * Updates status of cached runtime if previous value matches the specified predicate.
   *
   * @param identity runtime identifier
   * @param predicate predicate to test the previous status
   * @param newStatus status to update
   * @return true if status is updated, false if status is not specified because
   * @throws InfrastructureException if any exception occurs during status updating
   */
  boolean updateStatus(
      RuntimeIdentity identity, Predicate<WorkspaceStatus> predicate, WorkspaceStatus newStatus)
      throws InfrastructureException;

  /**
   * Updates commands of cached runtime state.
   *
   * @param identity runtime identifier
   * @param commands commands to store
   * @throws InfrastructureException if there is no cached state for specified runtime identifier
   * @throws InfrastructureException if any exception occurs during commands updating
   */
  void updateCommands(RuntimeIdentity identity, List<? extends Command> commands)
      throws InfrastructureException;

  /**
   * Removes state of the runtime with specified identifier.
   *
   * @param runtimeId runtime identifier
   * @throws InfrastructureException if any exception occurs during state removing
   */
  void remove(RuntimeIdentity runtimeId) throws InfrastructureException;
}
