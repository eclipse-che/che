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
package org.eclipse.che.ide.ext.java.client.command;

import org.eclipse.che.api.machine.shared.dto.CommandDto;
import org.eclipse.che.ide.CommandLine;
import org.eclipse.che.ide.extension.machine.client.command.CommandConfigurationFactory;
import org.eclipse.che.ide.extension.machine.client.command.CommandType;

import javax.validation.constraints.NotNull;

/**
 * Factory for {@link JavaCommandConfiguration} instances.
 *
 * @author Valeriy Svydenko
 */
public class JavaCommandConfigurationFactory extends CommandConfigurationFactory<JavaCommandConfiguration> {

    protected JavaCommandConfigurationFactory(CommandType commandType) {
        super(commandType);
    }

    @NotNull
    @Override
    public JavaCommandConfiguration createFromDto(@NotNull CommandDto descriptor) {
        final JavaCommandConfiguration configuration = new JavaCommandConfiguration(getCommandType(),
                                                                                    descriptor.getName(),
                                                                                    descriptor.getAttributes());

        final CommandLine cmd = new CommandLine(descriptor.getCommandLine());

        if (cmd.hasArgument("-d")) {
            int index = cmd.indexOf("-d");
            final String mainClass = cmd.getArgument(index + 2);
            configuration.setMainClass(mainClass);
            configuration.setMainClassFqn(cmd.getArgument(cmd.getArguments().size() - 1));
        }

        configuration.setCommandLine(descriptor.getCommandLine());
        return configuration;
    }
}
