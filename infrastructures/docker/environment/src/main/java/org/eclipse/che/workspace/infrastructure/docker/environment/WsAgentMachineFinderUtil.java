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
package org.eclipse.che.workspace.infrastructure.docker.environment;

import static java.lang.String.format;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.eclipse.che.api.core.model.workspace.config.MachineConfig;
import org.eclipse.che.api.core.model.workspace.runtime.Machine;
import org.eclipse.che.api.installer.server.impl.InstallerFqn;
import org.eclipse.che.api.workspace.server.spi.environment.InternalEnvironment;
import org.eclipse.che.api.workspace.server.spi.environment.InternalMachineConfig;
import org.eclipse.che.api.workspace.shared.Constants;

/**
 * Helps to find machine that has WsAgent server or installer.
 *
 * @author Alexander Garagatyi
 */
public class WsAgentMachineFinderUtil {
  public static final String WS_AGENT_INSTALLER = "org.eclipse.che.ws-agent";

  private WsAgentMachineFinderUtil() {}

  /**
   * Finds machine in provided environment which contains wsagent server.
   *
   * @param environment environment to find a machine with the wsagent server
   * @return {@link Optional} with name of the machine which contains wsagent server or empty if
   *     such machine is not present in provided environment
   * @throws IllegalArgumentException if more than 1 machine with the wsagent server are found
   */
  public static Optional<String> getWsAgentServerMachine(InternalEnvironment environment) {

    List<String> machinesWithWsAgentServer =
        environment
            .getMachines()
            .entrySet()
            .stream()
            .filter(entry -> containsWsAgentServer(entry.getValue()))
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());

    if (machinesWithWsAgentServer.isEmpty()) {
      return Optional.empty();
    }
    if (machinesWithWsAgentServer.size() == 1) {
      return Optional.of(machinesWithWsAgentServer.get(0));
    }
    throw new IllegalArgumentException(
        format(
            "Environment contains '%s' machines with wsagent server. Machines names: '%s'",
            machinesWithWsAgentServer.size(), machinesWithWsAgentServer));
  }

  /**
   * Checks whether provided {@link InternalMachineConfig} contains wsagent server.
   *
   * @param machineConfig machine config to check
   * @return true when wsagent server is found in provided machine, false otherwise
   */
  public static boolean containsWsAgentServer(InternalMachineConfig machineConfig) {
    return machineConfig.getServers().keySet().contains(Constants.SERVER_WS_AGENT_HTTP_REFERENCE);
  }

  /**
   * Checks whether provided {@link MachineConfig} contains wsagent server.
   *
   * @param machine machine to check
   * @return true when wsagent server is found in provided machine, false otherwise
   */
  public static boolean containsWsAgentServer(Machine machine) {
    return machine.getServers().keySet().contains(Constants.SERVER_WS_AGENT_HTTP_REFERENCE);
  }

  /**
   * Checks whether provided {@link InternalMachineConfig} contains wsagent installer or server.
   *
   * @param machineConfig machine config to check
   * @return true when wsagent server or installer is found in provided machine, false otherwise
   */
  public static boolean containsWsAgentServerOrInstaller(InternalMachineConfig machineConfig) {
    return machineConfig.getServers().keySet().contains(Constants.SERVER_WS_AGENT_HTTP_REFERENCE)
        || InstallerFqn.idInInstallerList(WS_AGENT_INSTALLER, machineConfig.getInstallers());
  }

  /**
   * Checks whether provided {@link MachineConfig} contains wsagent installer or server.
   *
   * @param machineConfig machine config to check
   * @return true when wsagent server or installer is found in provided machine, false otherwise
   */
  public static boolean containsWsAgentServerOrInstaller(MachineConfig machineConfig) {
    return machineConfig.getServers().keySet().contains(Constants.SERVER_WS_AGENT_HTTP_REFERENCE)
        || InstallerFqn.idInKeyList(WS_AGENT_INSTALLER, machineConfig.getInstallers());
  }
}
