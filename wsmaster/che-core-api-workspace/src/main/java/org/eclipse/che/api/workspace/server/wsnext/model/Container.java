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

public class Container {

  private String image = null;
  private List<EnvVar> env = new ArrayList<EnvVar>();
  private ResourceRequirements resources = null;
  private List<Command> commands = new ArrayList<Command>();
  private List<Server> servers = new ArrayList<Server>();
  private List<Volume> volumes = new ArrayList<Volume>();

  /** */
  public Container image(String image) {
    this.image = image;
    return this;
  }

  public String getImage() {
    return image;
  }

  public void setImage(String image) {
    this.image = image;
  }

  /** List of environment variables to set in the container. Cannot be updated. */
  public Container env(List<EnvVar> env) {
    this.env = env;
    return this;
  }

  public List<EnvVar> getEnv() {
    return env;
  }

  public void setEnv(List<EnvVar> env) {
    this.env = env;
  }

  /** */
  public Container resources(ResourceRequirements resources) {
    this.resources = resources;
    return this;
  }

  public ResourceRequirements getResources() {
    return resources;
  }

  public void setResources(ResourceRequirements resources) {
    this.resources = resources;
  }

  /** List of container commands */
  public Container commands(List<Command> commands) {
    this.commands = commands;
    return this;
  }

  public List<Command> getCommands() {
    return commands;
  }

  public void setCommands(List<Command> commands) {
    this.commands = commands;
  }

  /** List of container servers */
  public Container servers(List<Server> servers) {
    this.servers = servers;
    return this;
  }

  public List<Server> getServers() {
    return servers;
  }

  public void setServers(List<Server> servers) {
    this.servers = servers;
  }

  /** List of container volumes */
  public Container volumes(List<Volume> volumes) {
    this.volumes = volumes;
    return this;
  }

  public List<Volume> getVolumes() {
    return volumes;
  }

  public void setVolumes(List<Volume> volumes) {
    this.volumes = volumes;
  }

  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Container container = (Container) o;
    return Objects.equals(image, container.image)
        && Objects.equals(env, container.env)
        && Objects.equals(resources, container.resources)
        && Objects.equals(commands, container.commands)
        && Objects.equals(servers, container.servers)
        && Objects.equals(volumes, container.volumes);
  }

  @Override
  public int hashCode() {
    return Objects.hash(image, env, resources, commands, servers, volumes);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Container {\n");

    sb.append("    image: ").append(toIndentedString(image)).append("\n");
    sb.append("    env: ").append(toIndentedString(env)).append("\n");
    sb.append("    resources: ").append(toIndentedString(resources)).append("\n");
    sb.append("    commands: ").append(toIndentedString(commands)).append("\n");
    sb.append("    servers: ").append(toIndentedString(servers)).append("\n");
    sb.append("    volumes: ").append(toIndentedString(volumes)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}
