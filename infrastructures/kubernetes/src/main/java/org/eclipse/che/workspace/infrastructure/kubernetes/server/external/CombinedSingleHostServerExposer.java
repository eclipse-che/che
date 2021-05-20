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
package org.eclipse.che.workspace.infrastructure.kubernetes.server.external;

import static java.util.stream.Collectors.toMap;

import io.fabric8.kubernetes.api.model.ServicePort;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;

/**
 * This {@link ExternalServerExposer} is used in single-host mode when we need to expose some
 * servers on subdomain, instead of subpaths.
 *
 * <p>It aggregates 2 {@link ExternalServerExposer}s, using one to expose servers on subdomand, and
 * 2nd to expose servers on subpaths. It determines which to use for individual server based on some
 * attribute in {@link ServerConfig#getAttributes()} (see implementation {@link
 * CombinedSingleHostServerExposer#expose(KubernetesEnvironment, String, String, String,
 * ServicePort, Map)} for the details).
 *
 * @param <T> environment type
 */
public class CombinedSingleHostServerExposer<T extends KubernetesEnvironment>
    implements ExternalServerExposer<T> {

  private final ExternalServerExposer<T> subdomainServerExposer;
  private final ExternalServerExposer<T> subpathServerExposer;

  public CombinedSingleHostServerExposer(
      ExternalServerExposer<T> subdomainServerExposer,
      ExternalServerExposer<T> subpathServerExposer) {
    this.subdomainServerExposer = subdomainServerExposer;
    this.subpathServerExposer = subpathServerExposer;
  }

  /**
   * Exposes given 'externalServers' to either subdomain or subpath, using 2 different {@link
   * ExternalServerExposer}s. Which one to use for individual server is determined with {@link
   * ServerConfig#REQUIRE_SUBDOMAIN} attribute.
   *
   * @param k8sEnv environment
   * @param machineName machine containing servers
   * @param serviceName service associated with machine, mapping all machine server ports
   * @param serverId non-null for a unique server, null for a compound set of servers that should be
   *     exposed together.
   * @param servicePort specific service port to be exposed externally
   * @param externalServers server configs of servers to be exposed externally
   */
  @Override
  public void expose(
      T k8sEnv,
      String machineName,
      String serviceName,
      String serverId,
      ServicePort servicePort,
      Map<String, ServerConfig> externalServers) {

    if (serverId == null) {
      // this is the ID for non-unique servers
      serverId = servicePort.getName();
    }

    Map<String, ServerConfig> subpathServers = getStrategyConformingServers(externalServers);
    Map<String, ServerConfig> subdomainServers = getServersRequiringSubdomain(externalServers);

    if (!subpathServers.isEmpty()) {
      subpathServerExposer.expose(
          k8sEnv, machineName, serviceName, serverId, servicePort, subpathServers);
    }

    if (!subdomainServers.isEmpty()) {
      subdomainServerExposer.expose(
          k8sEnv, machineName, serviceName, serverId, servicePort, subdomainServers);
    }
  }

  @Override
  public Map<String, ServerConfig> getStrategyConformingServers(
      Map<String, ServerConfig> externalServers) {
    return externalServers
        .entrySet()
        .stream()
        .filter(e -> !e.getValue().isRequireSubdomain())
        .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  @Override
  public Map<String, ServerConfig> getServersRequiringSubdomain(
      Map<String, ServerConfig> externalServers) {
    return externalServers
        .entrySet()
        .stream()
        .filter(e -> e.getValue().isRequireSubdomain())
        .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
  }
}
