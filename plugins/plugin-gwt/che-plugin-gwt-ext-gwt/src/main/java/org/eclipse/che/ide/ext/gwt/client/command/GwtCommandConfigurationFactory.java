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
package org.eclipse.che.ide.ext.gwt.client.command;

import org.eclipse.che.api.machine.shared.dto.CommandDto;
import org.eclipse.che.ide.CommandLine;
import org.eclipse.che.ide.extension.machine.client.command.CommandConfigurationFactory;
import org.eclipse.che.ide.extension.machine.client.command.CommandType;

import javax.validation.constraints.NotNull;

/**
 * Factory for {@link GwtCommandConfiguration} instances.
 *
 * @author Artem Zatsarynnyi
 */
public class GwtCommandConfigurationFactory extends CommandConfigurationFactory<GwtCommandConfiguration> {

    protected GwtCommandConfigurationFactory(@NotNull CommandType commandType) {
        super(commandType);
    }

    private static boolean isGwtCommand(String commandLine) {
        return commandLine.startsWith(GwtCommandType.COMMAND_TEMPLATE);
    }

    @NotNull
    @Override
    public GwtCommandConfiguration createFromDto(@NotNull CommandDto descriptor) {
        if (!isGwtCommand(descriptor.getCommandLine())) {
            throw new IllegalArgumentException("Not a valid GWT command: " + descriptor.getCommandLine());
        }

        final GwtCommandConfiguration configuration =
                new GwtCommandConfiguration(getCommandType(), descriptor.getName(), descriptor.getAttributes());
        final CommandLine cmd = new CommandLine(descriptor.getCommandLine());

        if (cmd.hasArgument("-f")) {
            final int index = cmd.indexOf("-f");
            final String workDir = cmd.getArgument(index + 1);
            configuration.setWorkingDirectory(workDir);
        }

        for (String arg : cmd.getArguments()) {
            if (arg.startsWith("-Dgwt.module=")) {
                configuration.setGwtModule(arg.split("=")[1]);
            } else if (arg.startsWith("-Dgwt.bindAddress=")) {
                configuration.setCodeServerAddress(arg.split("=")[1]);
            }
        }

        return configuration;
    }
}
