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
package org.eclipse.che.api.workspace.server.model.impl;

import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.model.machine.Command;
import org.eclipse.che.api.core.model.machine.MachineConfig;
import org.eclipse.che.api.core.model.workspace.Environment;
import org.eclipse.che.api.core.model.workspace.WorkspaceConfig;
import org.eclipse.che.api.workspace.server.WorkspaceConfigValidator;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.regex.Pattern;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.String.format;

/**
 * @author Alexander Reshetnyak
 */
@Singleton
public class WorkspaceConfigValidatorImpl implements WorkspaceConfigValidator {
    /* should contain [3, 20] characters, first and last character is letter or digit, available characters {A-Za-z0-9.-_}*/
    private static final Pattern WS_NAME = Pattern.compile("[\\w][\\w\\.\\-]{1,18}[\\w]");

    @Inject
    public WorkspaceConfigValidatorImpl() {
    }

    @Override
    public void validate(WorkspaceConfig cfg) throws BadRequestException {
        validateWorkspaceName(cfg.getName());
        validateConfig(cfg);
    }

    @Override
    public void validateWithoutWorkspaceName(WorkspaceConfig cfg) throws BadRequestException {
        validateConfig(cfg);
    }

    @Override
    public void validateWorkspaceName(String workspace) throws BadRequestException {
        if (isNullOrEmpty(workspace)) {
            throw new BadRequestException("Workspace name should not be null or empty");
        }
        if (!WS_NAME.matcher(workspace).matches()) {
            throw new BadRequestException("Incorrect workspace name, it should be between 3 to 20 characters and may contain digits, " +
                                          "latin letters, underscores, dots, dashes and should start and end only with digits, " +
                                          "latin letters or underscores");
        }
    }

    private void validateConfig(WorkspaceConfig cfg) throws BadRequestException {
        //attributes
        for (String attributeName : cfg.getAttributes().keySet()) {
            //attribute name should not be empty and should not start with codenvy
            if (attributeName.trim().isEmpty() || attributeName.toLowerCase().startsWith("codenvy")) {
                throw new BadRequestException(format("Attribute name '%s' is not valid", attributeName));
            }
        }

        //environments
        requiredNotNull(cfg.getDefaultEnv(), "Workspace default environment name required");
        if (!cfg.getEnvironments().stream().anyMatch(env -> env.getName().equals(cfg.getDefaultEnv()))) {
            throw new BadRequestException("Workspace default environment configuration required");
        }
        for (Environment environment : cfg.getEnvironments()) {
            final String envName = environment.getName();
            requiredNotNull(envName, "Environment name should not be null");

            //machine configs
            if (environment.getMachineConfigs().isEmpty()) {
                throw new BadRequestException("Environment '" + envName + "' should contain at least 1 machine");
            }
            final long devCount = environment.getMachineConfigs()
                                             .stream()
                                             .filter(MachineConfig::isDev)
                                             .count();
            if (devCount != 1) {
                throw new BadRequestException(format("Environment should contain exactly 1 dev machine, but '%s' contains '%d'",
                                                     envName,
                                                     devCount));
            }
            for (MachineConfig machineCfg : environment.getMachineConfigs()) {
                if (isNullOrEmpty(machineCfg.getName())) {
                    throw new BadRequestException("Environment " + envName + " contains machine without of name");
                }
                requiredNotNull(machineCfg.getSource(), "Environment " + envName + " contains machine without of source");
                //TODO require type?
            }
        }

        //commands
        for (Command command : cfg.getCommands()) {
            requiredNotNull(command.getName(), "Workspace " + cfg.getName() + " contains command without of name");
            requiredNotNull(command.getCommandLine(), format("Command line required for command '%s' in workspace '%s'",
                                                             command.getName(),
                                                             cfg.getName()));
        }

        //projects
        //TODO
    }

    /**
     * Checks object reference is not {@code null}
     *
     * @param object
     *         object reference to check
     * @param message
     *         used as subject of exception message "{subject} required"
     * @throws org.eclipse.che.api.core.BadRequestException
     *         when object reference is {@code null}
     */
    private void requiredNotNull(Object object, String message) throws BadRequestException {
        if (object == null) {
            throw new BadRequestException(message);
        }
    }
}
