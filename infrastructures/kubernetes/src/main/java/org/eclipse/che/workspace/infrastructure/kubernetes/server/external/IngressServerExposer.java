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

import static java.util.Collections.emptyMap;

import com.google.common.base.Splitter;
import io.fabric8.kubernetes.api.model.ServicePort;
import io.fabric8.kubernetes.api.model.extensions.Ingress;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.KubernetesServerExposer;

/**
 * Uses Kubernetes {@link Ingress}es to expose the services.
 *
 * @see ExternalServerExposer
 */
@Singleton
public class IngressServerExposer<T extends KubernetesEnvironment>
    implements ExternalServerExposer<T> {

  /**
   * A string to look for in the value of the "che.infra.kubernetes.ingress.path_transform"
   * configuration property that marks the location where the generated public path of the service
   * should be put in the final string representing the ingress path.
   */
  static final String PATH_TRANSFORM_PATH_CATCH = "%s";

  private final ExternalServiceExposureStrategy serviceExposureStrategy;
  private final Map<String, String> ingressAnnotations;
  private final Map<String, String> labels;
  private final String pathTransformFmt;

  @Inject
  public IngressServerExposer(
      ExternalServiceExposureStrategy serviceExposureStrategy,
      @Named("infra.kubernetes.ingress.annotations") Map<String, String> annotations,
      @Nullable @Named("che.infra.kubernetes.ingress.labels") String labelsProperty,
      @Nullable @Named("che.infra.kubernetes.ingress.path_transform") String pathTransformFmt) {
    this.serviceExposureStrategy = serviceExposureStrategy;
    this.ingressAnnotations = annotations;
    this.pathTransformFmt = pathTransformFmt == null ? PATH_TRANSFORM_PATH_CATCH : pathTransformFmt;
    this.labels =
        labelsProperty != null
            ? Splitter.on(",").withKeyValueSeparator("=").split(labelsProperty)
            : emptyMap();
  }

  /**
   * Exposes service port on given service externally (outside kubernetes cluster) using {@link
   * Ingress}.
   *
   * @see ExternalServerExposer#expose(KubernetesEnvironment, String, String, String, ServicePort,
   *     Map)
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

    String serverName = KubernetesServerExposer.makeServerNameValidForDns(serverId);
    ExternalServerIngressBuilder ingressBuilder = new ExternalServerIngressBuilder();
    String host = serviceExposureStrategy.getExternalHost(serviceName, serverName);
    if (host != null) {
      ingressBuilder = ingressBuilder.withHost(host);
    }

    return ingressBuilder
        .withPath(
            String.format(
                pathTransformFmt,
                ensureEndsWithSlash(
                    serviceExposureStrategy.getExternalPath(serviceName, serverName))))
        .withName(getIngressName(serviceName, serverName))
        .withMachineName(machineName)
        .withServiceName(serviceName)
        .withAnnotations(ingressAnnotations)
        .withLabels(labels)
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
}
