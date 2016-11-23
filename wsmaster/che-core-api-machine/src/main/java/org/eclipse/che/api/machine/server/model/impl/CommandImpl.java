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
package org.eclipse.che.api.machine.server.model.impl;

import org.eclipse.che.api.core.model.machine.Command;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyColumn;
import javax.persistence.Table;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Data object for {@link Command}.
 *
 * @author Eugene Voevodin
 */
@Entity(name = "Command")
@Table(name = "command")
public class CommandImpl implements Command {

    @Id
    @GeneratedValue
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "commandline", nullable = false, columnDefinition = "TEXT")
    private String commandLine;

    @Column(name = "type", nullable = false)
    private String type;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "command_attributes", joinColumns = @JoinColumn(name = "command_id"))
    @MapKeyColumn(name = "name")
    @Column(name = "value", columnDefinition = "TEXT")
    private Map<String, String> attributes;

    public CommandImpl() {}

    public CommandImpl(String name, String commandLine, String type) {
        this.name = name;
        this.commandLine = commandLine;
        this.type = type;
    }

    public CommandImpl(Command command) {
        this.name = command.getName();
        this.commandLine = command.getCommandLine();
        this.type = command.getType();
        this.attributes = command.getAttributes();
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

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public Map<String, String> getAttributes() {
        if (attributes == null) {
            attributes = new HashMap<>();
        }
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof CommandImpl)) {
            return false;
        }
        final CommandImpl command = (CommandImpl)obj;
        return Objects.equals(name, command.name) &&
               Objects.equals(commandLine, command.commandLine) &&
               Objects.equals(type, command.type) &&
               Objects.equals(getAttributes(), command.getAttributes());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + Objects.hashCode(name);
        hash = 31 * hash + Objects.hashCode(commandLine);
        hash = 31 * hash + Objects.hashCode(type);
        hash = 31 * hash + Objects.hashCode(getAttributes());
        return hash;
    }

    @Override
    public String toString() {
        return "CommandImpl{" +
               "name='" + name + '\'' +
               ", commandLine='" + commandLine + '\'' +
               ", type='" + type + '\'' +
               ", attributes=" + getAttributes() +
               '}';
    }
}
