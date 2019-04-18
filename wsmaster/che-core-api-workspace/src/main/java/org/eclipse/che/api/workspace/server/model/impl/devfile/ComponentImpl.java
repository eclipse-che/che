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

import static java.util.stream.Collectors.toCollection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.eclipse.che.api.core.model.workspace.devfile.Component;
import org.eclipse.che.api.core.model.workspace.devfile.Endpoint;
import org.eclipse.che.api.core.model.workspace.devfile.Entrypoint;
import org.eclipse.che.api.core.model.workspace.devfile.Env;
import org.eclipse.che.api.core.model.workspace.devfile.Volume;

/** @author Sergii Leshchenko */
public class ComponentImpl implements Component {

  private String alias;
  private String type;
  private String id;
  private String reference;
  private String referenceContent;
  private Map<String, String> selector;
  private List<EntrypointImpl> entrypoints;
  private String image;
  private String memoryLimit;
  private boolean mountSources;
  private List<String> command;
  private List<String> args;
  private List<VolumeImpl> volumes;
  private List<EnvImpl> env;
  private List<EndpointImpl> endpoints;

  public ComponentImpl() {}

  public ComponentImpl(String type, String id) {
    this.type = type;
    this.id = id;
  }

  public ComponentImpl(
      String type,
      String reference,
      String referenceContent,
      Map<String, String> selector,
      List<? extends Entrypoint> entrypoints) {
    this.type = type;
    this.reference = reference;
    this.referenceContent = referenceContent;
    if (selector != null) {
      this.selector = new HashMap<>(selector);
    }
    if (entrypoints != null) {
      this.entrypoints =
          entrypoints.stream().map(EntrypointImpl::new).collect(toCollection(ArrayList::new));
    }
  }

  public ComponentImpl(
      String type,
      String alias,
      String image,
      String memoryLimit,
      boolean mountSources,
      List<String> command,
      List<String> args,
      List<? extends Volume> volumes,
      List<? extends Env> env,
      List<? extends Endpoint> endpoints) {
    this.alias = alias;
    this.type = type;
    this.image = image;
    this.memoryLimit = memoryLimit;
    this.mountSources = mountSources;
    this.command = command;
    this.args = args;
    if (volumes != null) {
      this.volumes = volumes.stream().map(VolumeImpl::new).collect(toCollection(ArrayList::new));
    }
    if (env != null) {
      this.env = env.stream().map(EnvImpl::new).collect(toCollection(ArrayList::new));
    }
    if (endpoints != null) {
      this.endpoints =
          endpoints.stream().map(EndpointImpl::new).collect(toCollection(ArrayList::new));
    }
  }

  public ComponentImpl(
      String type,
      String alias,
      String id,
      String reference,
      String referenceContent,
      List<? extends Entrypoint> entrypoints,
      String image,
      String memoryLimit,
      boolean mountSources,
      List<String> command,
      List<String> args,
      List<? extends Volume> volumes,
      List<? extends Env> env,
      List<? extends Endpoint> endpoints) {
    this.alias = alias;
    this.type = type;
    this.id = id;
    this.reference = reference;
    this.referenceContent = referenceContent;
    if (entrypoints != null) {
      this.entrypoints =
          entrypoints.stream().map(EntrypointImpl::new).collect(toCollection(ArrayList::new));
    }
    this.image = image;
    this.memoryLimit = memoryLimit;
    this.mountSources = mountSources;
    this.command = command;
    this.args = args;
    if (volumes != null) {
      this.volumes = volumes.stream().map(VolumeImpl::new).collect(toCollection(ArrayList::new));
    }
    if (env != null) {
      this.env = env.stream().map(EnvImpl::new).collect(toCollection(ArrayList::new));
    }
    if (endpoints != null) {
      this.endpoints =
          endpoints.stream().map(EndpointImpl::new).collect(toCollection(ArrayList::new));
    }
  }

  public ComponentImpl(Component component) {
    this(
        component.getType(),
        component.getAlias(),
        component.getId(),
        component.getReference(),
        component.getReferenceContent(),
        component.getEntrypoints(),
        component.getImage(),
        component.getMemoryLimit(),
        component.getMountSources(),
        component.getCommand(),
        component.getArgs(),
        component.getVolumes(),
        component.getEnv(),
        component.getEndpoints());
  }

  @Override
  public String getAlias() {
    return alias;
  }

  public void setAlias(String alias) {
    this.alias = alias;
  }

  @Override
  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  @Override
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  @Override
  public String getReference() {
    return reference;
  }

  public void setReference(String reference) {
    this.reference = reference;
  }

  @Override
  public String getReferenceContent() {
    return referenceContent;
  }

  public void setReferenceContent(String referenceContent) {
    this.referenceContent = referenceContent;
  }

  @Override
  public Map<String, String> getSelector() {
    if (selector == null) {
      selector = new HashMap<>();
    }
    return selector;
  }

  public void setSelector(Map<String, String> selector) {
    this.selector = selector;
  }

  @Override
  public List<EntrypointImpl> getEntrypoints() {
    if (entrypoints == null) {
      entrypoints = new ArrayList<>();
    }
    return entrypoints;
  }

  public void setEntrypoints(List<EntrypointImpl> entrypoints) {
    this.entrypoints = entrypoints;
  }

  @Override
  public String getImage() {
    return image;
  }

  public void setImage(String image) {
    this.image = image;
  }

  @Override
  public String getMemoryLimit() {
    return memoryLimit;
  }

  public void setMemoryLimit(String memoryLimit) {
    this.memoryLimit = memoryLimit;
  }

  @Override
  public boolean getMountSources() {
    return mountSources;
  }

  public void setMountSources(boolean mountSources) {
    this.mountSources = mountSources;
  }

  @Override
  public List<String> getCommand() {
    if (command == null) {
      command = new ArrayList<>();
    }
    return command;
  }

  public void setCommand(List<String> command) {
    this.command = command;
  }

  @Override
  public List<String> getArgs() {
    if (args == null) {
      args = new ArrayList<>();
    }
    return args;
  }

  public void setArgs(List<String> args) {
    this.args = args;
  }

  @Override
  public List<VolumeImpl> getVolumes() {
    if (volumes == null) {
      volumes = new ArrayList<>();
    }
    return volumes;
  }

  public void setVolumes(List<VolumeImpl> volumes) {
    this.volumes = volumes;
  }

  @Override
  public List<EnvImpl> getEnv() {
    if (env == null) {
      env = new ArrayList<>();
    }
    return env;
  }

  public void setEnv(List<EnvImpl> env) {
    this.env = env;
  }

  @Override
  public List<EndpointImpl> getEndpoints() {
    if (endpoints == null) {
      endpoints = new ArrayList<>();
    }
    return endpoints;
  }

  public void setEndpoints(List<EndpointImpl> endpoints) {
    this.endpoints = endpoints;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ComponentImpl)) {
      return false;
    }
    ComponentImpl component = (ComponentImpl) o;
    return getMountSources() == component.getMountSources()
        && Objects.equals(getAlias(), component.getAlias())
        && Objects.equals(getType(), component.getType())
        && Objects.equals(getId(), component.getId())
        && Objects.equals(getReference(), component.getReference())
        && Objects.equals(getReferenceContent(), component.getReferenceContent())
        && Objects.equals(getSelector(), component.getSelector())
        && Objects.equals(getEntrypoints(), component.getEntrypoints())
        && Objects.equals(getImage(), component.getImage())
        && Objects.equals(getMemoryLimit(), component.getMemoryLimit())
        && Objects.equals(getCommand(), component.getCommand())
        && Objects.equals(getArgs(), component.getArgs())
        && Objects.equals(getVolumes(), component.getVolumes())
        && Objects.equals(getEnv(), component.getEnv())
        && Objects.equals(getEndpoints(), component.getEndpoints());
  }

  @Override
  public int hashCode() {

    return Objects.hash(
        getAlias(),
        getType(),
        getId(),
        getReference(),
        getReferenceContent(),
        getSelector(),
        getEntrypoints(),
        getImage(),
        getMemoryLimit(),
        getMountSources(),
        getCommand(),
        getArgs(),
        getVolumes(),
        getEnv(),
        getEndpoints());
  }

  @Override
  public String toString() {
    return "ComponentImpl{"
        + "alias='"
        + alias
        + '\''
        + ", type='"
        + type
        + '\''
        + ", id='"
        + id
        + '\''
        + ", reference='"
        + reference
        + '\''
        + ", referenceContent='"
        + referenceContent
        + '\''
        + ", selector="
        + selector
        + ", entrypoints="
        + entrypoints
        + ", image='"
        + image
        + '\''
        + ", memoryLimit='"
        + memoryLimit
        + '\''
        + ", mountSources="
        + mountSources
        + ", command="
        + command
        + ", args="
        + args
        + ", volumes="
        + volumes
        + ", env="
        + env
        + ", endpoints="
        + endpoints
        + '}';
  }
}
