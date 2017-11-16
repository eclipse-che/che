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

import static java.util.stream.Collectors.toMap;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
  private Map<String, DockerContainerConfig> containers;
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
      Map<String, DockerContainerConfig> containers,
      String network)
      throws InfrastructureException {
    super(recipe, machines, warnings);
    this.containers = containers;
    this.network = network;
  }

  public DockerEnvironment(DockerEnvironment environment) throws InfrastructureException {
    super(environment.getRecipe(), environment.getMachines(), environment.getWarnings());
    if (environment.getContainers() != null) {
      containers =
          environment
              .getContainers()
              .entrySet()
              .stream()
              .collect(
                  toMap(Map.Entry::getKey, entry -> new DockerContainerConfig(entry.getValue())));
    }
  }

  /** Ordered mapping of containers names to containers configuration. */
  public Map<String, DockerContainerConfig> getContainers() {
    if (containers == null) {
      containers = new LinkedHashMap<>();
    }
    return containers;
  }

  public DockerEnvironment setContainers(Map<String, DockerContainerConfig> containers) {
    if (containers != null) {
      containers = new HashMap<>(containers);
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
