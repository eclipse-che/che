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

import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServicePort;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment.PodData;

/**
 * Modifies the specified Kubernetes environment to expose secure servers.
 *
 * <p>Note that ONE {@link SecureServerExposer} instance should be used for one workspace start.
 *
 * @author Sergii Leshchenko
 */
public interface SecureServerExposer<T extends KubernetesEnvironment> {

  String AUTH_ENDPOINT_PATH = "/jwt/auth";

  /**
   * Creates a service that should handle the traffic for the provided secure ports that are exposed
   * on the container from the pod.
   *
   * <p>The exposer may choose to not create such service or to create a service for only a subset
   * of the ports.
   *
   * @param allSecurePorts the secure ports on the container
   * @param pod the pod containing the container
   * @return an optional service to put "in front of" the pod to service the ports, empty if no such
   *     service should be created.
   */
  Optional<Service> createService(
      Collection<ServicePort> allSecurePorts,
      PodData pod,
      String machineName,
      Map<String, ? extends ServerConfig> secureServers);

  /**
   * Modifies the specified Kubernetes environment to expose secure servers.
   *
   * @param k8sEnv Kubernetes environment that should be modified.
   * @param pod the pod containing the exposed server
   * @param machineName machine name to which secure servers belong to
   * @param serviceName service name that exposes secure servers. Will be null if {@link
   *     #createService(Collection, PodData, String, Map)} returned empty optional
   * @param serverId non-null for a unique server, null for a compound set of servers that should be
   *     exposed together.
   * @param servicePort service port that exposes secure servers
   * @param secureServers secure servers to expose
   * @throws InfrastructureException when any exception occurs during servers exposing
   */
  void expose(
      T k8sEnv,
      PodData pod,
      String machineName,
      @Nullable String serviceName,
      @Nullable String serverId,
      ServicePort servicePort,
      Map<String, ServerConfig> secureServers)
      throws InfrastructureException;
}
