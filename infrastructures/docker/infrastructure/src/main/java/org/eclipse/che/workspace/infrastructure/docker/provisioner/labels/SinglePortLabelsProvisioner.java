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
package org.eclipse.che.workspace.infrastructure.docker.provisioner.labels;

import static java.lang.String.format;

import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.environment.InternalMachineConfig;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerContainerConfig;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerEnvironment;
import org.eclipse.che.workspace.infrastructure.docker.provisioner.ConfigurationProvisioner;
import org.eclipse.che.workspace.infrastructure.docker.server.mapping.SinglePortHostnameBuilder;

/**
 * Sets necessary container labels for the single-port proxy (Traefik).
 *
 * @author Max Shaposhnik (mshaposh@redhat.com)
 */
public class SinglePortLabelsProvisioner implements ConfigurationProvisioner {

  private final SinglePortHostnameBuilder hostnameBuilder;

  @Inject
  public SinglePortLabelsProvisioner(
      @Nullable @Named("che.docker.ip.external") String externalIpOfContainers,
      @Named("che.docker.ip") String internalIpOfContainers) {
    this.hostnameBuilder =
        new SinglePortHostnameBuilder(externalIpOfContainers, internalIpOfContainers);
  }

  @Override
  public void provision(DockerEnvironment internalEnv, RuntimeIdentity identity)
      throws InfrastructureException {
    for (Map.Entry<String, InternalMachineConfig> machineEntry :
        internalEnv.getMachines().entrySet()) {
      String machineName = machineEntry.getKey();
      DockerContainerConfig dockerConfig = internalEnv.getContainers().get(machineName);
      Map<String, String> containerLabels = new HashMap<>();
      for (Map.Entry<String, ServerConfig> serverEntry :
          machineEntry.getValue().getServers().entrySet()) {

        final String serverName = serverEntry.getKey().replace('/', '-');
        final String host =
            "Host:" + hostnameBuilder.build(serverName, machineName, identity.getWorkspaceId());
        final String serviceName = machineName + "-" + serverName;
        final String port = serverEntry.getValue().getPort().split("/")[0];

        containerLabels.put(format("traefik.%s.port", serviceName), port);
        containerLabels.put(format("traefik.%s.frontend.entryPoints", serviceName), "http");
        containerLabels.put(format("traefik.%s.frontend.rule", serviceName), host);
        // Needed to activate per-service rules
        containerLabels.put("traefik.frontend.rule", machineName);
      }
      dockerConfig.getLabels().putAll(containerLabels);
    }
  }
}
