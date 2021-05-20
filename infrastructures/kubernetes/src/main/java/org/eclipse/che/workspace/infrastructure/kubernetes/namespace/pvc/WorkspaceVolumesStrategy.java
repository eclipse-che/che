/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
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

import java.util.Map;
import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.ConfigurationProvisioner;

/**
 * Defines a basic set of operations for workspace volume provisioning strategies.
 *
 * @author Anton Korneta
 */
public interface WorkspaceVolumesStrategy extends ConfigurationProvisioner {

  /**
   * Configures the workspace PVCs, volumes, claim bindings with a strategy specific options.
   *
   * @param k8sEnv Kubernetes environment
   * @param identity runtime identity
   * @throws InfrastructureException when any error occurs while provisioning volumes
   */
  @Override
  void provision(KubernetesEnvironment k8sEnv, RuntimeIdentity identity)
      throws InfrastructureException;

  /**
   * Prepares volumes for backup of workspace data on a specific machine in a strategy specific way.
   * Note that this step, depending on the strategy, may take some time.
   *
   * @param k8sEnv Kubernetes environment that changes as a result of preparation
   * @param identity the target into which the workspace is being provisioned and where the volumes
   *     will be prepared.
   * @param timeoutMillis timeout in milliseconds
   * @throws InfrastructureException when any error while preparation occurs
   */
  void prepare(
      KubernetesEnvironment k8sEnv,
      RuntimeIdentity identity,
      long timeoutMillis,
      Map<String, String> startOptions)
      throws InfrastructureException;

  /**
   * Cleanups workspace backed up data in a strategy specific way.
   *
   * @param workspace the workspace for which cleanup will be performed
   * @throws InfrastructureException when any error while cleanup occurs
   */
  void cleanup(Workspace workspace) throws InfrastructureException;
}
