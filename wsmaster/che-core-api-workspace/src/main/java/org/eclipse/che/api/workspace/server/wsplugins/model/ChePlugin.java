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
package org.eclipse.che.api.workspace.server.wsplugins.model;

import static com.google.common.base.Strings.isNullOrEmpty;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Represents Che plugin in sidecar-powered workspace. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChePlugin {

  private String name = null;
  private String publisher = null;
  private String id = null;
  private String version = null;
  private List<CheContainer> containers = new ArrayList<>();
  private List<ChePluginEndpoint> endpoints = new ArrayList<>();

  @JsonProperty("workspaceEnv")
  private List<EnvVar> workspaceEnv = new ArrayList<>();

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
    if (!isNullOrEmpty(id)) {
      return id;
    } else if (!isNullOrEmpty(publisher) && !isNullOrEmpty(name) && !isNullOrEmpty(version)) {
      return publisher + "/" + name + "/" + version;
    } else {
      return null;
    }
  }

  public void setId(String id) {
    this.id = id;
  }

  public ChePlugin version(String version) {
    this.version = version;
    return this;
  }

  public String getPublisher() {
    return publisher;
  }

  public void setPublisher(String publisher) {
    this.publisher = publisher;
  }

  public ChePlugin publisher(String publisher) {
    this.publisher = publisher;
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
    if (containers == null) {
      containers = new ArrayList<>();
    }
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
    if (endpoints == null) {
      endpoints = new ArrayList<>();
    }
    return endpoints;
  }

  public void setEndpoints(List<ChePluginEndpoint> endpoints) {
    this.endpoints = endpoints;
  }

  /** List of environment variables to set in all the containers of a workspace */
  public ChePlugin workspaceEnv(List<EnvVar> workspaceEnv) {
    this.workspaceEnv = workspaceEnv;
    return this;
  }

  public List<EnvVar> getWorkspaceEnv() {
    if (workspaceEnv == null) {
      workspaceEnv = new ArrayList<>();
    }
    return workspaceEnv;
  }

  public void setWorkspaceEnv(List<EnvVar> workspaceEnv) {
    this.workspaceEnv = workspaceEnv;
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
        && Objects.equals(getPublisher(), chePlugin.getPublisher())
        && Objects.equals(getId(), chePlugin.getId())
        && Objects.equals(getVersion(), chePlugin.getVersion())
        && Objects.equals(getContainers(), chePlugin.getContainers())
        && Objects.equals(getEndpoints(), chePlugin.getEndpoints())
        && Objects.equals(getWorkspaceEnv(), chePlugin.getWorkspaceEnv());
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        getName(),
        getPublisher(),
        getId(),
        getVersion(),
        getContainers(),
        getEndpoints(),
        getWorkspaceEnv());
  }

  @Override
  public String toString() {
    return "ChePlugin{"
        + "name='"
        + name
        + '\''
        + ", publisher='"
        + publisher
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
        + ", workspaceEnv="
        + workspaceEnv
        + '}';
  }
}
