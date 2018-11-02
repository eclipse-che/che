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
@JsonPropertyOrder({"tool", "action"})
public class ToolsCommand {

  @JsonProperty("tool")
  private String tool;

  @JsonProperty("action")
  private Action action;

  @JsonIgnore private Map<String, Object> additionalProperties = new HashMap<>();

  @JsonProperty("tool")
  public String getTool() {
    return tool;
  }

  @JsonProperty("tool")
  public void setTool(String tool) {
    this.tool = tool;
  }

  @JsonProperty("action")
  public Action getAction() {
    return action;
  }

  @JsonProperty("action")
  public void setAction(Action action) {
    this.action = action;
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
