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
package org.eclipse.che.ide.extension.machine.client.command.custom;

import org.eclipse.che.api.machine.shared.dto.CommandDto;
import org.eclipse.che.ide.extension.machine.client.command.CommandConfigurationFactory;
import org.eclipse.che.ide.extension.machine.client.command.CommandType;

import javax.validation.constraints.NotNull;

/**
 * Factory for {@link CustomCommandConfiguration} instances.
 *
 * @author Artem Zatsarynnyi
 */
public class CustomCommandConfigurationFactory extends CommandConfigurationFactory<CustomCommandConfiguration> {

    protected CustomCommandConfigurationFactory(@NotNull CommandType commandType) {
        super(commandType);
    }

    @NotNull
    @Override
    public CustomCommandConfiguration createFromDto(@NotNull CommandDto descriptor) {
        final CustomCommandConfiguration configuration =
                new CustomCommandConfiguration(getCommandType(), descriptor.getName(), descriptor.getAttributes());
        configuration.setCommandLine(descriptor.getCommandLine());
        return configuration;
    }
}
