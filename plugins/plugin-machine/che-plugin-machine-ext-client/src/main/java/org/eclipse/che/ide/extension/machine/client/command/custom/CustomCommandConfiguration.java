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

import org.eclipse.che.ide.extension.machine.client.command.CommandConfiguration;
import org.eclipse.che.ide.extension.machine.client.command.CommandType;

import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * Represents command that is defined by arbitrary command line.
 *
 * @author Artem Zatsarynnyi
 */
public class CustomCommandConfiguration extends CommandConfiguration {

    private String commandLine;

    protected CustomCommandConfiguration(CommandType type, String name, Map<String, String> attributes) {
        super(type, name, attributes);
        commandLine = "";
    }

    public String getCommandLine() {
        return commandLine;
    }

    public void setCommandLine(String commandLine) {
        this.commandLine = commandLine;
    }

    @NotNull
    @Override
    public String toCommandLine() {
        return getCommandLine();
    }
}
