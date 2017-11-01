/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.docker.environment;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.stream.Collectors.toList;
import static org.eclipse.che.workspace.infrastructure.docker.ArgumentsValidator.checkArgument;

import com.google.common.base.Joiner;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.api.workspace.server.WsAgentMachineFinderUtil;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalMachineConfig;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerContainerConfig;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerEnvironment;

/** @author Alexander Garagatyi */
public class EnvironmentValidator {
  /* machine name must contain only {a-zA-Z0-9_-} characters and it's needed for validation machine names */
  private static final String MACHINE_NAME_REGEXP = "[a-zA-Z0-9]+[a-zA-Z0-9_-]*";
  private static final Pattern MACHINE_NAME_PATTERN =
      Pattern.compile("^" + MACHINE_NAME_REGEXP + "$");
  private static final Pattern SERVER_PORT = Pattern.compile("^[1-9]+[0-9]*(/(tcp|udp))?$");
  private static final Pattern SERVER_PROTOCOL = Pattern.compile("^[a-z][a-z0-9-+.]*$");

  // DockerContainer syntax patterns
  /**
   * Examples:
   *
   * <ul>
   *   <li>8080/tcp
   *   <li>8080/udp
   *   <li>8080
   *   <li>8/tcp
   *   <li>8
   * </ul>
   */
  private static final Pattern EXPOSE_PATTERN = Pattern.compile("^[1-9]+[0-9]*(/(tcp|udp))?$");
  /**
   * Examples:
   *
   * <ul>
   *   <li>service1
   *   <li>service1:alias1
   * </ul>
   */
  private static final Pattern LINK_PATTERN =
      Pattern.compile(
          "^(?<containerName>" + MACHINE_NAME_REGEXP + ")(:" + MACHINE_NAME_REGEXP + ")?$");

  private static final Pattern VOLUME_FROM_PATTERN =
      Pattern.compile("^(?<containerName>" + MACHINE_NAME_REGEXP + ")(:(ro|rw))?$");

  public void validate(
      Map<String, InternalMachineConfig> machines, DockerEnvironment dockerEnvironment)
      throws ValidationException, InfrastructureException {
    checkArgument(
        dockerEnvironment.getContainers() != null && !dockerEnvironment.getContainers().isEmpty(),
        "Environment should contain at least 1 container");

    checkArgument(
        machines != null && !machines.isEmpty(), "Environment should contain at least 1 machine");

    List<String> missingContainers =
        machines
            .keySet()
            .stream()
            .filter(machineName -> !dockerEnvironment.getContainers().containsKey(machineName))
            .collect(toList());
    checkArgument(
        missingContainers.isEmpty(),
        "Environment contains machines that are missing in environment recipe: %s",
        Joiner.on(", ").join(missingContainers));

    List<String> machinesWithWsagentServer =
        machines
            .entrySet()
            .stream()
            .filter(
                entry ->
                    WsAgentMachineFinderUtil.containsWsAgentServerOrInstaller(entry.getValue()))
            .map(Map.Entry::getKey)
            .collect(toList());

    checkArgument(
        machinesWithWsagentServer.size() == 1,
        "Environment should contain exactly 1 machine with wsagent, but contains '%s'. "
            + "All machines with this agent: %s",
        machinesWithWsagentServer.size(),
        Joiner.on(", ").join(machinesWithWsagentServer));

    // needed to validate different kinds of dependencies in containers to other containers
    Set<String> containersNames = dockerEnvironment.getContainers().keySet();

    for (Map.Entry<String, DockerContainerConfig> entry :
        dockerEnvironment.getContainers().entrySet()) {
      validateMachine(
          entry.getKey(), machines.get(entry.getKey()), entry.getValue(), containersNames);
    }
  }

  private void validateMachine(
      String machineName,
      @Nullable InternalMachineConfig machineConfig,
      DockerContainerConfig container,
      Set<String> containersNames)
      throws ValidationException {

    checkArgument(
        MACHINE_NAME_PATTERN.matcher(machineName).matches(),
        "Name of machine '%s' in environment is invalid",
        machineName);

    checkArgument(
        !isNullOrEmpty(container.getImage())
            || (container.getBuild() != null
                && (!isNullOrEmpty(container.getBuild().getContext())
                    || !isNullOrEmpty(container.getBuild().getDockerfileContent()))),
        "Field 'image' or 'build.context' is required in machine '%s' in environment",
        machineName);

    checkArgument(
        container.getBuild() == null
            || (isNullOrEmpty(container.getBuild().getContext())
                != isNullOrEmpty(container.getBuild().getDockerfileContent())),
        "Machine '%s' in environment contains mutually exclusive dockerfile content and build context.",
        machineName);

    if (machineConfig != null) {
      validateExtendedMachine(machineConfig, machineName);
    }

    for (String expose : container.getExpose()) {
      checkArgument(
          EXPOSE_PATTERN.matcher(expose).matches(),
          "Exposed port '%s' in machine '%s' in environment is invalid",
          expose,
          machineName);
    }

    for (String link : container.getLinks()) {
      Matcher matcher = LINK_PATTERN.matcher(link);

      checkArgument(
          matcher.matches(),
          "Link '%s' in machine '%s' in environment is invalid",
          link,
          machineName);

      String containerFromLink = matcher.group("containerName");
      checkArgument(
          !machineName.equals(containerFromLink),
          "Container '%s' has illegal link to itself",
          machineName);
      checkArgument(
          containersNames.contains(containerFromLink),
          "Machine '%s' in environment contains link to non existing machine '%s'",
          machineName,
          containerFromLink);
    }

    for (String depends : container.getDependsOn()) {
      checkArgument(
          MACHINE_NAME_PATTERN.matcher(depends).matches(),
          "Dependency '%s' in machine '%s' in environment is invalid",
          depends,
          machineName);

      checkArgument(
          !machineName.equals(depends),
          "Container '%s' has illegal dependency to itself",
          machineName);
      checkArgument(
          containersNames.contains(depends),
          "Machine '%s' in environment contains dependency to non existing machine '%s'",
          machineName,
          depends);
    }

    for (String volumesFrom : container.getVolumesFrom()) {
      Matcher matcher = VOLUME_FROM_PATTERN.matcher(volumesFrom);

      checkArgument(
          matcher.matches(),
          "Machine name '%s' in field 'volumes_from' of machine '%s' in environment is invalid",
          volumesFrom,
          machineName);

      String containerFromVolumesFrom = matcher.group("containerName");
      checkArgument(
          !machineName.equals(containerFromVolumesFrom),
          "Container '%s' can not mount volume from itself",
          machineName);
      checkArgument(
          containersNames.contains(containerFromVolumesFrom),
          "Machine '%s' in environment contains non existing machine '%s' in 'volumes_from' field",
          machineName,
          containerFromVolumesFrom);
    }

    checkArgument(
        container.getPorts() == null || container.getPorts().isEmpty(),
        "Ports binding is forbidden but found in machine '%s' of environment",
        machineName);

    checkArgument(
        container.getVolumes() == null || container.getVolumes().isEmpty(),
        "Volumes binding is forbidden but found in machine '%s' of environment",
        machineName);

    checkArgument(
        container.getNetworks() == null || container.getNetworks().isEmpty(),
        "Networks configuration is forbidden but found in machine '%s' of environment",
        machineName);
  }

  private void validateExtendedMachine(InternalMachineConfig machineConfig, String machineName)
      throws ValidationException {

    if (machineConfig.getServers() != null) {
      for (Map.Entry<String, ? extends ServerConfig> serverEntry :
          machineConfig.getServers().entrySet()) {
        String serverName = serverEntry.getKey();
        ServerConfig server = serverEntry.getValue();

        checkArgument(
            server.getPort() != null && SERVER_PORT.matcher(server.getPort()).matches(),
            "Machine '%s' in environment contains server conf '%s' with invalid port '%s'",
            machineName,
            serverName,
            server.getPort());
        checkArgument(
            server.getProtocol() == null || SERVER_PROTOCOL.matcher(server.getProtocol()).matches(),
            "Machine '%s' in environment contains server conf '%s' with invalid protocol '%s'",
            machineName,
            serverName,
            server.getProtocol());
      }
    }
  }
}
