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
@JsonPropertyOrder({"name", "source"})
public class Project {

  @JsonProperty("name")
  private String name;

  @JsonProperty("source")
  private Source source;

  @JsonProperty("name")
  public String getName() {
    return name;
  }

  @JsonProperty("name")
  public void setName(String name) {
    this.name = name;
  }

  public Project withName(String name) {
    this.name = name;
    return this;
  }

  @JsonProperty("source")
  public Source getSource() {
    return source;
  }

  @JsonProperty("source")
  public void setSource(Source source) {
    this.source = source;
  }

  public Project withSource(Source source) {
    this.source = source;
    return this;
  }
}
