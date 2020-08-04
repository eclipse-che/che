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

import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.fabric8.kubernetes.api.model.ServicePort;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.api.workspace.server.model.impl.ServerConfigImpl;
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

  protected static final Map<String, String> GATEWAY_CONFIGMAP_LABELS =
      ImmutableMap.<String, String>builder()
          .put("app", "che")
          .put("role", "gateway-config")
          .build();

  private final ExternalServiceExposureStrategy strategy;
  private final GatewayRouteConfigGenerator gatewayConfigGenerator;

  @Inject
  public GatewayServerExposer(
      ExternalServiceExposureStrategy strategy,
      GatewayRouteConfigGenerator gatewayConfigGenerator) {
    this.strategy = strategy;
    this.gatewayConfigGenerator = gatewayConfigGenerator;
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
    ConfigMap traefikGatewayConfig =
        generateTraefikConfig(machineName, serviceName, serverId, servicePort, externalServers);

    k8sEnv.getConfigMaps().put(traefikGatewayConfig.getMetadata().getName(), traefikGatewayConfig);
  }

  private ConfigMap generateTraefikConfig(
      String machineName,
      String serviceName,
      String serverId,
      ServicePort servicePort,
      Map<String, ServerConfig> serversConfigs) {
    final String serverName = KubernetesServerExposer.makeServerNameValidForDns(serverId);
    final String name = createName(serviceName, serverName);
    final String serviceClusterUrl = createServiceUrl(serviceName, servicePort);
    final String path = ensureEndsWithSlash(strategy.getExternalPath(serviceName, serverName));
    final Map<String, String> configData =
        gatewayConfigGenerator.generate(name, serviceClusterUrl, path);
    final Map<String, String> annotations = createAnnotations(serversConfigs, path, machineName);

    return new ConfigMapBuilder()
        .withNewMetadata()
        .withName(name)
        .withLabels(GATEWAY_CONFIGMAP_LABELS)
        .withAnnotations(annotations)
        .endMetadata()
        .withData(configData)
        .build();
  }

  private String ensureEndsWithSlash(String path) {
    return path.endsWith("/") ? path : path + '/';
  }

  private String createName(String serviceName, String serverName) {
    return serviceName + "-" + serverName;
  }

  private String createServiceUrl(String serviceName, ServicePort servicePort) {
    return "http://"
        + serviceName
        + ".che.svc.cluster.local:"
        + servicePort.getTargetPort().getIntVal().toString();
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
