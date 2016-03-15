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
package org.eclipse.che.ide.extension.maven.client.command;

import org.eclipse.che.ide.extension.machine.client.command.CommandConfiguration;
import org.eclipse.che.ide.extension.machine.client.command.CommandType;

import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * Represents Maven command.
 *
 * @author Artem Zatsarynnyi
 */
public class MavenCommandConfiguration extends CommandConfiguration {

    private String workingDirectory;
    private String commandLine;

    protected MavenCommandConfiguration(CommandType type, String name, Map<String, String> attributes) {
        super(type, name, attributes);
        workingDirectory = "";
        commandLine = "";
    }

    public String getWorkingDirectory() {
        return workingDirectory;
    }

    public void setWorkingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory;
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
        final StringBuilder cmd = new StringBuilder("mvn");
        if (!workingDirectory.trim().isEmpty()) {
            cmd.append(" -f ").append(workingDirectory.trim());
        }
        if (!commandLine.trim().isEmpty()) {
            cmd.append(' ').append(commandLine.trim());
        }
        return cmd.toString();
    }
}
