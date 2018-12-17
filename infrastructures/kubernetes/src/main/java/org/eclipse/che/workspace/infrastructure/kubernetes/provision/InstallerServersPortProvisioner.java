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
package org.eclipse.che.workspace.infrastructure.kubernetes.provision;

import static java.util.stream.Collectors.toList;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.installer.server.model.impl.InstallerImpl;
import org.eclipse.che.api.installer.server.model.impl.InstallerServerConfigImpl;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalInfrastructureException;
import org.eclipse.che.api.workspace.server.spi.environment.InternalMachineConfig;
import org.eclipse.che.commons.annotation.Traced;
import org.eclipse.che.commons.lang.Pair;
import org.eclipse.che.commons.tracing.TracingTags;
import org.eclipse.che.workspace.infrastructure.kubernetes.Names;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment.PodData;

/**
 * Fixes installers servers ports if conflicts are present.
 *
 * <p>Installers fail to start if they provide servers which should be running on the same port.
 * Installers in different machines can conflict too since containers in a pod use shared ports. To
 * resolve such conflicts Infrastructure provides a free port for installer server by injection
 * environment variable.
 *
 * <p>The environment variable name has the following format
 * `CHE_SERVER_{NORMALISED_SERVER_NAME}_PORT`. Where `NORMALISED_SERVER_NAME` is a server name in
 * the upper case where all characters which are not Latin letters or digits replaced with `_`
 * symbol. So installers must respect the corresponding environment variables with the recommended
 * value for server port.
 *
 * @author Sergii Leshchenko
 */
public class InstallerServersPortProvisioner implements ConfigurationProvisioner {

  public static final String SERVER_PORT_ENV_FMT = "CHE_SERVER_%s_PORT";

  private final int minPort;
  private final int maxPort;

  @Inject
  public InstallerServersPortProvisioner(
      @Named("che.infra.kubernetes.installer_server_min_port") int minPort,
      @Named("che.infra.kubernetes.installer_server_max_port") int maxPort) {
    this.minPort = minPort;
    this.maxPort = maxPort;
  }

  @Override
  @Traced
  public void provision(KubernetesEnvironment k8sEnv, RuntimeIdentity identity)
      throws InfrastructureException {

    TracingTags.WORKSPACE_ID.set(identity::getWorkspaceId);

    for (PodData pod : k8sEnv.getPodsData().values()) {
      // it is needed to detect conflicts between all containers in a pod
      // because they use the same ports
      List<InternalMachineConfig> podMachines =
          pod.getSpec()
              .getContainers()
              .stream()
              .map(container -> k8sEnv.getMachines().get(Names.machineName(pod, container)))
              .collect(toList());

      fixInstallersPortsConflicts(podMachines);
    }
  }

  @VisibleForTesting
  void fixInstallersPortsConflicts(List<InternalMachineConfig> machinesConfigs)
      throws InfrastructureException {
    ServersPorts ports = new ServersPorts();

    for (InternalMachineConfig machineConfig : machinesConfigs) {
      for (InstallerImpl installer : machineConfig.getInstallers()) {
        Map<Integer, Collection<String>> port2ServerRefs = getServersRefsGroupedByPorts(installer);

        for (Entry<Integer, Collection<String>> portServersEntry : port2ServerRefs.entrySet()) {
          Integer port = portServersEntry.getKey();

          if (!ports.occupy(port)) {
            int newPort = ports.findFreePort();
            assignNewPort(portServersEntry.getValue(), newPort, machineConfig, installer);
          }
        }
      }
    }
  }

  @VisibleForTesting
  void assignNewPort(
      Collection<String> serversRefs,
      int newPort,
      InternalMachineConfig machineConfig,
      InstallerImpl installer) {

    for (String serverName : serversRefs) {
      ServerConfig serverConfig = installer.getServers().get(serverName);
      String protocol = getPortProtocol(serverConfig.getPort()).second;

      InstallerServerConfigImpl newServerConfig = new InstallerServerConfigImpl(serverConfig);

      newServerConfig.setPort(newPort + "/" + protocol);

      installer.getServers().put(serverName, newServerConfig);

      machineConfig.getServers().put(serverName, newServerConfig);

      // put environment variable for installer
      machineConfig.getEnv().put(getEnvName(serverName), Integer.toString(newPort));
    }
  }

  @VisibleForTesting
  Map<Integer, Collection<String>> getServersRefsGroupedByPorts(InstallerImpl installer) {
    Multimap<Integer, String> portToServerNames = ArrayListMultimap.create();

    for (Entry<String, ? extends ServerConfig> serverEntry : installer.getServers().entrySet()) {
      String serverName = serverEntry.getKey();
      ServerConfig serverConfig = serverEntry.getValue();

      Pair<Integer, String> portProtocol = getPortProtocol(serverConfig.getPort());

      portToServerNames.put(portProtocol.first, serverName);
    }

    return portToServerNames.asMap();
  }

  @VisibleForTesting
  String getEnvName(String serverName) {
    String serverNameEnv = serverName.replaceAll("\\W", "_");
    return String.format(SERVER_PORT_ENV_FMT, serverNameEnv.toUpperCase());
  }

  private Pair<Integer, String> getPortProtocol(String port) {
    String[] dividedPort = port.split("/");
    Integer portValue = Integer.parseInt(dividedPort[0]);
    String protocol = dividedPort[1];
    return Pair.of(portValue, protocol);
  }

  class ServersPorts {
    private Set<Integer> occupiedPorts;
    private int freePort;

    private ServersPorts() {
      this.occupiedPorts = new HashSet<>();
      this.freePort = minPort;
    }

    @VisibleForTesting
    ServersPorts(int initPortValue, Set<Integer> occupiedPorts) {
      this.occupiedPorts = occupiedPorts;
      this.freePort = initPortValue;
    }

    /**
     * Marks the specified port as occupied.
     *
     * <p>If the specified port is not already occupied <tt>true</tt> will be returned,
     * <tt>false</tt> otherwise.
     *
     * @return <tt>true</tt> if this set did not already contain the specified element
     */
    @VisibleForTesting
    boolean occupy(int port) {
      return occupiedPorts.add(port);
    }

    @VisibleForTesting
    int findFreePort() throws InternalInfrastructureException {
      int newPort;
      do {
        newPort = freePort++;
        if (newPort > maxPort) {
          throw new InternalInfrastructureException(
              String.format(
                  "There is no available port in configured ports range [%s, %s].",
                  minPort, maxPort));
        }
      } while (!occupiedPorts.add(newPort));

      return newPort;
    }

    @VisibleForTesting
    Set<Integer> getOccupiedPorts() {
      return occupiedPorts;
    }
  }
}
