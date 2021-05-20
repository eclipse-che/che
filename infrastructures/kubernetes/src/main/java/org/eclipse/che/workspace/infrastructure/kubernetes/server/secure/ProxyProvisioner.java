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
package org.eclipse.che.workspace.infrastructure.kubernetes.server.secure;

import io.fabric8.kubernetes.api.model.ServicePort;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment.PodData;

/**
 * A proxy provisioner is a class responsible for provisioning and exposing a reverse proxy that
 * should proxy the access to a backend service.
 */
public interface ProxyProvisioner {
  int FIRST_AVAILABLE_PROXY_PORT = 4400;

  /**
   * Modifies the provided environment with Kubernetes objects needed for the proxy and creates a
   * service that is pointing to the proxy that can then be used to expose the proxy.
   *
   * <p>Note that this method is called multiple times (once for each backend service) and so has to
   * build the kubernetes objects and configuration iteratively, if required.
   *
   * @param k8sEnv Kubernetes environment to modify
   * @param pod the pod that runs the server being exposed
   * @param backendServiceName service name that will be exposed
   * @param backendServicePort service port that will be exposed
   * @param protocol protocol that will be used for exposed port
   * @param requireSubdomain if true, the supplied servers are supposed to require a subdomain, if
   *     false the servers are considered to follow the configured exposure strategy
   * @param secureServers secure servers to expose
   * @return JWTProxy service port that expose the specified one
   * @throws InfrastructureException if any exception occurs during port exposing
   */
  ServicePort expose(
      KubernetesEnvironment k8sEnv,
      PodData pod,
      String machineName,
      @Nullable String backendServiceName,
      ServicePort backendServicePort,
      String protocol,
      boolean requireSubdomain,
      Map<String, ServerConfig> secureServers)
      throws InfrastructureException;

  /** The name of the service handling the traffic to the proxy. */
  String getServiceName();
}
