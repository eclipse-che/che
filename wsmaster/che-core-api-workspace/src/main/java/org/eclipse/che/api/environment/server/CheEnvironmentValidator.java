/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.environment.server;

import com.google.common.base.Joiner;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.machine.MachineConfig;
import org.eclipse.che.api.core.model.machine.ServerConf;
import org.eclipse.che.api.core.model.workspace.Environment;
import org.eclipse.che.api.core.model.workspace.ExtendedMachine;
import org.eclipse.che.api.core.model.workspace.ServerConf2;
import org.eclipse.che.api.core.model.workspace.compose.ComposeService;
import org.eclipse.che.api.environment.server.compose.ComposeServicesStartStrategy;
import org.eclipse.che.api.environment.server.compose.model.ComposeEnvironmentImpl;
import org.eclipse.che.api.machine.server.MachineInstanceProviders;
import org.eclipse.che.commons.annotation.Nullable;

import javax.inject.Inject;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

/**
 * Validates description of environment of workspace.
 *
 * @author Alexander Garagatyi
 */
public class CheEnvironmentValidator {
    /* machine name must contain only {a-zA-Z0-9_-} characters and it's needed for validation machine names */
    private static final String  MACHINE_NAME_REGEXP  = "[a-zA-Z0-9_-]+";
    private static final Pattern MACHINE_NAME_PATTERN = Pattern.compile("^" + MACHINE_NAME_REGEXP + "$");
    private static final Pattern SERVER_PORT          = Pattern.compile("^[1-9]+[0-9]*(/(tcp|udp))?$");
    private static final Pattern SERVER_PROTOCOL      = Pattern.compile("^[a-z][a-z0-9-+.]*$");

    // Compose syntax patterns
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

    private final MachineInstanceProviders     machineInstanceProviders;
    private final EnvironmentParser            environmentParser;
    private final ComposeServicesStartStrategy startStrategy;

    @Inject
    public CheEnvironmentValidator(MachineInstanceProviders machineInstanceProviders,
                                   EnvironmentParser environmentParser,
                                   ComposeServicesStartStrategy startStrategy) {
        this.machineInstanceProviders = machineInstanceProviders;
        this.environmentParser = environmentParser;
        this.startStrategy = startStrategy;
    }

    public void validate(String envName, Environment env) throws IllegalArgumentException,
                                                                 ServerException {
        checkArgument(!isNullOrEmpty(envName),
                      "Environment name should not be neither null nor empty");
        checkNotNull(env.getRecipe(), "Environment recipe should not be null");
        checkArgument(environmentParser.getEnvironmentTypes().contains(env.getRecipe().getType()),
                      "Type '%s' of environment '%s' is not supported. Supported types: %s",
                      env.getRecipe().getType(),
                      envName,
                      Joiner.on(',').join(environmentParser.getEnvironmentTypes()));
        checkArgument(env.getRecipe().getContent() != null || env.getRecipe().getLocation() != null,
                      "Recipe of environment '%s' must contain location or content", envName);
        checkArgument(env.getRecipe().getContent() == null || env.getRecipe().getLocation() == null,
                      "Recipe of environment '%s' contains mutually exclusive fields location and content",
                      envName);

        ComposeEnvironmentImpl composeEnvironment;
        try {
            composeEnvironment = environmentParser.parse(env);
        } catch (ServerException e) {
            throw new ServerException(format("Parsing of recipe of environment '%s' failed. Error: %s",
                                             envName, e.getLocalizedMessage()));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(format("Parsing of recipe of environment '%s' failed. Error: %s",
                                                      envName, e.getLocalizedMessage()));
        }

        checkArgument(composeEnvironment.getServices() != null && !composeEnvironment.getServices().isEmpty(),
                      "Environment '%s' should contain at least 1 machine",
                      envName);

        checkArgument(env.getMachines() != null && !env.getMachines().isEmpty(),
                      "Environment '%s' doesn't contain machine with 'ws-agent' agent",
                      envName);

        List<String> missingServices = env.getMachines()
                                          .keySet()
                                          .stream()
                                          .filter(machineName -> !composeEnvironment.getServices()
                                                                                    .containsKey(machineName))
                                          .collect(toList());
        checkArgument(missingServices.isEmpty(),
                      "Environment '%s' contains machines that are missing in environment recipe: %s",
                      envName, Joiner.on(", ").join(missingServices));

        List<String> devMachines = env.getMachines()
                                      .entrySet()
                                      .stream()
                                      .filter(entry -> entry.getValue()
                                                            .getAgents()
                                                            .contains("ws-agent"))
                                      .map(Map.Entry::getKey)
                                      .collect(toList());

        checkArgument(devMachines.size() == 1,
                      "Environment '%s' should contain exactly 1 machine with ws-agent, but contains '%s'. " +
                      "All machines with this agent: %s",
                      envName, devMachines.size(), Joiner.on(", ").join(devMachines));

        // needed to validate different kinds of dependencies in services to other services
        Set<String> servicesNames = composeEnvironment.getServices().keySet();

        composeEnvironment.getServices()
                          .forEach((serviceName, service) -> validateMachine(serviceName,
                                                                             env.getMachines().get(serviceName),
                                                                             service,
                                                                             envName,
                                                                             servicesNames));

        // check that order can be resolved
        try {
            startStrategy.order(composeEnvironment);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    format("Start order of machine in environment '%s' is not resolvable. Error: %s",
                           envName, e.getLocalizedMessage()));
        }
    }

    protected void validateMachine(String machineName,
                                   @Nullable ExtendedMachine extendedMachine,
                                   ComposeService service,
                                   String envName,
                                   Set<String> servicesNames) throws IllegalArgumentException {
        checkArgument(MACHINE_NAME_PATTERN.matcher(machineName).matches(),
                      "Name of machine '%s' in environment '%s' is invalid",
                      machineName, envName);

        // TODO remove workaround with dockerfile content in context.dockerfile
        checkArgument(!isNullOrEmpty(service.getImage()) ||
                      (service.getBuild() != null && (!isNullOrEmpty(service.getBuild().getContext()) ||
                                                      !isNullOrEmpty(service.getBuild().getDockerfile()))),
                      "Field 'image' or 'build.context' is required in machine '%s' in environment '%s'",
                      machineName, envName);

        if (extendedMachine.getAttributes() != null &&
            extendedMachine.getAttributes().get("memoryLimitBytes") != null) {

            try {
                long memoryLimitBytes = Long.parseLong(extendedMachine.getAttributes().get("memoryLimitBytes"));
                checkArgument(memoryLimitBytes > 0,
                              "Value of attribute 'memoryLimitBytes' of machine '%s' in environment '%s' is illegal",
                              machineName, envName);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                        format("Value of attribute 'memoryLimitBytes' of machine '%s' in environment '%s' is illegal",
                               machineName, envName));
            }
        }

        if (extendedMachine.getServers() != null) {
            extendedMachine.getServers()
                           .entrySet()
                           .forEach(serverEntry -> {
                               String serverName = serverEntry.getKey();
                               ServerConf2 server = serverEntry.getValue();

                               checkArgument(server.getPort() != null && SERVER_PORT.matcher(server.getPort()).matches(),
                                             "Machine '%s' in environment '%s' contains server conf '%s' with invalid port '%s'",
                                             machineName, envName, serverName, server.getPort());
                               checkArgument(server.getProtocol() == null || SERVER_PROTOCOL.matcher(server.getProtocol()).matches(),
                                             "Machine '%s' in environment '%s' contains server conf '%s' with invalid protocol '%s'",
                                             machineName, envName, serverName, server.getProtocol());
                           });
        }

        service.getExpose()
               .forEach(expose -> checkArgument(EXPOSE_PATTERN.matcher(expose).matches(),
                                                "Exposed port '%s' in machine '%s' in environment '%s' is invalid",
                                                expose, machineName, envName));

        service.getLinks()
               .forEach(link -> {
                   Matcher matcher = LINK_PATTERN.matcher(link);

                   checkArgument(matcher.matches(),
                                 "Link '%s' in machine '%s' in environment '%s' is invalid",
                                 link, machineName, envName);

                   String serviceFromLink = matcher.group("serviceName");
                   checkArgument(servicesNames.contains(serviceFromLink),
                                 "Machine '%s' in environment '%s' contains link to non existing machine '%s'",
                                 machineName, envName, serviceFromLink);
               });

        service.getDependsOn()
               .forEach(depends -> {
                   checkArgument(MACHINE_NAME_PATTERN.matcher(depends).matches(),
                                 "Dependency '%s' in machine '%s' in environment '%s' is invalid",
                                 depends, machineName, envName);

                   checkArgument(servicesNames.contains(depends),
                                 "Machine '%s' in environment '%s' contains dependency to non existing machine '%s'",
                                 machineName, envName, depends);
               });

        service.getVolumesFrom()
               .forEach(volumesFrom -> {
                   Matcher matcher = VOLUME_FROM_PATTERN.matcher(volumesFrom);

                   checkArgument(matcher.matches(),
                                 "Machine name '%s' in field 'volumes_from' of machine '%s' in environment '%s' is invalid",
                                 volumesFrom, machineName, envName);

                   String serviceFromVolumesFrom = matcher.group("serviceName");
                   checkArgument(servicesNames.contains(serviceFromVolumesFrom),
                                 "Machine '%s' in environment '%s' contains non existing machine '%s' in 'volumes_from' field",
                                 machineName, envName, serviceFromVolumesFrom);
               });

        checkArgument(service.getPorts() == null || service.getPorts().isEmpty(),
                      "Ports binding is forbidden but found in machine '%s' of environment '%s'",
                      machineName, envName);

        checkArgument(service.getVolumes() == null || service.getVolumes().isEmpty(),
                      "Volumes binding is forbidden but found in machine '%s' of environment '%s'",
                      machineName, envName);
    }

    public void validateMachine(MachineConfig machineCfg) throws IllegalArgumentException {
        String machineName = machineCfg.getName();
        checkArgument(!isNullOrEmpty(machineName), "Machine name is null or empty");
        checkArgument(MACHINE_NAME_PATTERN.matcher(machineName).matches(),
                      "Machine name '%s' is invalid", machineName);
        checkNotNull(machineCfg.getSource(), "Machine '%s' doesn't have source", machineName);
        checkArgument(machineCfg.getSource().getContent() != null || machineCfg.getSource().getLocation() != null,
                      "Source of machine '%s' must contain location or content", machineName);
        checkArgument(machineCfg.getSource().getContent() == null || machineCfg.getSource().getLocation() == null,
                      "Source of machine '%s' contains mutually exclusive fields location and content",
                      machineName);
        checkArgument(machineInstanceProviders.hasProvider(machineCfg.getType()),
                      "Type '%s' of machine '%s' is not supported. Supported values are: %s.",
                      machineCfg.getType(),
                      machineName,
                      Joiner.on(", ").join(machineInstanceProviders.getProviderTypes()));

        if (machineCfg.getSource().getType().equals("dockerfile") && machineCfg.getSource().getLocation() != null) {
            try {
                final String protocol = new URL(machineCfg.getSource().getLocation()).getProtocol();
                checkArgument(protocol.equals("http") || protocol.equals("https"),
                              "Machine '%s' has invalid source location protocol: %s",
                              machineName,
                              machineCfg.getSource().getLocation());
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException(format("Machine '%s' has invalid source location: '%s'",
                                                          machineName,
                                                          machineCfg.getSource().getLocation()));
            }
        }
        for (ServerConf serverConf : machineCfg.getServers()) {
            checkArgument(serverConf.getPort() != null && SERVER_PORT.matcher(serverConf.getPort()).matches(),
                          "Machine '%s' contains server conf with invalid port '%s'",
                          machineName,
                          serverConf.getPort());
            checkArgument(serverConf.getProtocol() == null || SERVER_PROTOCOL.matcher(serverConf.getProtocol()).matches(),
                          "Machine '%s' contains server conf with invalid protocol '%s'",
                          machineName,
                          serverConf.getProtocol());
        }
        for (Map.Entry<String, String> envVariable : machineCfg.getEnvVariables().entrySet()) {
            checkArgument(!isNullOrEmpty(envVariable.getKey()),
                          "Machine '%s' contains environment variable with null or empty name",
                          machineName);
            checkNotNull(envVariable.getValue(),
                         "Machine '%s' contains environment variable '%s' with null value",
                         machineName,
                         envVariable.getKey());
        }
    }

    /**
     * Checks that object reference is not null, throws {@link IllegalArgumentException} otherwise.
     *
     * <p>Exception uses error message built from error message template and error message parameters.
     */
    private static void checkNotNull(Object object, String errorMessageTemplate, Object... errorMessageParams) {
        if (object == null) {
            throw new IllegalArgumentException(format(errorMessageTemplate, errorMessageParams));
        }
    }
}
