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
import org.eclipse.che.workspace.infrastructure.kubernetes.server.resolver.IngressServerResolver;

/**
 * Uses gateway configured with ConfigMaps to expose servers.
 *
 * <p>TODO: implement
 *
 * @param <T> type of environment
 */
public class GatewayServerExposer<T extends KubernetesEnvironment>
    implements ExternalServerExposer<T> {

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
   * TODO: rewrite
   * Exposes service port on given service externally (outside kubernetes cluster). The exposed
   * service port is associated with a specific Server configuration. Server configuration should be
   * encoded in the exposing object's annotations, to be used by {@link IngressServerResolver}.
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
    String serverName = KubernetesServerExposer.makeServerNameValidForDns(serverId);
    String name = getIngressName(serviceName, serverName);

    String serviceClusterUrl =
        "http://"
            + serviceName
            + ".che.svc.cluster.local:"
            + servicePort.getTargetPort().getIntVal().toString();
    String path = ensureEndsWithSlash(strategy.getExternalPath(serviceName, serverName));
    Map<String, String> configData = new HashMap<>();

    String routeConfig = gatewayConfigGenerator.generate(name, serviceClusterUrl, path);

    configData.put(name + ".yml", routeConfig);
    Map<String, String> labels = new HashMap<>();
    labels.put("app", "che");
    labels.put("role", "gateway-config");

    Map<String, ServerConfig> configsWithPaths = new HashMap<>();
    for (String scKey : serversConfigs.keySet()) {
      ServerConfigImpl sc = new ServerConfigImpl(serversConfigs.get(scKey));
      sc.setPath(path);
      configsWithPaths.put(scKey, sc);
    }

    Map<String, String> cmAnnotations =
        new HashMap<>(
            Annotations.newSerializer()
                .servers(configsWithPaths)
                .machineName(machineName)
                .annotations());

    return new ConfigMapBuilder()
        .withNewMetadata()
        .withName(name)
        .withLabels(labels)
        .withAnnotations(cmAnnotations)
        .endMetadata()
        .withData(configData)
        .build();
  }

  private static String ensureEndsWithSlash(String path) {
    return path.endsWith("/") ? path : path + '/';
  }

  private static String getIngressName(String serviceName, String serverName) {
    return serviceName + "-" + serverName;
  }
}
