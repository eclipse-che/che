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
package org.eclipse.che.workspace.infrastructure.kubernetes.server.secure.jwtproxy;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.assistedinject.Assisted;
import javax.inject.Inject;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.external.ExternalServerExposer;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.external.ExternalServerExposerProvider;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.secure.DefaultSecureServerExposer;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.secure.jwtproxy.factory.PassThroughProxyProvisionerFactory;

/**
 * Exposes secure servers with JWTProxy.
 *
 * <p>To expose secure servers it provisions JwtProxy objects into environment with {@link
 * PassThroughProxyProvisioner}. Then JwtProxy service port is made public accessible by {@link
 * ExternalServerExposer <T>}.
 *
 * <p>In this way, requests to exposed secure servers will be routed via JwtProxy pod that is added
 * one per workspace.
 *
 * @see PassThroughProxyProvisioner
 */
public class PassThroughProxySecureServerExposer<T extends KubernetesEnvironment>
    extends DefaultSecureServerExposer<T> {

  @VisibleForTesting
  PassThroughProxySecureServerExposer(
      PassThroughProxyProvisioner passThroughProxyProvisioner, ExternalServerExposer<T> exposer) {
    super(passThroughProxyProvisioner, exposer);
  }

  @Inject
  public PassThroughProxySecureServerExposer(
      @Assisted RuntimeIdentity identity,
      PassThroughProxyProvisionerFactory jwtProxyProvisionerFactory,
      ExternalServerExposerProvider<T> exposer) {
    super(identity, jwtProxyProvisionerFactory, exposer);
  }
}
