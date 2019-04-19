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

import io.fabric8.kubernetes.api.model.ServicePort;
import io.fabric8.kubernetes.api.model.extensions.Ingress;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.workspace.infrastructure.kubernetes.Names;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;

/**
 * Provides a path-based strategy for exposing service ports outside the cluster using Ingress
 * Ingresses will be created without an explicit host (defaulting to *).
 *
 * <p>This strategy uses different Ingress path entries <br>
 * Each external server is exposed with a unique path prefix.
 *
 * <p>This strategy imposes limitation on user-developed applications. <br>
 * It should only be used for local development with a single IP address
 *
 * <pre>
 *   Path-Based Ingress exposing service's port:
 * Ingress
 * ...
 * spec:
 *   rules:
 *     - http:
 *         paths:
 *           - path: service123/webapp        ---->> Service.metadata.name + / + Service.spec.ports[0].name
 *             backend:
 *               serviceName: service123      ---->> Service.metadata.name
 *               servicePort: [8080|web-app]  ---->> Service.spec.ports[0].[port|name]
 * </pre>
 *
 * @author Sergii Leshchenko
 * @author Guy Daich
 */
public class DefaultHostIngressExternalServerExposer
    implements ExternalServerExposerStrategy<KubernetesEnvironment> {

  public static final String DEFAULT_HOST_STRATEGY = "default-host";
  private final Map<String, String> ingressAnnotations;

  @Inject
  public DefaultHostIngressExternalServerExposer(
      @Named("infra.kubernetes.ingress.annotations") Map<String, String> ingressAnnotations) {
    this.ingressAnnotations = ingressAnnotations;
  }

  @Override
  public void expose(
      KubernetesEnvironment k8sEnv,
      String machineName,
      String serviceName,
      ServicePort servicePort,
      Map<String, ServerConfig> externalServers) {
    Ingress ingress = generateIngress(machineName, serviceName, servicePort, externalServers);
    k8sEnv.getIngresses().put(ingress.getMetadata().getName(), ingress);
  }

  private Ingress generateIngress(
      String machineName,
      String serviceName,
      ServicePort servicePort,
      Map<String, ServerConfig> ingressesServers) {
    return new ExternalServerIngressBuilder()
        .withPath(generateExternalServerIngressPath(serviceName, servicePort))
        .withName(generateExternalServerIngressName())
        .withMachineName(machineName)
        .withServiceName(serviceName)
        .withAnnotations(ingressAnnotations)
        .withServicePort(servicePort.getName())
        .withServers(ingressesServers)
        .build();
  }

  private String generateExternalServerIngressName() {
    return Names.generateName("ingress");
  }

  private String generateExternalServerIngressPath(String serviceName, ServicePort servicePort) {
    return "/" + serviceName + "/" + servicePort.getName();
  }
}
