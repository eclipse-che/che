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

import static java.lang.Boolean.TRUE;
import static org.eclipse.che.api.core.model.workspace.config.ServerConfig.SERVICE_NAME_ATTRIBUTE;
import static org.eclipse.che.api.core.model.workspace.config.ServerConfig.SERVICE_PORT_ATTRIBUTE;
import static org.eclipse.che.workspace.infrastructure.kubernetes.Annotations.CREATE_IN_CHE_INSTALLATION_NAMESPACE;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.ServicePort;
import java.util.Map;
import javax.inject.Inject;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.workspace.infrastructure.kubernetes.Annotations;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.KubernetesServerExposer;
import org.eclipse.che.workspace.infrastructure.kubernetes.util.GatewayConfigmapLabels;

/**
 * Uses gateway configured with ConfigMaps to expose servers.
 *
 * @param <T> type of environment
 */
public class GatewayServerExposer<T extends KubernetesEnvironment>
    implements ExternalServerExposer<T> {

  private final ExternalServiceExposureStrategy strategy;
  private final GatewayConfigmapLabels configmapLabels;

  @Inject
  public GatewayServerExposer(
      ExternalServiceExposureStrategy strategy, GatewayConfigmapLabels configmapLabels) {
    this.strategy = strategy;
    this.configmapLabels = configmapLabels;
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

    for (String esKey : externalServers.keySet()) {
      final String serverName = KubernetesServerExposer.makeServerNameValidForDns(serverId);
      final String name = createName(serviceName, serverName);
      k8sEnv
          .getConfigMaps()
          .put(
              name,
              createGatewayRouteConfigmap(
                  name,
                  machineName,
                  serviceName,
                  servicePort,
                  serverName,
                  esKey,
                  externalServers.get(esKey)));
    }
  }

  private ConfigMap createGatewayRouteConfigmap(
      String name,
      String machineName,
      String serviceName,
      ServicePort servicePort,
      String serverName,
      String scRef,
      ServerConfig serverConfig) {

    final String path = ensureEndsWithSlash(strategy.getExternalPath(serviceName, serverName));
    serverConfig.getAttributes().put(SERVICE_NAME_ATTRIBUTE, serviceName);
    ServerConfig.setEndpointOrigin(serverConfig.getAttributes(), path);
    serverConfig
        .getAttributes()
        .put(SERVICE_PORT_ATTRIBUTE, getTargetPort(servicePort.getTargetPort()));

    final Map<String, String> annotations =
        Annotations.newSerializer()
            .server(scRef, serverConfig)
            .machineName(machineName)
            .annotations();
    annotations.put(CREATE_IN_CHE_INSTALLATION_NAMESPACE, TRUE.toString());

    ConfigMapBuilder gatewayConfigMap =
        new ConfigMapBuilder()
            .withNewMetadata()
            .withName(name)
            .withLabels(configmapLabels.getLabels())
            .withAnnotations(annotations)
            .endMetadata();
    return gatewayConfigMap.build();
  }

  private String ensureEndsWithSlash(String path) {
    return path.endsWith("/") ? path : path + '/';
  }

  private String createName(String serviceName, String serverName) {
    return serviceName + "-" + serverName;
  }

  private String getTargetPort(IntOrString targetPort) {
    return targetPort.getIntVal() != null
        ? targetPort.getIntVal().toString()
        : targetPort.getStrVal();
  }
}
