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

  @JsonProperty("command")
  public String getCommand() {
    return command;
  }

  @JsonProperty("command")
  public void setCommand(String command) {
    this.command = command;
  }

  public Action withCommand(String command) {
    this.command = command;
    return this;
  }

  @JsonProperty("workdir")
  public String getWorkdir() {
    return workdir;
  }

  @JsonProperty("workdir")
  public void setWorkdir(String workdir) {
    this.workdir = workdir;
  }

  public Action withWorkdir(String workdir) {
    this.workdir = workdir;
    return this;
  }

  @JsonProperty("type")
  public String getType() {
    return type;
  }

  @JsonProperty("type")
  public void setType(String type) {
    this.type = type;
  }

  public Action withType(String type) {
    this.type = type;
    return this;
  }

  @JsonProperty("tool")
  public String getTool() {
    return tool;
  }

  @JsonProperty("tool")
  public void setTool(String tool) {
    this.tool = tool;
  }

  public Action withTool(String tool) {
    this.tool = tool;
    return this;
  }
}
