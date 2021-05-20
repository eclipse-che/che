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
package org.eclipse.che.workspace.infrastructure.kubernetes.server.secure;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.assistedinject.Assisted;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServicePort;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import javax.inject.Inject;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment.PodData;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.external.ExternalServerExposer;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.external.ExternalServerExposerProvider;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.secure.jwtproxy.PassThroughProxyProvisioner;

/**
 * Exposes secure servers using a proxy.
 *
 * <p>To expose secure servers it provisions proxy objects into environment with {@link
 * org.eclipse.che.workspace.infrastructure.kubernetes.server.secure.ProxyProvisioner}. Then proxy
 * service port is made public accessible by {@link ExternalServerExposer <T>}.
 *
 * <p>In this way, requests to exposed secure servers will be routed via the proxy that is added one
 * per workspace.
 *
 * @see
 *     org.eclipse.che.workspace.infrastructure.kubernetes.server.secure.jwtproxy.JwtProxyProvisioner
 * @see PassThroughProxyProvisioner
 */
public class DefaultSecureServerExposer<T extends KubernetesEnvironment>
    implements SecureServerExposer<T> {

  private final ExternalServerExposer<T> exposer;
  private final ProxyProvisioner proxyProvisioner;

  @VisibleForTesting
  public DefaultSecureServerExposer(
      ProxyProvisioner jwtProxyProvisioner, ExternalServerExposer<T> exposer) {
    this.exposer = exposer;
    this.proxyProvisioner = jwtProxyProvisioner;
  }

  @Inject
  public DefaultSecureServerExposer(
      @Assisted RuntimeIdentity identity,
      ProxyProvisionerFactory proxyProvisionerFactory,
      ExternalServerExposerProvider<T> exposer) {
    this.exposer = exposer.get();
    this.proxyProvisioner = proxyProvisionerFactory.create(identity);
  }

  /**
   * This always returns an empty optional because JWT proxy is injected into the workspace pod and
   * assumes the servers it exposes listen on localhost.
   *
   * @see
   *     org.eclipse.che.workspace.infrastructure.kubernetes.server.secure.SecureServerExposer#createService(Collection,
   *     PodData, String, Map)
   */
  @Override
  public Optional<Service> createService(
      Collection<ServicePort> allSecurePorts,
      PodData pod,
      String machineName,
      Map<String, ? extends ServerConfig> secureServers) {
    return Optional.empty();
  }

  @Override
  public void expose(
      T k8sEnv,
      PodData pod,
      String machineName,
      @Nullable String serviceName,
      @Nullable String serverId,
      ServicePort servicePort,
      Map<String, ServerConfig> secureServers)
      throws InfrastructureException {

    Map<String, ServerConfig> conformingServers =
        exposer.getStrategyConformingServers(secureServers);
    Map<String, ServerConfig> subdomainServers =
        exposer.getServersRequiringSubdomain(secureServers);

    if (!conformingServers.isEmpty()) {
      doExpose(
          k8sEnv, pod, machineName, serviceName, serverId, servicePort, false, conformingServers);
    }

    if (!subdomainServers.isEmpty()) {
      doExpose(
          k8sEnv, pod, machineName, serviceName, serverId, servicePort, true, subdomainServers);
    }
  }

  private void doExpose(
      T k8sEnv,
      PodData pod,
      String machineName,
      @Nullable String serviceName,
      @Nullable String serverId,
      ServicePort servicePort,
      boolean requireSubdomain,
      Map<String, ServerConfig> secureServers)
      throws InfrastructureException {
    ServicePort exposedServicePort =
        proxyProvisioner.expose(
            k8sEnv,
            pod,
            machineName,
            serviceName,
            servicePort,
            servicePort.getProtocol(),
            requireSubdomain,
            secureServers);

    exposer.expose(
        k8sEnv,
        machineName,
        proxyProvisioner.getServiceName(),
        serverId,
        exposedServicePort,
        secureServers);
  }
}
