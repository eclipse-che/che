/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.workspace.infrastructure.docker.environment;

import com.google.common.base.Joiner;

import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.core.model.workspace.config.Environment;
import org.eclipse.che.api.core.model.workspace.config.MachineConfig;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerEnvironment;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerContainerConfig;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static org.eclipse.che.workspace.infrastructure.docker.ArgumentsValidator.checkArgument;

/**
 * @author Alexander Garagatyi
 */
public class EnvironmentValidator {
    /* machine name must contain only {a-zA-Z0-9_-} characters and it's needed for validation machine names */
    private static final String  MACHINE_NAME_REGEXP  = "[a-zA-Z0-9_-]+";
    private static final Pattern MACHINE_NAME_PATTERN = Pattern.compile("^" + MACHINE_NAME_REGEXP + "$");
    private static final Pattern SERVER_PORT          = Pattern.compile("^[1-9]+[0-9]*(/(tcp|udp))?$");
    private static final Pattern SERVER_PROTOCOL      = Pattern.compile("^[a-z][a-z0-9-+.]*$");

    // DockerService syntax patterns
    /**
     * Examples:
     * <ul>
     * <li>8080/tcp</li>
     * <li>8080/udp</li>
     * <li>8080</li>
     * <li>8/tcp</li>
     * <li>8</li>
     * </ul>
     */
    private static final Pattern EXPOSE_PATTERN = Pattern.compile("^[1-9]+[0-9]*(/(tcp|udp))?$");
    /**
     * Examples:
     * <ul>
     * <li>service1</li>
     * <li>service1:alias1</li>
     * </ul>
     */
    private static final Pattern LINK_PATTERN   =
            Pattern.compile("^(?<serviceName>" + MACHINE_NAME_REGEXP + ")(:" + MACHINE_NAME_REGEXP + ")?$");

    private static final Pattern VOLUME_FROM_PATTERN =
            Pattern.compile("^(?<serviceName>" + MACHINE_NAME_REGEXP + ")(:(ro|rw))?$");

    public void validate(Environment env, DockerEnvironment dockerEnvironment) throws ValidationException,
                                                                                      InfrastructureException {
        checkArgument(dockerEnvironment.getServices() != null && !dockerEnvironment.getServices().isEmpty(),
                      "Environment should contain at least 1 machine");

        checkArgument(env.getMachines() != null && !env.getMachines().isEmpty(),
                      "Environment doesn't contain machine with 'org.eclipse.che.ws-agent' agent");

        List<String> missingServices = env.getMachines()
                                          .keySet()
                                          .stream()
                                          .filter(machineName -> !dockerEnvironment.getServices()
                                                                                   .containsKey(machineName))
                                          .collect(toList());
        checkArgument(missingServices.isEmpty(),
                      "Environment 'contains machines that are missing in environment recipe: %s",
                      Joiner.on(", ").join(missingServices));

        List<String> devMachines = env.getMachines()
                                      .entrySet()
                                      .stream()
                                      .filter(entry -> entry.getValue().getAgents() != null &&
                                                       entry.getValue().getAgents()
                                                            .contains("org.eclipse.che.ws-agent"))
                                      .map(Map.Entry::getKey)
                                      .collect(toList());

        checkArgument(devMachines.size() == 1,
                      "Environment should contain exactly 1 machine with agent 'org.eclipse.che.ws-agent', but contains '%s'. " +
                      "All machines with this agent: %s",
                      devMachines.size(), Joiner.on(", ").join(devMachines));

        // needed to validate different kinds of dependencies in services to other services
        Set<String> servicesNames = dockerEnvironment.getServices().keySet();

        for (Map.Entry<String, DockerContainerConfig> entry : dockerEnvironment.getServices().entrySet()) {
            validateMachine(entry.getKey(),
                            env.getMachines().get(entry.getKey()),
                            entry.getValue(),
                            servicesNames);
        }
    }

    private void validateMachine(String machineName,
                                 @Nullable MachineConfig machineConfig,
                                 DockerContainerConfig service,
                                 Set<String> servicesNames) throws ValidationException {
        checkArgument(MACHINE_NAME_PATTERN.matcher(machineName).matches(),
                      "Name of machine '%s' in environment is invalid",
                      machineName);

        checkArgument(!isNullOrEmpty(service.getImage()) ||
                      (service.getBuild() != null && (!isNullOrEmpty(service.getBuild().getContext()) ||
                                                      !isNullOrEmpty(service.getBuild().getDockerfileContent()))),
                      "Field 'image' or 'build.context' is required in machine '%s' in environment",
                      machineName);

        checkArgument(service.getBuild() == null || (isNullOrEmpty(service.getBuild().getContext()) !=
                                                     isNullOrEmpty(service.getBuild().getDockerfileContent())),
                      "Machine '%s' in environment contains mutually exclusive dockerfile content and build context.",
                      machineName);

        if (machineConfig != null) {
            validateExtendedMachine(machineConfig, machineName);
        }

        for (String expose : service.getExpose()) {
            checkArgument(EXPOSE_PATTERN.matcher(expose).matches(),
                          "Exposed port '%s' in machine '%s' in environment is invalid",
                          expose, machineName);
        }

        for (String link : service.getLinks()) {
            Matcher matcher = LINK_PATTERN.matcher(link);

            checkArgument(matcher.matches(),
                          "Link '%s' in machine '%s' in environment is invalid",
                          link, machineName);

            String serviceFromLink = matcher.group("serviceName");
            checkArgument(servicesNames.contains(serviceFromLink),
                          "Machine '%s' in environment contains link to non existing machine '%s'",
                          machineName, serviceFromLink);
        }

        for (String depends : service.getDependsOn()) {
            checkArgument(MACHINE_NAME_PATTERN.matcher(depends).matches(),
                          "Dependency '%s' in machine '%s' in environment is invalid",
                          depends, machineName);

            checkArgument(servicesNames.contains(depends),
                          "Machine '%s' in environment contains dependency to non existing machine '%s'",
                          machineName, depends);
        }

        for (String volumesFrom : service.getVolumesFrom()) {
            Matcher matcher = VOLUME_FROM_PATTERN.matcher(volumesFrom);

            checkArgument(matcher.matches(),
                          "Machine name '%s' in field 'volumes_from' of machine '%s' in environment is invalid",
                          volumesFrom, machineName);

            String serviceFromVolumesFrom = matcher.group("serviceName");
            checkArgument(servicesNames.contains(serviceFromVolumesFrom),
                          "OldMachine '%s' in environment contains non existing machine '%s' in 'volumes_from' field",
                          machineName, serviceFromVolumesFrom);
        }

        checkArgument(service.getPorts() == null || service.getPorts().isEmpty(),
                      "Ports binding is forbidden but found in machine '%s' of environment",
                      machineName);

        checkArgument(service.getVolumes() == null || service.getVolumes().isEmpty(),
                      "Volumes binding is forbidden but found in machine '%s' of environment",
                      machineName);

        checkArgument(service.getNetworks() == null || service.getNetworks().isEmpty(),
                      "Networks configuration is forbidden but found in machine '%s' of environment",
                      machineName);
    }

    private void validateExtendedMachine(MachineConfig machineConfig, String machineName) throws ValidationException {
        if (machineConfig.getAttributes() != null &&
            machineConfig.getAttributes().get("memoryLimitBytes") != null) {

            try {
                long memoryLimitBytes = Long.parseLong(machineConfig.getAttributes().get("memoryLimitBytes"));
                checkArgument(memoryLimitBytes > 0,
                              "Value of attribute 'memoryLimitBytes' of machine '%s' in environment is illegal",
                              machineName);
            } catch (NumberFormatException e) {
                throw new ValidationException(
                        format("Value of attribute 'memoryLimitBytes' of machine '%s' in environment is illegal",
                               machineName));
            }
        }

        if (machineConfig.getServers() != null) {
            for (Map.Entry<String, ? extends ServerConfig> serverEntry : machineConfig.getServers().entrySet()) {
                String serverName = serverEntry.getKey();
                ServerConfig server = serverEntry.getValue();

                checkArgument(server.getPort() != null && SERVER_PORT.matcher(server.getPort()).matches(),
                              "Machine '%s' in environment contains server conf '%s' with invalid port '%s'",
                              machineName, serverName, server.getPort());
                checkArgument(server.getProtocol() == null ||
                              SERVER_PROTOCOL.matcher(server.getProtocol()).matches(),
                              "Machine '%s' in environment contains server conf '%s' with invalid protocol '%s'",
                              machineName, serverName, server.getProtocol());
            }
        }

        if (machineConfig.getAgents() != null) {
            for (String agent : machineConfig.getAgents()) {
                checkArgument(!isNullOrEmpty(agent),
                              "Machine '%s' in environment contains invalid agent '%s'",
                              machineName, agent);
            }
        }

    }
}
