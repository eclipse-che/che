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
package org.eclipse.che.workspace.infrastructure.kubernetes.provision.server;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodSpec;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.environment.InternalMachineConfig;
import org.eclipse.che.workspace.infrastructure.kubernetes.Names;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.ConfigurationProvisioner;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.KubernetesServerExposer;

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
public class ServersConverter implements ConfigurationProvisioner {

  private final Map<String, String> ingressAnnotations;

  @Inject
  public ServersConverter(
      @Named("infra.kubernetes.ingress.annotations") Map<String, String> ingressAnnotations) {
    this.ingressAnnotations = ingressAnnotations;
  }

  @Override
  public void provision(KubernetesEnvironment k8sEnv, RuntimeIdentity identity)
      throws InfrastructureException {

    for (Pod podConfig : k8sEnv.getPods().values()) {
      final PodSpec podSpec = podConfig.getSpec();
      for (Container containerConfig : podSpec.getContainers()) {
        String machineName = Names.machineName(podConfig, containerConfig);
        InternalMachineConfig machineConfig = k8sEnv.getMachines().get(machineName);
        if (!machineConfig.getServers().isEmpty()) {
          KubernetesServerExposer kubernetesServerExposer =
              new KubernetesServerExposer<>(
                  ingressAnnotations, machineName, podConfig, containerConfig, k8sEnv);
          kubernetesServerExposer.expose(machineConfig.getServers());
        }
      }
    }
  }
}
