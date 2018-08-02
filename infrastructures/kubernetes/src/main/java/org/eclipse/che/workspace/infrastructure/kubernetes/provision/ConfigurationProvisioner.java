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
package org.eclipse.che.workspace.infrastructure.kubernetes.provision;

import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.kubernetes.KubernetesInfrastructure;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;

/**
 * Modifies workspace environment configuration and Kubernetes environment with everything needed
 * for some logical part of {@link KubernetesInfrastructure}.
 *
 * @author Anton Korneta
 */
public interface ConfigurationProvisioner<T extends KubernetesEnvironment> {

  /**
   * Configures the Kubernetes environment and workspace environment with infrastructure needs.
   *
   * @param k8sEnv Kubernetes environment
   * @param identity runtime identity
   * @throws InfrastructureException when any error occurs
   */
  void provision(T k8sEnv, RuntimeIdentity identity) throws InfrastructureException;
}
