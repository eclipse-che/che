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
import io.fabric8.kubernetes.api.model.ServicePort;
import io.fabric8.kubernetes.api.model.extensions.Ingress;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.KubernetesServerResolver;

public class ExternalServerExposer<T extends KubernetesEnvironment> {
  /**
   * A string to look for in the value of the "che.infra.kubernetes.ingress.path_transform"
   * configuration property that marks the location where the generated public path of the service
   * should be put in the final string representing the ingress path.
   */
  static final String PATH_TRANSFORM_PATH_CATCH = "%s";

  private final ExternalServiceExposureStrategy strategy;
  private final Map<String, String> ingressAnnotations;
  private final String pathTransformFmt;

  @Inject
  public ExternalServerExposer(
      ExternalServiceExposureStrategy strategy,
      @Named("infra.kubernetes.ingress.annotations") Map<String, String> annotations,
      @Nullable @Named("che.infra.kubernetes.ingress.path_transform") String pathTransformFmt) {
    this.strategy = strategy;
    this.ingressAnnotations = annotations;
    this.pathTransformFmt = pathTransformFmt == null ? PATH_TRANSFORM_PATH_CATCH : pathTransformFmt;
  }

  /**
   * A helper method to split the servers to unique sets that should be exposed together.
   *
   * <p>The consumer is responsible for doing the actual exposure and is supplied 2 pieces of data.
   * The first is the server ID, which is non-null for any unique server from the input set and null
   * for any compound set of servers that should be exposed together. The caller is responsible for
   * figuring out an appropriate ID in such case.
   *
   * @param allServers all unique and non-unique servers mixed together
   * @param consumer the consumer responsible for handling the split sets of servers
   */
  public static void onEachExposableServerSet(
      Map<String, ServerConfig> allServers,
      BiConsumer<String, Map<String, ServerConfig>> consumer) {
    Map<String, ServerConfig> nonUniqueServers = new HashMap<>();

    for (Map.Entry<String, ServerConfig> e : allServers.entrySet()) {
      String serverId = makeValidDnsName(e.getKey());
      if (e.getValue().isUnique()) {
        consumer.accept(serverId, ImmutableMap.of(serverId, e.getValue()));
      } else {
        nonUniqueServers.put(serverId, e.getValue());
      }
    }

    if (!nonUniqueServers.isEmpty()) {
      consumer.accept(null, nonUniqueServers);
    }
  }

  /**
   * Exposes service ports on given service externally (outside kubernetes cluster). Each exposed
   * service port is associated with a specific Server configuration. Server configuration should be
   * encoded in the exposing object's annotations, to be used by {@link KubernetesServerResolver}.
   *
   * @param k8sEnv Kubernetes environment
   * @param machineName machine containing servers
   * @param serviceName service associated with machine, mapping all machine server ports
   * @param servicePort specific service port to be exposed externally
   * @param externalServers server configs of servers to be exposed externally
   */
  public void expose(
      T k8sEnv,
      String machineName,
      String serviceName,
      ServicePort servicePort,
      Map<String, ServerConfig> externalServers) {

    onEachExposableServerSet(
        externalServers,
        (serverId, servers) -> {
          if (serverId == null) {
            // this is the ID for non-unique servers
            serverId = servicePort.getName();
          }

          exposeServers(k8sEnv, machineName, serviceName, serverId, servicePort, servers);
        });
  }

  /** Exposes the given set of servers using a single ingress/route. */
  protected void exposeServers(
      T k8sEnv,
      String machineName,
      String serviceName,
      String serverId,
      ServicePort servicePort,
      Map<String, ServerConfig> externalServers) {

    Ingress ingress =
        generateIngress(machineName, serviceName, serverId, servicePort, externalServers);

    k8sEnv.getIngresses().put(ingress.getMetadata().getName(), ingress);
  }

  private Ingress generateIngress(
      String machineName,
      String serviceName,
      String serverId,
      ServicePort servicePort,
      Map<String, ServerConfig> servers) {

    String serverName = makeValidDnsName(serverId);
    ExternalServerIngressBuilder ingressBuilder = new ExternalServerIngressBuilder();
    String host = strategy.getExternalHost(serviceName, serverName);
    if (host != null) {
      ingressBuilder = ingressBuilder.withHost(host);
    }

    return ingressBuilder
        .withPath(
            String.format(
                pathTransformFmt,
                ensureEndsWithSlash(strategy.getExternalPath(serviceName, serverName))))
        .withName(getIngressName(serviceName, serverName))
        .withMachineName(machineName)
        .withServiceName(serviceName)
        .withAnnotations(ingressAnnotations)
        .withServicePort(servicePort.getName())
        .withServers(servers)
        .build();
  }

  private static String ensureEndsWithSlash(String path) {
    return path.endsWith("/") ? path : path + '/';
  }

  private static String getIngressName(String serviceName, String serverName) {
    return serviceName + "-" + serverName;
  }

  protected static String makeValidDnsName(String name) {
    return name.replaceAll("/", "-");
  }
}
