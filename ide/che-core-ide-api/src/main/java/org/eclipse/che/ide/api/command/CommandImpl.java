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
package org.eclipse.che.ide.api.command;

import org.eclipse.che.api.core.model.machine.Command;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * Model of the command.
 *
 * @author Artem Zatsarynnyi
 */
public class CommandImpl implements Command {

    private final String              type;
    private       String              name;
    private       String              commandLine;
    private       Map<String, String> attributes;

    /**
     * Creates new command of the specified type with the given name and command line.
     *
     * @param name
     *         command name
     * @param commandLine
     *         command line
     * @param type
     *         type of the command
     */
    public CommandImpl(String name, String commandLine, String type) {
        this(name, commandLine, type, Collections.<String, String>emptyMap());
    }

    public CommandImpl(String name, String commandLine, String type, Map<String, String> attributes) {
        this.name = name;
        this.commandLine = commandLine;
        this.type = type;
        this.attributes = attributes;
    }

    /** Creates copy of the given {@link Command}. */
    public CommandImpl(Command command) {
        this(command.getName(),
             command.getCommandLine(),
             command.getType(),
             command.getAttributes());
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getCommandLine() {
        return commandLine;
    }

    public void setCommandLine(String commandLine) {
        this.commandLine = commandLine;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof CommandImpl)) {
            return false;
        }

        CommandImpl other = (CommandImpl)o;

        return Objects.equals(getName(), other.getName())
               && Objects.equals(type, other.type)
               && Objects.equals(commandLine, other.commandLine)
               && Objects.equals(getAttributes(), other.getAttributes());
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type, commandLine, getAttributes());
    }
}
