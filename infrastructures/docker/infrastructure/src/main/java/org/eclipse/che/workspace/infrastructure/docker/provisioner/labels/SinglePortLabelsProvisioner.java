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

import static java.lang.Boolean.parseBoolean;
import static java.lang.String.format;
import static org.eclipse.che.api.core.model.workspace.config.ServerConfig.INTERNAL_SERVER_ATTRIBUTE;

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
  private final String internalIpOfContainers;
  private final String externalIpOfContainers;
  private final String dockerNetwork;

  @Inject
  public SinglePortLabelsProvisioner(
      @Nullable @Named("che.docker.ip") String internalIpOfContainers,
      @Nullable @Named("che.docker.ip.external") String externalIpOfContainers,
      @Nullable @Named("che.docker.network") String dockerNetwork,
      @Nullable @Named("che.singleport.wildcard_domain.host") String wildcardHost) {
    if (internalIpOfContainers == null && externalIpOfContainers == null) {
      throw new IllegalStateException(
          "Value of both of the properties 'che.docker.ip' and 'che.docker.ip.external' is null,"
              + " which is unsuitable for the single-port mode");
    }
    this.hostnameBuilder =
        new SinglePortHostnameBuilder(externalIpOfContainers, internalIpOfContainers, wildcardHost);
    this.internalIpOfContainers = internalIpOfContainers;
    this.externalIpOfContainers = externalIpOfContainers;
    this.dockerNetwork = dockerNetwork;
  }

  @Override
  public void provision(DockerEnvironment internalEnv, RuntimeIdentity identity)
      throws InfrastructureException {
    for (Map.Entry<String, InternalMachineConfig> machineEntry :
        internalEnv.getMachines().entrySet()) {
      final String machineName = machineEntry.getKey();
      Map<String, String> containerLabels = new HashMap<>();
      for (Map.Entry<String, ServerConfig> serverEntry :
          machineEntry.getValue().getServers().entrySet()) {
        // skip internal servers
        if (parseBoolean(serverEntry.getValue().getAttributes().get(INTERNAL_SERVER_ATTRIBUTE))) {
          continue;
        }
        final String host =
            hostnameBuilder.build(serverEntry.getKey(), machineName, identity.getWorkspaceId());
        final String serviceName = getServiceName(host);
        final String port = serverEntry.getValue().getPort().split("/")[0];

        containerLabels.put(format("traefik.%s.port", serviceName), port);
        containerLabels.put(format("traefik.%s.frontend.entryPoints", serviceName), "http");
        containerLabels.put(format("traefik.%s.frontend.rule", serviceName), "Host:" + host);
        // Needed to activate per-service rules
        containerLabels.put("traefik.frontend.rule", machineName);
      }
      // To prevent gateway timeouts in multiuser mode
      if (dockerNetwork != null) {
        containerLabels.put("traefik.docker.network", dockerNetwork);
      }
      DockerContainerConfig dockerConfig = internalEnv.getContainers().get(machineName);
      dockerConfig.getLabels().putAll(containerLabels);
    }
  }

  /**
   * Constructs unique traefik service name - contains server, machine names and workspace ID. Dots
   * is not allowed and replaced by dashes. Result is like:
   * exec-agent-http-dev-machine-workspaceao6k83hkdav975g5
   */
  private String getServiceName(String host) {
    int idx =
        (externalIpOfContainers != null && host.contains(externalIpOfContainers))
            ? host.indexOf(externalIpOfContainers)
            : host.indexOf(internalIpOfContainers);
    return host.substring(0, idx - 1).replaceAll("\\.", "-");
  }
}
