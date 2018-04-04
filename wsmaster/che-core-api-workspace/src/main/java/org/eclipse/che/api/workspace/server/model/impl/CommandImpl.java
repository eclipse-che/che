/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.workspace.server.model.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
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
import org.eclipse.che.api.core.model.workspace.config.Command;

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
    final CommandImpl that = (CommandImpl) obj;
    return Objects.equals(id, that.id)
        && Objects.equals(name, that.name)
        && Objects.equals(commandLine, that.commandLine)
        && Objects.equals(type, that.type)
        && getAttributes().equals(that.getAttributes());
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 31 * hash + Objects.hashCode(id);
    hash = 31 * hash + Objects.hashCode(name);
    hash = 31 * hash + Objects.hashCode(commandLine);
    hash = 31 * hash + Objects.hashCode(type);
    hash = 31 * hash + getAttributes().hashCode();
    return hash;
  }

  @Override
  public String toString() {
    return "CommandImpl{"
        + "id="
        + id
        + ", name='"
        + name
        + '\''
        + ", commandLine='"
        + commandLine
        + '\''
        + ", type='"
        + type
        + '\''
        + ", attributes="
        + attributes
        + '}';
  }
}
