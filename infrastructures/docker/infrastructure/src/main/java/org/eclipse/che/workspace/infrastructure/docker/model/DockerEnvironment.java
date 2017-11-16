/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.docker.model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import org.eclipse.che.api.core.model.workspace.Warning;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.environment.InternalEnvironment;
import org.eclipse.che.api.workspace.server.spi.environment.InternalMachineConfig;
import org.eclipse.che.api.workspace.server.spi.environment.InternalRecipe;

/**
 * Description of docker container environment as representation of environment of machines in Che.
 *
 * @author Alexander Garagatyi
 */
public class DockerEnvironment extends InternalEnvironment {
  private LinkedHashMap<String, DockerContainerConfig> containers;
  private String network;

  public DockerEnvironment() {}

  public DockerEnvironment(
      InternalRecipe recipe, Map<String, InternalMachineConfig> machines, List<Warning> warnings) {
    super(recipe, machines, warnings);
  }

  public DockerEnvironment(
      InternalRecipe recipe,
      Map<String, InternalMachineConfig> machines,
      List<Warning> warnings,
      LinkedHashMap<String, DockerContainerConfig> containers,
      String network)
      throws InfrastructureException {
    super(recipe, machines, warnings);
    this.containers = containers;
    this.network = network;
  }

  public DockerEnvironment(DockerEnvironment environment) throws InfrastructureException {
    super(environment.getRecipe(), environment.getMachines(), environment.getWarnings());
    if (environment.getContainers() != null) {
      containers = new LinkedHashMap<>();
      for (Entry<String, DockerContainerConfig> containerEntry :
          environment.getContainers().entrySet()) {
        containers.put(
            containerEntry.getKey(), new DockerContainerConfig(containerEntry.getValue()));
      }
    }
  }

  /** Ordered mapping of containers names to containers configuration. */
  public LinkedHashMap<String, DockerContainerConfig> getContainers() {
    if (containers == null) {
      containers = new LinkedHashMap<>();
    }
    return containers;
  }

  public DockerEnvironment setContainers(LinkedHashMap<String, DockerContainerConfig> containers) {
    if (containers != null) {
      containers = new LinkedHashMap<>(containers);
    }
    this.containers = containers;
    return this;
  }

  public String getNetwork() {
    return network;
  }

  public DockerEnvironment setNetwork(String network) {
    this.network = network;
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof DockerEnvironment)) {
      return false;
    }
    DockerEnvironment that = (DockerEnvironment) o;
    return Objects.equals(getContainers(), that.getContainers())
        && Objects.equals(getNetwork(), that.getNetwork())
        && Objects.equals(getRecipe(), that.getRecipe())
        && Objects.equals(getMachines(), that.getMachines())
        && Objects.equals(getWarnings(), that.getWarnings());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getContainers(), getNetwork(), getMachines(), getRecipe(), getWarnings());
  }

  @Override
  public String toString() {
    return "DockerEnvironment{"
        + "containers="
        + containers
        + ", network='"
        + network
        + '\''
        + ", machines="
        + getMachines()
        + ", recipe="
        + getRecipe()
        + ", warnings="
        + getWarnings()
        + '}';
  }
}
