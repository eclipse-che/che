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
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"type", "tool", "command", "workdir"})
public class Action {

  @JsonProperty("tool")
  private String tool;

  @JsonProperty("type")
  private String type;

  @JsonProperty("command")
  private String command;

  @JsonProperty("workdir")
  private String workdir;

  @JsonIgnore private Map<String, String> additionalProperties = new HashMap<>();

  @JsonProperty("command")
  public String getCommand() {
    return command;
  }

  @JsonProperty("command")
  public void setCommand(String command) {
    this.command = command;
  }

  @JsonProperty("workdir")
  public String getWorkdir() {
    return workdir;
  }

  @JsonProperty("workdir")
  public void setWorkdir(String workdir) {
    this.workdir = workdir;
  }

  @JsonProperty("type")
  public String getType() {
    return type;
  }

  @JsonProperty("type")
  public void setType(String type) {
    this.type = type;
  }

  @JsonProperty("tool")
  public String getTool() {
    return tool;
  }

  @JsonProperty("tool")
  public void setTool(String tool) {
    this.tool = tool;
  }

  @JsonAnyGetter
  public Map<String, String> getAdditionalProperties() {
    return this.additionalProperties;
  }

  @JsonAnySetter
  public void setAdditionalProperty(String name, String value) {
    this.additionalProperties.put(name, value);
  }
}
