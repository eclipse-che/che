/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.devfile.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"version", "name", "projects", "tools", "commands"})
public class DevFile {

  @JsonProperty("version")
  private String version;

  @JsonProperty("name")
  private String name;

  @JsonProperty("projects")
  private List<Project> projects = null;

  @JsonProperty("tools")
  private List<Tool> tools = null;

  @JsonProperty("commands")
  private List<Command> commands = null;

  @JsonProperty("version")
  public String getVersion() {
    return version;
  }

  @JsonProperty("version")
  public void setVersion(String version) {
    this.version = version;
  }

  public DevFile withVersion(String version) {
    this.version = version;
    return this;
  }

  @JsonProperty("name")
  public String getName() {
    return name;
  }

  @JsonProperty("name")
  public void setName(String name) {
    this.name = name;
  }

  public DevFile withName(String name) {
    this.name = name;
    return this;
  }

  @JsonProperty("projects")
  public List<Project> getProjects() {
    return projects;
  }

  @JsonProperty("projects")
  public void setProjects(List<Project> projects) {
    this.projects = projects;
  }

  public DevFile withProjects(List<Project> projects) {
    this.projects = projects;
    return this;
  }

  @JsonProperty("tools")
  public List<Tool> getTools() {
    return tools;
  }

  @JsonProperty("tools")
  public void setTools(List<Tool> tools) {
    this.tools = tools;
  }

  public DevFile withTools(List<Tool> tools) {
    this.tools = tools;
    return this;
  }

  @JsonProperty("commands")
  public List<Command> getCommands() {
    return commands;
  }

  @JsonProperty("commands")
  public void setCommands(List<Command> commands) {
    this.commands = commands;
  }

  public DevFile withCommands(List<Command> commands) {
    this.commands = commands;
    return this;
  }
}
