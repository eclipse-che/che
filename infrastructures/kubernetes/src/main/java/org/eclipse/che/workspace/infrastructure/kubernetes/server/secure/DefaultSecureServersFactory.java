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
package org.eclipse.che.workspace.infrastructure.kubernetes.server.secure;

import io.fabric8.kubernetes.api.model.ServicePort;
import java.util.Map;
import javax.inject.Inject;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.external.ExternalServerExposerStrategy;

/**
 * Default implementation of {@link SecureServerExposerFactory} that creates instances of {@link
 * SecureServerExposer} that exposes secure servers as usual external server without setting
 * authentication layer.
 *
 * @author Sergii Leshchenko
 */
public class DefaultSecureServersFactory<T extends KubernetesEnvironment>
    implements SecureServerExposerFactory<T> {
  private final ExternalServerExposerStrategy<T> exposerStrategy;

  @Inject
  public DefaultSecureServersFactory(ExternalServerExposerStrategy<T> exposerStrategy) {
    this.exposerStrategy = exposerStrategy;
  }

  @Override
  public SecureServerExposer<T> create(RuntimeIdentity runtimeId) {
    return new DefaultSecureServerExposer();
  }

  private class DefaultSecureServerExposer implements SecureServerExposer<T> {
    @Override
    public void expose(
        T k8sEnv,
        String machineName,
        String serviceName,
        ServicePort servicePort,
        Map<String, ServerConfig> secureServers) {
      exposerStrategy.expose(k8sEnv, machineName, serviceName, servicePort, secureServers);
    }
  }
}
