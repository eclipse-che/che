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
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"name", "actions"})
public class Command {

  @JsonProperty("name")
  private String name;

  @JsonProperty("actions")
  private List<Action> actions = null;

  @JsonProperty("attributes")
  private Map<String, String> attributes = new HashMap<>();

  @JsonProperty("name")
  public String getName() {
    return name;
  }

  @JsonProperty("name")
  public void setName(String name) {
    this.name = name;
  }

  public Command withName(String name) {
    this.name = name;
    return this;
  }

  @JsonProperty("actions")
  public List<Action> getActions() {
    return actions;
  }

  @JsonProperty("actions")
  public void setActions(List<Action> actions) {
    this.actions = actions;
  }

  public Command withActions(List<Action> actions) {
    this.actions = actions;
    return this;
  }

  @JsonAnyGetter
  public Map<String, String> getAttributes() {
    return this.attributes;
  }

  @JsonAnySetter
  public void getAttribute(String name, String value) {
    this.attributes.put(name, value);
  }

  public Command withAttributes(Map<String, String> attributes) {
    this.attributes = attributes;
    return this;
  }
}
