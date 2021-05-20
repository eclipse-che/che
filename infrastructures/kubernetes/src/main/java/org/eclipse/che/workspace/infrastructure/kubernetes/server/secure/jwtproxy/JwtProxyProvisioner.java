/*
 * Copyright (c) 2012-2020 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.kubernetes.server.secure.jwtproxy;

import com.google.inject.assistedinject.Assisted;
import java.security.KeyPair;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InternalInfrastructureException;
import org.eclipse.che.multiuser.machine.authentication.server.signature.SignatureKeyManager;
import org.eclipse.che.multiuser.machine.authentication.server.signature.SignatureKeyManagerException;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.external.ServiceExposureStrategyProvider;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.secure.jwtproxy.factory.JwtProxyConfigBuilderFactory;

/**
 * Modifies Kubernetes environment to expose the specified service port via JWTProxy.
 *
 * <p>Exposing includes the following operation:
 *
 * <ul>
 *   <li>Putting Machine configuration into Kubernetes environment if absent;
 *   <li>Putting JwtProxy pod with one container if absent;
 *   <li>Putting JwtProxy service that will expose added JWTProxy pod if absent;
 *   <li>Putting JwtProxy ConfigMap that contains public key and JwtProxy config in yaml format if
 *       absent;
 *   <li>Updating JwtProxy Service to expose port for secure server;
 *   <li>Updating JwtProxy configuration in config map by adding the corresponding verifier proxy
 *       there;
 * </ul>
 *
 * @see JwtProxyConfigBuilder
 * @see SignatureKeyManager
 * @author Sergii Leshchenko
 */
public class JwtProxyProvisioner extends AbstractJwtProxyProvisioner {

  @Inject
  public JwtProxyProvisioner(
      SignatureKeyManager signatureKeyManager,
      JwtProxyConfigBuilderFactory jwtProxyConfigBuilderFactory,
      ServiceExposureStrategyProvider serviceExposureStrategyProvider,
      CookiePathStrategy cookiePathStrategy,
      MultiHostCookiePathStrategy multiHostCookiePathStrategy,
      @Named("che.server.secure_exposer.jwtproxy.image") String jwtProxyImage,
      @Named("che.server.secure_exposer.jwtproxy.memory_request") String memoryRequestBytes,
      @Named("che.server.secure_exposer.jwtproxy.memory_limit") String memoryLimitBytes,
      @Named("che.server.secure_exposer.jwtproxy.cpu_request") String cpuRequestCores,
      @Named("che.server.secure_exposer.jwtproxy.cpu_limit") String cpuLimitCores,
      @Named("che.workspace.sidecar.image_pull_policy") String imagePullPolicy,
      @Assisted RuntimeIdentity identity)
      throws InternalInfrastructureException {
    super(
        constructKeyPair(signatureKeyManager, identity),
        jwtProxyConfigBuilderFactory,
        serviceExposureStrategyProvider.get(),
        serviceExposureStrategyProvider.getMultiHostStrategy(),
        cookiePathStrategy,
        multiHostCookiePathStrategy,
        jwtProxyImage,
        memoryRequestBytes,
        memoryLimitBytes,
        cpuRequestCores,
        cpuLimitCores,
        imagePullPolicy,
        identity.getWorkspaceId(),
        true);
  }

  private static KeyPair constructKeyPair(
      SignatureKeyManager signatureKeyManager, RuntimeIdentity identity)
      throws InternalInfrastructureException {
    try {
      return signatureKeyManager.getOrCreateKeyPair(identity.getWorkspaceId());
    } catch (SignatureKeyManagerException e) {
      throw new InternalInfrastructureException(
          "Signature key pair for machine authentication cannot be retrieved. Reason: "
              + e.getMessage());
    }
  }

  @Override
  protected ExposureConfiguration getExposureConfiguration(ServerConfig serverConfig) {
    return new ExposureConfiguration(serverConfig);
  }
}
