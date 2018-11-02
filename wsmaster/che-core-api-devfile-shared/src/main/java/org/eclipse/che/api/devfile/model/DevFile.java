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

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"specVersion", "name", "projects", "tools", "commands"})
public class DevFile {

  @JsonProperty("specVersion")
  private String specVersion;

  @JsonProperty("name")
  private String name;

  @JsonProperty("projects")
  private List<Project> projects = null;

  @JsonProperty("tools")
  private List<Tool> tools = null;

  @JsonProperty("commands")
  private List<Command> commands = null;

  @JsonIgnore private Map<String, Object> additionalProperties = new HashMap<>();

  @JsonProperty("specVersion")
  public String getSpecVersion() {
    return specVersion;
  }

  @JsonProperty("specVersion")
  public void setSpecVersion(String specVersion) {
    this.specVersion = specVersion;
  }

  @JsonProperty("name")
  public String getName() {
    return name;
  }

  @JsonProperty("name")
  public void setName(String name) {
    this.name = name;
  }

  @JsonProperty("projects")
  public List<Project> getProjects() {
    return projects;
  }

  @JsonProperty("projects")
  public void setProjects(List<Project> projects) {
    this.projects = projects;
  }

  @JsonProperty("tools")
  public List<Tool> getTools() {
    return tools;
  }

  @JsonProperty("tools")
  public void setTools(List<Tool> tools) {
    this.tools = tools;
  }

  @JsonProperty("commands")
  public List<Command> getCommands() {
    return commands;
  }

  @JsonProperty("commands")
  public void setCommands(List<Command> commands) {
    this.commands = commands;
  }

  @JsonAnyGetter
  public Map<String, Object> getAdditionalProperties() {
    return this.additionalProperties;
  }

  @JsonAnySetter
  public void setAdditionalProperty(String name, Object value) {
    this.additionalProperties.put(name, value);
  }
}
