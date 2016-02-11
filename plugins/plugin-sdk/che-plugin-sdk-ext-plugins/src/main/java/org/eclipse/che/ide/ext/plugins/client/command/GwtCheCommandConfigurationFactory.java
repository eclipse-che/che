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
package org.eclipse.che.ide.ext.plugins.client.command;

import org.eclipse.che.api.machine.shared.dto.CommandDto;
import org.eclipse.che.ide.CommandLine;
import org.eclipse.che.ide.extension.machine.client.command.CommandConfigurationFactory;
import org.eclipse.che.ide.extension.machine.client.command.CommandType;

import javax.validation.constraints.NotNull;

import static org.eclipse.che.ide.ext.plugins.client.command.GwtCheCommandType.CODE_SERVER_FQN;

/**
 * Factory for {@link GwtCheCommandConfiguration} instances.
 *
 * @author Artem Zatsarynnyi
 */
public class GwtCheCommandConfigurationFactory extends CommandConfigurationFactory<GwtCheCommandConfiguration> {

    protected GwtCheCommandConfigurationFactory(@NotNull CommandType commandType) {
        super(commandType);
    }

    private static boolean isGwtCheCommand(String commandLine) {
        return commandLine.startsWith("java -classpath ") && commandLine.contains(CODE_SERVER_FQN);
    }

    @NotNull
    @Override
    public GwtCheCommandConfiguration createFromDto(@NotNull CommandDto descriptor) {
        if (!isGwtCheCommand(descriptor.getCommandLine())) {
            throw new IllegalArgumentException("Not a valid GWT4CHE command: " + descriptor.getCommandLine());
        }

        final GwtCheCommandConfiguration configuration =
                new GwtCheCommandConfiguration(getCommandType(), descriptor.getName(), descriptor.getAttributes());
        final CommandLine cmd = new CommandLine(descriptor.getCommandLine());

        final String classPathArgument = cmd.getArgument(2);
        // remove quotes
        configuration.setClassPath(classPathArgument.substring(1, classPathArgument.length() - 1));

        final int gwtModuleArgumentIndex = cmd.indexOf(CODE_SERVER_FQN) + 1;
        final String gwtModuleArgument = cmd.getArgument(gwtModuleArgumentIndex);
        configuration.setGwtModule(gwtModuleArgument);

        for (String arg : cmd.getArguments()) {
            if (arg.equals("-bindAddress")) {
                final int bindAddressArgumentIndex = cmd.indexOf(arg) + 1;
                configuration.setCodeServerAddress(cmd.getArgument(bindAddressArgumentIndex));
            }
        }

        return configuration;
    }
}
