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

import org.eclipse.che.api.core.model.machine.MachineConfig;
import org.eclipse.che.api.core.model.machine.ServerConf;
import org.eclipse.che.api.core.model.workspace.Environment;
import org.eclipse.che.api.machine.server.MachineInstanceProviders;

import javax.inject.Inject;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.regex.Pattern;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.String.format;

/**
 * Validates description of environment of workspace.
 *
 * @author Alexander Garagatyi
 */
public class CheEnvironmentValidator {
    /* machine name must contain only {a-zA-Z0-9_-} characters and it's needed for validation machine names */
    private static final Pattern MACHINE_NAME_PATTERN = Pattern.compile("^/?[a-zA-Z0-9_-]+$");
    private static final Pattern SERVER_PORT          = Pattern.compile("[1-9]+[0-9]*/(?:tcp|udp)");
    private static final Pattern SERVER_PROTOCOL      = Pattern.compile("[a-z][a-z0-9-+.]*");

    private final MachineInstanceProviders machineInstanceProviders;

    @Inject
    public CheEnvironmentValidator(MachineInstanceProviders machineInstanceProviders) {
        this.machineInstanceProviders = machineInstanceProviders;
    }

    public void validate(Environment env) throws IllegalArgumentException {
        String envName = env.getName();
        checkArgument(envName != null && !envName.isEmpty(),
                      "Environment name should not be neither null nor empty");
        checkArgument(env.getMachineConfigs() != null && !env.getMachineConfigs().isEmpty(),
                      "Environment '%s' should contain at least 1 machine",
                      envName);

        final long devCount = env.getMachineConfigs()
                                 .stream()
                                 .filter(MachineConfig::isDev)
                                 .count();
        checkArgument(devCount == 1,
                      "Environment '%s' should contain exactly 1 dev machine, but contains '%d'",
                      envName,
                      devCount);
        for (MachineConfig machineCfg : env.getMachineConfigs()) {
            validateMachine(machineCfg, envName);
        }
    }

    private void validateMachine(MachineConfig machineCfg, String envName) throws IllegalArgumentException {
        String machineName = machineCfg.getName();
        checkArgument(!isNullOrEmpty(machineName), "Environment '%s' contains machine with null or empty name", envName);
        checkArgument(MACHINE_NAME_PATTERN.matcher(machineName).matches(),
                      "Environment '%s' contains machine with invalid name '%s'", envName, machineName);
        checkNotNull(machineCfg.getSource(), "Machine '%s' in environment '%s' doesn't have source", machineName, envName);
        checkArgument(machineCfg.getSource().getContent() != null || machineCfg.getSource().getLocation() != null,
                      "Source of machine '%s' in environment '%s' must contain location or content", machineName, envName);
        checkArgument(machineCfg.getSource().getContent() == null || machineCfg.getSource().getLocation() == null,
                      "Source of machine '%s' in environment '%s' contains mutually exclusive fields location and content",
                      machineName, envName);
        checkArgument(machineInstanceProviders.hasProvider(machineCfg.getType()),
                      "Type '%s' of machine '%s' in environment '%s' is not supported. Supported values are: %s.",
                      machineCfg.getType(),
                      machineName,
                      envName,
                      Joiner.on(", ").join(machineInstanceProviders.getProviderTypes()));

        if (machineCfg.getSource().getType().equals("dockerfile") && machineCfg.getSource().getLocation() != null) {
            try {
                final String protocol = new URL(machineCfg.getSource().getLocation()).getProtocol();
                checkArgument(protocol.equals("http") || protocol.equals("https"),
                              "Environment '%s' contains machine '%s' with invalid source location protocol: %s",
                              envName,
                              machineName,
                              machineCfg.getSource().getLocation());
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException(format("Environment '%s' contains machine '%s' with invalid source location: '%s'",
                                                          envName,
                                                          machineName,
                                                          machineCfg.getSource().getLocation()));
            }
        }
        for (ServerConf serverConf : machineCfg.getServers()) {
            checkArgument(serverConf.getPort() != null && SERVER_PORT.matcher(serverConf.getPort()).matches(),
                          "Machine '%s' in environment '%s' contains server conf with invalid port '%s'",
                          machineName,
                          envName,
                          serverConf.getPort());
            checkArgument(serverConf.getProtocol() == null || SERVER_PROTOCOL.matcher(serverConf.getProtocol()).matches(),
                          "Machine '%s' in environment '%s' contains server conf with invalid protocol '%s'",
                          machineName,
                          envName,
                          serverConf.getProtocol());
        }
        for (Map.Entry<String, String> envVariable : machineCfg.getEnvVariables().entrySet()) {
            checkArgument(!isNullOrEmpty(envVariable.getKey()),
                          "Machine '%s' in environment '%s' contains environment variable with null or empty name",
                          machineName,
                          envName);
            checkNotNull(envVariable.getValue(),
                         "Machine '%s' in environment '%s' contains environment variable '%s' with null value",
                         machineName,
                         envName,
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

    /**
     * Checks that expression is true, throws {@link IllegalArgumentException} otherwise.
     *
     * <p>Exception uses error message built from error message template and error message parameters.
     */
    private static void checkArgument(boolean expression, String errorMessage) {
        if (!expression) {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    /**
     * Checks that expression is true, throws {@link IllegalArgumentException} otherwise.
     *
     * <p>Exception uses error message built from error message template and error message parameters.
     */
    private static void checkArgument(boolean expression, String errorMessageTemplate, Object... errorMessageParams)
            throws IllegalArgumentException {
        if (!expression) {
            throw new IllegalArgumentException(format(errorMessageTemplate, errorMessageParams));
        }
    }
}
