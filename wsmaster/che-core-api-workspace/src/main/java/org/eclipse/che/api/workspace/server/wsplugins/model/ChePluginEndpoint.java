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
package org.eclipse.che.api.workspace.server.wsplugins.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a network endpoint that can be accessed by clients inside or outside of workspace.
 *
 * <p>Whether an endpoint is accessible from the outside of a workspace is defined by {@link
 * #isPublic()} method.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChePluginEndpoint {

  private String name = null;

  @JsonProperty("public")
  private boolean isPublic = false;

  private int targetPort = 0;
  private Map<String, String> attributes = new HashMap<>();

  public ChePluginEndpoint name(String name) {
    this.name = name;
    return this;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public boolean isPublic() {
    return isPublic;
  }

  public ChePluginEndpoint setPublic(boolean isPublic) {
    this.isPublic = isPublic;
    return this;
  }

  public ChePluginEndpoint targetPort(int targetPort) {
    this.targetPort = targetPort;
    return this;
  }

  public int getTargetPort() {
    return targetPort;
  }

  public void setTargetPort(int targetPort) {
    this.targetPort = targetPort;
  }

  public ChePluginEndpoint attributes(Map<String, String> attributes) {
    this.attributes = attributes;
    return this;
  }

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
    if (!(o instanceof ChePluginEndpoint)) {
      return false;
    }
    ChePluginEndpoint that = (ChePluginEndpoint) o;
    return isPublic() == that.isPublic()
        && Objects.equals(getName(), that.getName())
        && Objects.equals(getTargetPort(), that.getTargetPort())
        && Objects.equals(getAttributes(), that.getAttributes());
  }

  @Override
  public int hashCode() {

    return Objects.hash(getName(), isPublic(), getTargetPort(), getAttributes());
  }

  @Override
  public String toString() {
    return "ChePluginEndpoint{"
        + "name='"
        + name
        + '\''
        + ", isPlugin="
        + isPublic
        + ", targetPort='"
        + targetPort
        + '\''
        + ", attributes="
        + attributes
        + '}';
  }
}
