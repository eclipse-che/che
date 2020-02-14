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
package org.eclipse.che.workspace.infrastructure.kubernetes.server.secure;

import static org.eclipse.che.commons.lang.NameGenerator.generate;
import static org.eclipse.che.workspace.infrastructure.kubernetes.Constants.CHE_ORIGINAL_NAME_LABEL;
import static org.eclipse.che.workspace.infrastructure.kubernetes.server.KubernetesServerExposer.SERVER_PREFIX;
import static org.eclipse.che.workspace.infrastructure.kubernetes.server.KubernetesServerExposer.SERVER_UNIQUE_PART_SIZE;

import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServicePort;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import javax.inject.Inject;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment.PodData;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.ServerServiceBuilder;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.external.ExternalServerExposer;

/**
 * Default implementation of {@link SecureServerExposerFactory} that creates instances of {@link
 * SecureServerExposer} that exposes secure servers as usual external server without setting
 * authentication layer.
 *
 * @author Sergii Leshchenko
 */
public class DefaultSecureServersFactory<T extends KubernetesEnvironment>
    implements SecureServerExposerFactory<T> {
  private final ExternalServerExposer<T> exposer;

  @Inject
  public DefaultSecureServersFactory(ExternalServerExposer<T> exposer) {
    this.exposer = exposer;
  }

  @Override
  public SecureServerExposer<T> create(RuntimeIdentity runtimeId) {
    return new DefaultSecureServerExposer();
  }

  private class DefaultSecureServerExposer implements SecureServerExposer<T> {

    @Override
    public Optional<Service> createService(
        Collection<ServicePort> allSecurePorts,
        PodData pod,
        String machineName,
        Map<String, ? extends ServerConfig> secureServers) {
      return Optional.of(
          new ServerServiceBuilder()
              .withName(generate(SERVER_PREFIX, SERVER_UNIQUE_PART_SIZE) + '-' + machineName)
              .withMachineName(machineName)
              .withSelectorEntry(CHE_ORIGINAL_NAME_LABEL, pod.getMetadata().getName())
              .withPorts(new ArrayList<>(allSecurePorts))
              .withServers(secureServers)
              .build());
    }

    @Override
    public void expose(
        T k8sEnv,
        PodData pod,
        String machineName,
        String serviceName,
        String serverId,
        ServicePort servicePort,
        Map<String, ServerConfig> secureServers) {
      exposer.expose(k8sEnv, machineName, serviceName, serverId, servicePort, secureServers);
    }
  }
}
