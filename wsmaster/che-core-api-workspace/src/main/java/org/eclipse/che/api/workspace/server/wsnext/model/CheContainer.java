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

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Represents sidecar container in Che workspace. */
public class CheContainer {

  private String image = null;
  private List<EnvVar> env = new ArrayList<>();

  @JsonProperty("editor-commands")
  private List<Command> commands = new ArrayList<>();

  private List<Volume> volumes = new ArrayList<>();
  private List<CheContainerPort> ports = new ArrayList<>();

  public CheContainer image(String image) {
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
  public CheContainer env(List<EnvVar> env) {
    this.env = env;
    return this;
  }

  public List<EnvVar> getEnv() {
    return env;
  }

  public void setEnv(List<EnvVar> env) {
    this.env = env;
  }

  /** List of container commands */
  public CheContainer commands(List<Command> commands) {
    this.commands = commands;
    return this;
  }

  public List<Command> getCommands() {
    return commands;
  }

  public void setCommands(List<Command> commands) {
    this.commands = commands;
  }

  /** List of container volumes */
  public CheContainer volumes(List<Volume> volumes) {
    this.volumes = volumes;
    return this;
  }

  public List<Volume> getVolumes() {
    return volumes;
  }

  public void setVolumes(List<Volume> volumes) {
    this.volumes = volumes;
  }

  public CheContainer ports(List<CheContainerPort> ports) {
    this.ports = ports;
    return this;
  }

  public List<CheContainerPort> getPorts() {
    return ports;
  }

  public void setPorts(List<CheContainerPort> ports) {
    this.ports = ports;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof CheContainer)) {
      return false;
    }
    CheContainer that = (CheContainer) o;
    return Objects.equals(getImage(), that.getImage())
        && Objects.equals(getEnv(), that.getEnv())
        && Objects.equals(getCommands(), that.getCommands())
        && Objects.equals(getVolumes(), that.getVolumes())
        && Objects.equals(getPorts(), that.getPorts());
  }

  @Override
  public int hashCode() {

    return Objects.hash(getImage(), getEnv(), getCommands(), getVolumes(), getPorts());
  }

  @Override
  public String toString() {
    return "CheContainer{"
        + "image='"
        + image
        + '\''
        + ", env="
        + env
        + ", commands="
        + commands
        + ", volumes="
        + volumes
        + ", ports="
        + ports
        + '}';
  }
}
