/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.workspace.server.wsnext.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ChePlugin {

  private String name = null;
  private String id = null;
  private String version = null;
  private List<CheContainer> containers = new ArrayList<>();
  private List<ChePluginEndpoint> endpoints = new ArrayList<>();

  /** Object name. Name must be unique. */
  public ChePlugin name(String name) {
    this.name = name;
    return this;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public ChePlugin id(String id) {
    this.id = id;
    return this;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public ChePlugin version(String version) {
    this.version = version;
    return this;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public ChePlugin containers(List<CheContainer> containers) {
    this.containers = containers;
    return this;
  }

  public List<CheContainer> getContainers() {
    return containers;
  }

  public void setContainers(List<CheContainer> containers) {
    this.containers = containers;
  }

  public ChePlugin endpoints(List<ChePluginEndpoint> endpoints) {
    this.endpoints = endpoints;
    return this;
  }

  public List<ChePluginEndpoint> getEndpoints() {
    return endpoints;
  }

  public void setEndpoints(List<ChePluginEndpoint> endpoints) {
    this.endpoints = endpoints;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ChePlugin)) {
      return false;
    }
    ChePlugin chePlugin = (ChePlugin) o;
    return Objects.equals(getName(), chePlugin.getName())
        && Objects.equals(getId(), chePlugin.getId())
        && Objects.equals(getVersion(), chePlugin.getVersion())
        && Objects.equals(getContainers(), chePlugin.getContainers())
        && Objects.equals(getEndpoints(), chePlugin.getEndpoints());
  }

  @Override
  public int hashCode() {

    return Objects.hash(
        super.hashCode(), getName(), getId(), getVersion(), getContainers(), getEndpoints());
  }

  @Override
  public String toString() {
    return "ChePlugin{"
        + "name='"
        + name
        + '\''
        + ", id='"
        + id
        + '\''
        + ", version='"
        + version
        + '\''
        + ", containers="
        + containers
        + ", endpoints="
        + endpoints
        + '}';
  }
}
