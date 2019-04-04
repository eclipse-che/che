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
package org.eclipse.che.api.workspace.server.model.impl.devfile;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.eclipse.che.api.core.model.workspace.devfile.Endpoint;

/** @author Sergii Leshchenko */
public class EndpointImpl implements Endpoint {

  private String name;
  private Integer port;
  private Map<String, String> attributes;

  public EndpointImpl() {}

  public EndpointImpl(String name, Integer port, Map<String, String> attributes) {
    this.name = name;
    this.port = port;
    this.attributes = attributes;
  }

  public EndpointImpl(Endpoint endpoint) {
    this(endpoint.getName(), endpoint.getPort(), endpoint.getAttributes());
  }

  @Override
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public Integer getPort() {
    return port;
  }

  public void setPort(Integer port) {
    this.port = port;
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
    if (!(o instanceof EndpointImpl)) {
      return false;
    }
    EndpointImpl endpoint = (EndpointImpl) o;
    return Objects.equals(getName(), endpoint.getName())
        && Objects.equals(getPort(), endpoint.getPort())
        && Objects.equals(getAttributes(), endpoint.getAttributes());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getName(), getPort(), getAttributes());
  }

  @Override
  public String toString() {
    return "EndpointImpl{"
        + "name='"
        + name
        + '\''
        + ", port="
        + port
        + ", attributes="
        + attributes
        + '}';
  }
}
