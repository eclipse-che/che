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
package org.eclipse.che.workspace.infrastructure.kubernetes.server.external;

import static java.lang.String.format;
import static org.eclipse.che.workspace.infrastructure.kubernetes.server.external.ExternalServerExposerStrategyProvider.STRATEGY_PROPERTY;

import com.google.common.base.Strings;
import io.fabric8.kubernetes.api.model.ServicePort;
import io.fabric8.kubernetes.api.model.extensions.Ingress;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.inject.ConfigurationException;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;

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
  private static final String INGRESS_DOMAIN_PROPERTY = "che.infra.kubernetes.ingress.domain";

  private final String domain;
  private final Map<String, String> ingressAnnotations;

  @Inject
  public MultiHostIngressExternalServerExposer(
      @Named("infra.kubernetes.ingress.annotations") Map<String, String> ingressAnnotations,
      @Named(INGRESS_DOMAIN_PROPERTY) String domain,
      @Named(STRATEGY_PROPERTY) String strategy) {
    if (Strings.isNullOrEmpty(domain) && MULTI_HOST_STRATEGY.equals(strategy)) {
      throw new ConfigurationException(
          format(
              "Strategy of generating ingress URLs for Che servers is set to '%s', "
                  + "but property '%s' is not set",
              MULTI_HOST_STRATEGY, INGRESS_DOMAIN_PROPERTY));
    }
    this.ingressAnnotations = ingressAnnotations;
    this.domain = domain;
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
        .withHost(generateExternalServerIngressHostname(serviceName, servicePort))
        .withPath(generateExternalServerIngressPath())
        .withName(generateExternalServerIngressName(serviceName, servicePort))
        .withMachineName(machineName)
        .withServiceName(serviceName)
        .withAnnotations(ingressAnnotations)
        .withServicePort(servicePort.getName())
        .withServers(ingressesServers)
        .build();
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
