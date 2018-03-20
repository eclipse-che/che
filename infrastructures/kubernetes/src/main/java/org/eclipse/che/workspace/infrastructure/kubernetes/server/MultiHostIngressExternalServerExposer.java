/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.kubernetes.server;

import static java.lang.Integer.parseInt;
import static java.util.stream.Collectors.toMap;

import io.fabric8.kubernetes.api.model.ServicePort;
import io.fabric8.kubernetes.api.model.extensions.Ingress;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides a host-based strategy for exposing service ports outside the cluster using Ingress
 *
 * <p>This strategy uses different Ingress host entries <br>
 * Each external server is exposed with a unique subdomain of CHE_DOMAIN.
 *
 * <pre>
 *   Host-Based Ingress exposing service's port:
 * Ingress
 * ...
 * spec:
 *   rules:
 *     - host: service123-webapp.che-domain   ---->> Service.metadata.name + - + Service.spec.ports[0].name + . + CHE_DOMAIN
 *     - http:
 *         paths:
 *           - path: /
 *             backend:
 *               serviceName: service123      ---->> Service.metadata.name
 *               servicePort: [8080|web-app]  ---->> Service.spec.ports[0].[port|name]
 * </pre>
 *
 * @author Sergii Leshchenko
 * @author Guy Daich
 */
public class MultiHostIngressExternalServerExposer
    implements ExternalServerExposerStrategy<KubernetesEnvironment> {

  public static final String MULTI_HOST_STRATEGY = "multi-host";
  private final String domain;
  private final Map<String, String> ingressAnnotations;
  private static final Logger LOG =
      LoggerFactory.getLogger(MultiHostIngressExternalServerExposer.class);

  @Inject
  public MultiHostIngressExternalServerExposer(
      @Named("infra.kubernetes.ingress.annotations") Map<String, String> ingressAnnotations,
      @Named("che.infra.kubernetes.ingress.domain") String domain) {
    if (ingressAnnotations == null) {
      LOG.warn(
          "Ingresses annotations are absent. Make sure that workspace ingresses don't need "
              + "to be configured according to ingress controller.");
    }
    this.ingressAnnotations = ingressAnnotations;
    this.domain = domain;
  }

  @Override
  public void exposeExternalServers(
      KubernetesEnvironment k8sEnv,
      String machineName,
      String serviceName,
      Map<String, ServicePort> portToServicePort,
      Map<String, ServerConfig> externalServers) {

    for (ServicePort servicePort : portToServicePort.values()) {
      int port = servicePort.getTargetPort().getIntVal();

      Map<String, ServerConfig> ingressesServers =
          externalServers
              .entrySet()
              .stream()
              .filter(e -> parseInt(e.getValue().getPort().split("/")[0]) == port)
              .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));

      Ingress ingress =
          new ExternalServerIngressBuilder()
              .withHost(generateExternalServerIngressHostname(serviceName, servicePort))
              .withPath(generateExternalServerIngressPath())
              .withName(generateExternalServerIngressName(serviceName, servicePort))
              .withMachineName(machineName)
              .withServiceName(serviceName)
              .withAnnotations(ingressAnnotations)
              .withServicePort(servicePort.getName())
              .withServers(ingressesServers)
              .build();

      k8sEnv.getIngresses().put(ingress.getMetadata().getName(), ingress);
    }
  }

  private String generateExternalServerIngressPath() {
    return "/";
  }

  private String generateExternalServerIngressName(String serviceName, ServicePort servicePort) {
    return serviceName + '-' + servicePort.getName();
  }

  private String generateExternalServerIngressHostname(
      String serviceName, ServicePort servicePort) {
    return serviceName + "-" + servicePort.getName() + "." + domain;
  }
}
