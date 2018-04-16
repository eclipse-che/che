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
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;

/**
 * Abstract Ingress-based external server exposer strategy. Creates an ingress for each
 * service-exposed port, with relevant server annotations.
 *
 * @author Guy Daich
 */
public abstract class AbstractIngressExternalServerExposerStrategy
    implements ExternalServerExposerStrategy<KubernetesEnvironment> {

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

      Ingress ingress = generateIngress(machineName, serviceName, servicePort, ingressesServers);

      k8sEnv.getIngresses().put(ingress.getMetadata().getName(), ingress);
    }
  }

  protected abstract Ingress generateIngress(
      String machineName,
      String serviceName,
      ServicePort servicePort,
      Map<String, ServerConfig> ingressesServers);
}
