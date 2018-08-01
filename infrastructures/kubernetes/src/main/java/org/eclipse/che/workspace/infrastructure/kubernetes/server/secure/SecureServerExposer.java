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
package org.eclipse.che.workspace.infrastructure.kubernetes.server.secure;

import io.fabric8.kubernetes.api.model.ServicePort;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;

/**
 * Modifies the specified Kubernetes environment to expose secure servers.
 *
 * <p>Note that ONE {@link SecureServerExposer} instance should be used for one workspace start.
 *
 * @author Sergii Leshchenko
 */
public interface SecureServerExposer<T extends KubernetesEnvironment> {

  /**
   * Modifies the specified Kubernetes environment to expose secure servers.
   *
   * @param k8sEnv Kubernetes environment that should be modified.
   * @param machineName machine name to which secure servers belong to
   * @param serviceName service name that exposes secure servers
   * @param servicePort service port that exposes secure servers
   * @param secureServers secure servers to expose
   * @throws InfrastructureException when any exception occurs during servers exposing
   */
  void expose(
      T k8sEnv,
      String machineName,
      String serviceName,
      ServicePort servicePort,
      Map<String, ServerConfig> secureServers)
      throws InfrastructureException;
}
