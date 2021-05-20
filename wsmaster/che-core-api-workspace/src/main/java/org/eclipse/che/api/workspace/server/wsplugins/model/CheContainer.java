/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Represents sidecar container in Che workspace. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CheContainer {

  private String image = null;
  private String name = null;
  private List<EnvVar> env = new ArrayList<>();

  @JsonProperty("commands")
  private List<Command> commands = new ArrayList<>();

  private List<Volume> volumes = new ArrayList<>();
  private List<CheContainerPort> ports = new ArrayList<>();

  @JsonProperty("memoryLimit")
  private String memoryLimit = null;

  @JsonProperty("memoryRequest")
  private String memoryRequest = null;

  @JsonProperty("cpuLimit")
  private String cpuLimit = null;

  @JsonProperty("cpuRequest")
  private String cpuRequest = null;

  @JsonProperty("mountSources")
  private boolean mountSources = false;

  @JsonProperty("command")
  private List<String> command;

  @JsonProperty("args")
  private List<String> args;

  @JsonProperty("lifecycle")
  private Lifecycle lifecycle;

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

  public CheContainer name(String name) {
    this.name = name;
    return this;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  /** List of environment variables to set in the container. Cannot be updated. */
  public CheContainer env(List<EnvVar> env) {
    this.env = env;
    return this;
  }

  public List<EnvVar> getEnv() {
    if (env == null) {
      env = new ArrayList<>();
    }
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
    if (commands == null) {
      commands = new ArrayList<>();
    }
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
    if (volumes == null) {
      volumes = new ArrayList<>();
    }
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
    if (ports == null) {
      ports = new ArrayList<>();
    }
    return ports;
  }

  public void setPorts(List<CheContainerPort> ports) {
    this.ports = ports;
  }

  public CheContainer memoryLimit(String memoryLimit) {
    this.memoryLimit = memoryLimit;
    return this;
  }

  public String getMemoryLimit() {
    return memoryLimit;
  }

  public void setMemoryLimit(String memoryLimit) {
    this.memoryLimit = memoryLimit;
  }

  public CheContainer memoryRequest(String memoryRequest) {
    this.memoryRequest = memoryRequest;
    return this;
  }

  public String getMemoryRequest() {
    return memoryRequest;
  }

  public void setMemoryRequest(String memoryRequest) {
    this.memoryRequest = memoryRequest;
  }

  public CheContainer cpuLimit(String cpuLimit) {
    this.cpuLimit = cpuLimit;
    return this;
  }

  public String getCpuLimit() {
    return cpuLimit;
  }

  public void setCpuLimit(String cpuLimit) {
    this.cpuLimit = cpuLimit;
  }

  public CheContainer cpuRequest(String cpuRequest) {
    this.cpuRequest = cpuRequest;
    return this;
  }

  public String getCpuRequest() {
    return cpuRequest;
  }

  public void setCpuRequest(String cpuRequest) {
    this.cpuRequest = cpuRequest;
  }

  public CheContainer mountSources(boolean mountSources) {
    this.mountSources = mountSources;
    return this;
  }

  public boolean isMountSources() {
    return mountSources;
  }

  public void setMountSources(boolean mountSources) {
    this.mountSources = mountSources;
  }

  public CheContainer command(List<String> command) {
    this.command = command;
    return this;
  }

  public List<String> getCommand() {
    if (command == null) {
      return new ArrayList<>();
    }
    return command;
  }

  public void setCommand(List<String> command) {
    this.command = command;
  }

  public CheContainer args(List<String> args) {
    this.args = args;
    return this;
  }

  public List<String> getArgs() {
    if (args == null) {
      return new ArrayList<>();
    }
    return args;
  }

  public void setLifecycle(Lifecycle lifecycle) {
    this.lifecycle = lifecycle;
  }

  public CheContainer lifecycle(Lifecycle lifecycle) {
    this.lifecycle = lifecycle;
    return this;
  }

  public Lifecycle getLifecycle() {
    return lifecycle;
  }

  public void setArgs(List<String> args) {
    this.args = args;
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
        && Objects.equals(getPorts(), that.getPorts())
        && Objects.equals(getMemoryLimit(), that.getMemoryLimit())
        && Objects.equals(getMemoryRequest(), that.getMemoryRequest())
        && Objects.equals(getCpuLimit(), that.getCpuLimit())
        && Objects.equals(getCpuRequest(), that.getCpuRequest())
        && Objects.equals(getName(), that.getName())
        && isMountSources() == that.isMountSources()
        && Objects.equals(getCommand(), that.getCommand())
        && Objects.equals(getArgs(), that.getArgs());
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        getImage(),
        getEnv(),
        getCommands(),
        getVolumes(),
        getPorts(),
        getMemoryLimit(),
        getMemoryRequest(),
        getCpuLimit(),
        getCpuRequest(),
        getName(),
        isMountSources(),
        getCommand(),
        getArgs());
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
        + ", memoryLimit="
        + memoryLimit
        + ", memoryRequest="
        + memoryRequest
        + ", cpuLimit="
        + cpuLimit
        + ", cpuRequest="
        + cpuRequest
        + ", name="
        + name
        + ", mountSources="
        + mountSources
        + ", command="
        + command
        + ", args="
        + args
        + '}';
  }
}
