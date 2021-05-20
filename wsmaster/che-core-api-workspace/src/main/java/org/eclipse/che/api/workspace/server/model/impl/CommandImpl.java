/*
 * Copyright (c) 2012-2019 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.workspace.server.model.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyColumn;
import javax.persistence.Table;
import org.eclipse.che.api.core.model.workspace.config.Command;
import org.eclipse.che.api.core.model.workspace.devfile.PreviewUrl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.PreviewUrlImpl;

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

  @Embedded private PreviewUrlImpl previewUrl;

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

  public CommandImpl(
      String name,
      String commandLine,
      String type,
      PreviewUrlImpl previewUrl,
      Map<String, String> attributes) {
    this.name = name;
    this.commandLine = commandLine;
    this.type = type;
    this.previewUrl = previewUrl;
    this.attributes = new HashMap<>(attributes);
  }

  public CommandImpl(Command command) {
    this.name = command.getName();
    this.commandLine = command.getCommandLine();
    this.type = command.getType();
    if (command.getPreviewUrl() != null) {
      this.previewUrl = new PreviewUrlImpl(command.getPreviewUrl());
    }
    this.attributes = new HashMap<>(command.getAttributes());
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

  public PreviewUrlImpl getPreviewUrl() {
    return previewUrl;
  }

  public void setPreviewUrl(PreviewUrl previewUrl) {
    if (previewUrl != null) {
      this.previewUrl = new PreviewUrlImpl(previewUrl);
    } else {
      this.previewUrl = null;
    }
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
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CommandImpl command = (CommandImpl) o;
    return Objects.equals(id, command.id)
        && Objects.equals(name, command.name)
        && Objects.equals(commandLine, command.commandLine)
        && Objects.equals(type, command.type)
        && Objects.equals(previewUrl, command.previewUrl)
        && Objects.equals(attributes, command.attributes);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, commandLine, type, previewUrl, attributes);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", CommandImpl.class.getSimpleName() + "[", "]")
        .add("id=" + id)
        .add("name='" + name + "'")
        .add("commandLine='" + commandLine + "'")
        .add("type='" + type + "'")
        .add("previewUrl=" + previewUrl)
        .add("attributes=" + attributes)
        .toString();
  }
}
