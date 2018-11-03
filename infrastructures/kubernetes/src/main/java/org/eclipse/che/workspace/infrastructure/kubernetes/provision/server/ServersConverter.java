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
package org.eclipse.che.workspace.infrastructure.kubernetes.provision.server;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodSpec;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.environment.InternalMachineConfig;
import org.eclipse.che.workspace.infrastructure.kubernetes.Names;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.ConfigurationProvisioner;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.KubernetesServerExposer;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.external.ExternalServerExposerStrategy;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.secure.SecureServerExposer;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.secure.SecureServerExposerFactoryProvider;

/**
 * Converts {@link ServerConfig} to Kubernetes related objects to add a server into Kubernetes
 * runtime.
 *
 * <p>Adds Kubernetes objects by calling {@link KubernetesServerExposer#expose(Map)} on each machine
 * with servers.
 *
 * @author Alexander Garagatyi
 */
@Singleton
public class ServersConverter<T extends KubernetesEnvironment>
    implements ConfigurationProvisioner<T> {

  private final ExternalServerExposerStrategy<T> externalServerExposerStrategy;
  private final SecureServerExposerFactoryProvider<T> secureServerExposerFactoryProvider;

  @Inject
  public ServersConverter(
      ExternalServerExposerStrategy<T> externalServerExposerStrategy,
      SecureServerExposerFactoryProvider<T> secureServerExposerFactoryProvider) {
    this.externalServerExposerStrategy = externalServerExposerStrategy;
    this.secureServerExposerFactoryProvider = secureServerExposerFactoryProvider;
  }

  @Override
  public void provision(T k8sEnv, RuntimeIdentity identity) throws InfrastructureException {
    SecureServerExposer<T> secureServerExposer =
        secureServerExposerFactoryProvider.get(k8sEnv).create(identity);

    for (Pod podConfig : k8sEnv.getPods().values()) {
      final PodSpec podSpec = podConfig.getSpec();
      for (Container containerConfig : podSpec.getContainers()) {
        String machineName = Names.machineName(podConfig, containerConfig);
        InternalMachineConfig machineConfig = k8sEnv.getMachines().get(machineName);
        if (!machineConfig.getServers().isEmpty()) {
          KubernetesServerExposer kubernetesServerExposer =
              new KubernetesServerExposer<>(
                  externalServerExposerStrategy,
                  secureServerExposer,
                  machineName,
                  podConfig,
                  containerConfig,
                  k8sEnv);
          kubernetesServerExposer.expose(machineConfig.getServers());
        }
      }
    }
  }
}
