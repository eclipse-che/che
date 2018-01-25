package org.eclipse.che.plugin.traefik;

import static java.lang.String.format;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.environment.InternalMachineConfig;
import org.eclipse.che.workspace.infrastructure.docker.Labels;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerContainerConfig;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerEnvironment;
import org.eclipse.che.workspace.infrastructure.docker.provisioner.ConfigurationProvisioner;

/**
 * @author Max Shaposhnik (mshaposh@redhat.com)
 */
public class TraefikServerProvisioner implements ConfigurationProvisioner {

  @Override
  public void provision(DockerEnvironment internalEnv, RuntimeIdentity identity)
      throws InfrastructureException {
    for (Map.Entry<String, InternalMachineConfig> machineEntry : internalEnv.getMachines()
        .entrySet()) {
      String machineName = machineEntry.getKey();
      DockerContainerConfig dockerConfig = internalEnv.getContainers().get(machineName);
      Map<String, String> containerLabels = new HashMap<>();
      for (Map.Entry<String, ServerConfig> serverEntry : machineEntry.getValue().getServers()
          .entrySet()) {
        //Host should be in form: Host:<serverName>.<machineName>.<workspaceId>.<wildcardNipDomain>
        final String host = format("Host:%s", hostName);
        final String serviceName = machineName + "-" + serverEntry.getKey();
        containerLabels.put(format("traefik.%s.port", serviceName), serverEntry.getValue().getPort());
        containerLabels.put(format("traefik.%s.frontend.entryPoints", serviceName), "http");
        containerLabels.put(format("traefik.%s.frontend.rule", serviceName), host);
        containerLabels.put("traefik.frontend.rule", dockerConfig.getContainerName());

      }
      dockerConfig
          .getLabels()
          .putAll(containerLabels);
    }
  }
}
