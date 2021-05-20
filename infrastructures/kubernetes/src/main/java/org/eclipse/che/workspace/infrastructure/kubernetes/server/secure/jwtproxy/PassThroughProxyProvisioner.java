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

import static java.util.Collections.singletonList;

import com.google.inject.assistedinject.Assisted;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InternalInfrastructureException;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.external.ServiceExposureStrategyProvider;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.secure.jwtproxy.factory.JwtProxyConfigBuilderFactory;

/**
 * A Jwt Proxy provisioner that configures JWT proxy to just pass through all the traffic without
 * any authentication. Should be used in the single user mode only for "securing" the secure
 * servers.
 */
public class PassThroughProxyProvisioner extends AbstractJwtProxyProvisioner {

  @Inject
  public PassThroughProxyProvisioner(
      JwtProxyConfigBuilderFactory jwtProxyConfigBuilderFactory,
      ServiceExposureStrategyProvider serviceExposureStrategyProvider,
      CookiePathStrategy cookiePathStrategy,
      MultiHostCookiePathStrategy multiHostCookiePathStrategy,
      @Named("che.server.secure_exposer.jwtproxy.image") String jwtImage,
      @Named("che.server.secure_exposer.jwtproxy.memory_request") String memoryRequestBytes,
      @Named("che.server.secure_exposer.jwtproxy.memory_limit") String memoryLimitBytes,
      @Named("che.server.secure_exposer.jwtproxy.cpu_request") String cpuRequestCores,
      @Named("che.server.secure_exposer.jwtproxy.cpu_limit") String cpuLimitCores,
      @Named("che.workspace.sidecar.image_pull_policy") String imagePullPolicy,
      @Assisted RuntimeIdentity identity)
      throws InternalInfrastructureException {
    super(
        constructSignatureKeyPair(),
        jwtProxyConfigBuilderFactory,
        serviceExposureStrategyProvider.get(),
        serviceExposureStrategyProvider.getMultiHostStrategy(),
        cookiePathStrategy,
        multiHostCookiePathStrategy,
        jwtImage,
        memoryRequestBytes,
        memoryLimitBytes,
        cpuRequestCores,
        cpuLimitCores,
        imagePullPolicy,
        identity.getWorkspaceId(),
        false);
  }

  /**
   * Constructs a key pair to satisfy JWT proxy which needs a key pair in its configuration. In case
   * of pass-through proxy, this key pair is unused so we just generate a random one.
   *
   * @return a random key pair
   * @throws InternalInfrastructureException if RSA is not available as a key pair generator. This
   *     should not happen.
   */
  private static KeyPair constructSignatureKeyPair() throws InternalInfrastructureException {
    try {
      KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
      kpg.initialize(512);
      return kpg.generateKeyPair();
    } catch (NoSuchAlgorithmException e) {
      throw new InternalInfrastructureException(
          "Could not generate a fake key pair to support JWT proxy in single-user mode.");
    }
  }

  @Override
  protected ExposureConfiguration getExposureConfiguration(ServerConfig serverConfig) {
    // exclude everything on each server from JWT proxy auth, making it effectively a passthrough
    // proxy
    return new ExposureConfiguration(singletonList("/"), false);
  }
}
