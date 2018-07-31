/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.kubernetes.server.secure.jwtproxy;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.assistedinject.Assisted;
import io.fabric8.kubernetes.api.model.ServicePort;
import java.util.Map;
import javax.inject.Inject;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.multiuser.machine.authentication.server.signature.SignatureKeyManager;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.external.ExternalServerExposerStrategy;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.secure.SecureServerExposer;

/**
 * Exposes secure servers with JWTProxy.
 *
 * <p>To expose secure servers it provisions JwtProxy objects into environment with {@link
 * JwtProxyProvisioner}. Then JwtProxy service port is made public accessible by {@link
 * ExternalServerExposerStrategy<T>}.
 *
 * <p>In this way, requests to exposed secure servers will be routed via JwtProxy pod that is added
 * one per workspace. And it will be impossible to requests secure servers if there is no machine
 * token in request.
 *
 * @see JwtProxyProvisioner
 * @author Sergii Leshchenko
 */
public class JwtProxySecureServerExposer<T extends KubernetesEnvironment>
    implements SecureServerExposer<T> {

  private final ExternalServerExposerStrategy<T> exposerStrategy;
  private final JwtProxyProvisioner proxyProvisioner;

  @VisibleForTesting
  JwtProxySecureServerExposer(
      JwtProxyProvisioner jwtProxyProvisioner, ExternalServerExposerStrategy<T> exposerStrategy) {
    this.exposerStrategy = exposerStrategy;
    this.proxyProvisioner = jwtProxyProvisioner;
  }

  @Inject
  public JwtProxySecureServerExposer(
      @Assisted RuntimeIdentity identity,
      SignatureKeyManager signatureKeyManager,
      ExternalServerExposerStrategy<T> exposerStrategy) {
    this.exposerStrategy = exposerStrategy;

    proxyProvisioner = new JwtProxyProvisioner(identity, signatureKeyManager);
  }

  @Override
  public void expose(
      T k8sEnv,
      String machineName,
      String serviceName,
      ServicePort servicePort,
      Map<String, ServerConfig> secureServers)
      throws InfrastructureException {
    ServicePort exposedServicePort =
        proxyProvisioner.expose(
            k8sEnv,
            serviceName,
            servicePort.getTargetPort().getIntVal(),
            servicePort.getProtocol(),
            secureServers);

    exposerStrategy.expose(
        k8sEnv, machineName, proxyProvisioner.getServiceName(), exposedServicePort, secureServers);
  }
}
