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

import org.eclipse.che.ide.extension.machine.client.command.CommandConfiguration;
import org.eclipse.che.ide.extension.machine.client.command.CommandType;

import java.util.Map;

/**
 * Represents command for compiling class files and starts a Java application.
 * It does this by starting a Java runtime environment, loading a specified class, and calling that class's main method.
 *
 * @author Valeriy Svydenko
 */
public class JavaCommandConfiguration extends CommandConfiguration {

    private String project;
    private String mainClass;
    private String mainClassFqn;
    private String commandLine;

    protected JavaCommandConfiguration(CommandType type,
                                       String name,
                                       Map<String, String> attributes) {
        super(type, name, attributes);
        project = "";
        mainClass = "";
        commandLine = "";
        mainClassFqn = "";
    }

    /** Returns a path to the project. */
    public String getProject() {
        return project;
    }

    /** Sets a path to the project. */
    public void setProject(String workingDirectory) {
        this.project = workingDirectory;
    }

    /** Returns command line. */
    public String getCommandLine() {
        return commandLine;
    }

    /** Sets command line. */
    public void setCommandLine(String commandLine) {
        this.commandLine = commandLine;
    }

    /** Returns a path to the Main class. */
    public String getMainClass() {
        return mainClass;
    }

    /** Returns a fqn of the Main class. */
    public String getMainClassFqn() {
        return mainClassFqn;
    }

    /** Sets fqn of the Main class. */
    public void setMainClassFqn(String mainClassFqn) {
        this.mainClassFqn = mainClassFqn;
    }

    /** Sets path to the Main class. */
    public void setMainClass(String mainClass) {
        this.mainClass = mainClass;
    }

    @Override
    public String toCommandLine() {
        final StringBuilder cmd = new StringBuilder("");
        if (!commandLine.trim().isEmpty()) {
            cmd.append(commandLine.trim());
        }
        return cmd.toString();
    }

}
