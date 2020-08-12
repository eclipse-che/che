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
package org.eclipse.che.workspace.infrastructure.kubernetes.server.external;

import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.ServicePort;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.api.workspace.server.model.impl.ServerConfigImpl;
import org.eclipse.che.api.workspace.server.spi.environment.GatewayRouteConfig;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.workspace.infrastructure.kubernetes.Annotations;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.KubernetesServerExposer;

/**
 * Uses gateway configured with ConfigMaps to expose servers.
 *
 * @param <T> type of environment
 */
public class GatewayServerExposer<T extends KubernetesEnvironment>
    implements ExternalServerExposer<T> {

  private final ExternalServiceExposureStrategy strategy;

  @Inject
  public GatewayServerExposer(ExternalServiceExposureStrategy strategy) {
    this.strategy = strategy;
  }

  /**
   * Exposes service port on given service externally (outside kubernetes cluster) using the Gateway
   * specific configurations.
   *
   * @param k8sEnv Kubernetes environment
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
      @Nullable String machineName,
      String serviceName,
      String serverId,
      ServicePort servicePort,
      Map<String, ServerConfig> externalServers) {

    if (serverId == null) {
      // this is the ID for non-unique servers
      serverId = servicePort.getName();
    }

    k8sEnv.addGatewayRouteConfig(
        createGatewayRouteConfig(machineName, serviceName, serverId, servicePort, externalServers));
  }

  private GatewayRouteConfig createGatewayRouteConfig(
      String machineName,
      String serviceName,
      String serverId,
      ServicePort servicePort,
      Map<String, ServerConfig> serversConfigs) {
    final String serverName = KubernetesServerExposer.makeServerNameValidForDns(serverId);
    final String name = createName(serviceName, serverName);
    final String path = ensureDontEndsWithSlash(strategy.getExternalPath(serviceName, serverName));
    final Map<String, String> annotations = createAnnotations(serversConfigs, path, machineName);
    return new GatewayRouteConfig(
        name, serviceName, getTargetPort(servicePort.getTargetPort()), path, annotations);
  }

  private String ensureDontEndsWithSlash(String path) {
    return path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
  }

  private String createName(String serviceName, String serverName) {
    return serviceName + "-" + serverName;
  }

  private String getTargetPort(IntOrString targetPort) {
    return targetPort.getIntVal() != null
        ? targetPort.getIntVal().toString()
        : targetPort.getStrVal();
  }

  private Map<String, String> createAnnotations(
      Map<String, ServerConfig> serversConfigs, String path, String machineName) {
    Map<String, ServerConfig> configsWithPaths = new HashMap<>();
    for (String scKey : serversConfigs.keySet()) {
      configsWithPaths.put(scKey, new ServerConfigImpl(serversConfigs.get(scKey)).withPath(path));
    }

    return Annotations.newSerializer()
        .servers(configsWithPaths)
        .machineName(machineName)
        .annotations();
  }
}
