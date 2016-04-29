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
package org.eclipse.che.plugin.maven.client.command;

import org.eclipse.che.api.machine.shared.dto.CommandDto;
import org.eclipse.che.ide.CommandLine;
import org.eclipse.che.ide.extension.machine.client.command.CommandConfigurationFactory;
import org.eclipse.che.ide.extension.machine.client.command.CommandType;

import javax.validation.constraints.NotNull;

/**
 * Factory for {@link MavenCommandConfiguration} instances.
 *
 * @author Artem Zatsarynnyi
 */
public class MavenCommandConfigurationFactory extends CommandConfigurationFactory<MavenCommandConfiguration> {

    protected MavenCommandConfigurationFactory(@NotNull CommandType commandType) {
        super(commandType);
    }

    private static boolean isMavenCommand(String commandLine) {
        return commandLine.startsWith("mvn");
    }

    @NotNull
    @Override
    public MavenCommandConfiguration createFromDto(@NotNull CommandDto descriptor) {
        if (!isMavenCommand(descriptor.getCommandLine())) {
            throw new IllegalArgumentException("Not a valid Maven command: " + descriptor.getCommandLine());
        }

        final MavenCommandConfiguration configuration =
                new MavenCommandConfiguration(getCommandType(), descriptor.getName(), descriptor.getAttributes());

        final CommandLine cmd = new CommandLine(descriptor.getCommandLine());

        if (cmd.hasArgument("-f")) {
            final int index = cmd.indexOf("-f");
            final String workDir = cmd.getArgument(index + 1);
            configuration.setWorkingDirectory(workDir);

            cmd.removeArgument("-f");
            cmd.removeArgument(workDir);
        }

        cmd.removeArgument("mvn");
        configuration.setCommandLine(cmd.toString());

        return configuration;
    }
}
