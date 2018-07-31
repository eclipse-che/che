/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.docker.environment.compose.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.eclipse.che.workspace.infrastructure.docker.environment.compose.deserializer.CommandDeserializer;
import org.eclipse.che.workspace.infrastructure.docker.environment.compose.deserializer.EnvironmentDeserializer;
import org.eclipse.che.workspace.infrastructure.docker.environment.compose.deserializer.MemLimitDeserializer;

/**
 * Description of docker compose service.
 *
 * @author Alexander Garagatyi
 */
public class ComposeService {
  @JsonProperty("container_name")
  private String containerName;

  @JsonDeserialize(using = CommandDeserializer.class)
  private List<String> command;

  private List<String> entrypoint;
  private String image;

  @JsonProperty("depends_on")
  private List<String> dependsOn;

  @JsonDeserialize(using = EnvironmentDeserializer.class)
  private Map<String, String> environment;

  private Set<String> expose;
  private List<String> ports;
  private Map<String, String> labels;
  private List<String> links;
  private List<String> volumes;

  @JsonProperty("volumes_from")
  private List<String> volumesFrom;

  @JsonDeserialize(using = MemLimitDeserializer.class)
  @JsonProperty("mem_limit")
  private Long memLimit;

  private BuildContext build;
  private List<String> networks;

  public ComposeService() {}

  public ComposeService(ComposeService service) {
    image = service.getImage();
    if (service.getBuild() != null) {
      build = new BuildContext(service.getBuild());
    }
    if (service.getEntrypoint() != null) {
      entrypoint = new ArrayList<>(service.getEntrypoint());
    }
    if (service.getCommand() != null) {
      command = new ArrayList<>(service.getCommand());
    }
    if (service.getEnvironment() != null) {
      environment = new HashMap<>(service.getEnvironment());
    }
    if (service.getDependsOn() != null) {
      dependsOn = new ArrayList<>(service.getDependsOn());
    }
    containerName = service.getContainerName();
    if (service.getLinks() != null) {
      links = new ArrayList<>(service.getLinks());
    }
    if (service.getLabels() != null) {
      labels = new HashMap<>(service.getLabels());
    }

    this.setExpose(service.getExpose());

    if (service.getPorts() != null) {
      ports = new ArrayList<>(service.getPorts());
    }
    if (service.getVolumesFrom() != null) {
      volumesFrom = new ArrayList<>(service.getVolumesFrom());
    }
    if (service.getVolumes() != null) {
      volumes = new ArrayList<>(service.getVolumes());
    }
    memLimit = service.getMemLimit();
    if (service.getNetworks() != null) {
      networks = new ArrayList<>(service.getNetworks());
    }
  }

  /** Image for container creation. */
  public String getImage() {
    return image;
  }

  public void setImage(String image) {
    this.image = image;
  }

  public ComposeService withImage(String image) {
    this.image = image;
    return this;
  }

  /** Build context for container image creation. */
  public BuildContext getBuild() {
    return build;
  }

  public void setBuild(BuildContext build) {
    this.build = build;
  }

  public ComposeService withBuild(BuildContext build) {
    this.build = build;
    return this;
  }

  /** Override the default entrypoint. */
  public List<String> getEntrypoint() {
    return entrypoint;
  }

  public void setEntrypoint(List<String> entrypoint) {
    this.entrypoint = entrypoint;
  }

  public ComposeService withEntrypoint(List<String> entrypoint) {
    this.entrypoint = entrypoint;
    return this;
  }

  /** Override the default command. */
  public List<String> getCommand() {
    return command;
  }

  public void setCommand(List<String> command) {
    this.command = command;
  }

  public ComposeService withCommand(List<String> command) {
    this.command = command;
    return this;
  }

  /** Environment variables that should be added into container. */
  public Map<String, String> getEnvironment() {
    if (environment == null) {
      environment = new HashMap<>();
    }
    return environment;
  }

  public void setEnvironment(Map<String, String> environment) {
    this.environment = environment;
  }

  public ComposeService withEnvironment(Map<String, String> environment) {
    this.environment = environment;
    return this;
  }

  /**
   * Express dependency between services.
   *
   * <p>Compose engine implementation should start services in dependency order.
   */
  public List<String> getDependsOn() {
    if (dependsOn == null) {
      dependsOn = new ArrayList<>();
    }

    return dependsOn;
  }

  public void setDependsOn(List<String> dependsOn) {
    this.dependsOn = dependsOn;
  }

  public ComposeService withDependsOn(List<String> dependsOn) {
    this.dependsOn = dependsOn;
    return this;
  }

  /** Specify a custom container name, rather than a generated default name. */
  public String getContainerName() {
    return containerName;
  }

  public void setContainerName(String containerName) {
    this.containerName = containerName;
  }

  public ComposeService withContainerName(String containerName) {
    this.containerName = containerName;
    return this;
  }

  /**
   * Link to containers in another service.
   *
   * <p>Either specify both the service name and a link alias (SERVICE:ALIAS), or just the service
   * name. <br>
   * Examples:
   *
   * <ul>
   *   <li>db
   *   <li>db:database
   * </ul>
   */
  public List<String> getLinks() {
    if (links == null) {
      links = new ArrayList<>();
    }
    return links;
  }

  public void setLinks(List<String> links) {
    this.links = links;
  }

  public ComposeService withLinks(List<String> links) {
    this.links = links;
    return this;
  }

  /** Add metadata to containers using Docker labels. */
  public Map<String, String> getLabels() {
    if (labels == null) {
      labels = new HashMap<>();
    }
    return labels;
  }

  public void setLabels(Map<String, String> labels) {
    this.labels = labels;
  }

  public ComposeService withLabels(Map<String, String> labels) {
    this.labels = labels;
    return this;
  }

  /**
   * Immutable expose ports list without publishing them to the host machine - they’ll only be
   * accessible to linked services.
   *
   * <p>Only the internal port can be specified. <br>
   * Examples:
   *
   * <ul>
   *   <li>3000/tcp
   *   <li>8000/udp
   * </ul>
   */
  public Set<String> getExpose() {
    if (expose == null) {
      return Collections.emptySet();
    }
    return ImmutableSet.copyOf(expose);
  }

  public void setExpose(Set<String> expose) {
    if (expose == null) {
      this.expose = null;
    } else {
      this.expose =
          expose
              .stream()
              .map(this::normalizeExposeValue)
              .collect(Collectors.toCollection(HashSet::new));
    }
  }

  private String normalizeExposeValue(String expose) {
    return expose.contains("/") ? expose : expose + "/tcp";
  }

  public ComposeService withExpose(Set<String> expose) {
    setExpose(expose);
    return this;
  }

  /**
   * Expose ports. Either specify both ports (HOST:CONTAINER), or just the container port (a random
   * host port will be chosen).
   *
   * <p>Examples:
   *
   * <ul>
   *   <li>80
   *   <li>3000
   *   <li>8080:80
   *   <li>80:8000
   *   <li>9090-9091:8080-8081
   *   <li>127.0.0.1:8001:8001
   *   <li>127.0.0.1:5000-5010:5000-5010
   * </ul>
   */
  public List<String> getPorts() {
    if (ports == null) {
      ports = new ArrayList<>();
    }
    return ports;
  }

  public void setPorts(List<String> ports) {
    this.ports = ports;
  }

  public ComposeService withPorts(List<String> ports) {
    this.ports = ports;
    return this;
  }

  /**
   * Mount paths or named volumes.
   *
   * <p>Examples:
   *
   * <ul>
   *   <li>/var/lib/mysql
   *   <li>/opt/data:/var/lib/mysql
   *   <li>data-volume:/var/lib/mysql
   * </ul>
   */
  public List<String> getVolumes() {
    if (volumes == null) {
      volumes = new ArrayList<>();
    }
    return volumes;
  }

  public void setVolumes(List<String> volumes) {
    this.volumes = volumes;
  }

  public ComposeService withVolumes(List<String> volumes) {
    this.volumes = volumes;
    return this;
  }

  /**
   * Mount all of the volumes from another service.
   *
   * <p>Optionally access level can be specified: read-only access (ro) or read-write (rw). If no
   * access level is specified, then read-write will be used.
   *
   * <p>Examples:
   *
   * <ul>
   *   <li>service_name
   *   <li>service_name:ro
   *   <li>service_name:rw
   * </ul>
   */
  public List<String> getVolumesFrom() {
    if (volumesFrom == null) {
      volumesFrom = new ArrayList<>();
    }
    return volumesFrom;
  }

  public void setVolumesFrom(List<String> volumesFrom) {
    this.volumesFrom = volumesFrom;
  }

  public ComposeService withVolumesFrom(List<String> volumesFrom) {
    this.volumesFrom = volumesFrom;
    return this;
  }

  /** Memory limit for the container of service, specified in bytes. */
  public Long getMemLimit() {
    return memLimit;
  }

  public void setMemLimit(Long memLimit) {
    this.memLimit = memLimit;
  }

  public ComposeService withMemLimit(Long memLimit) {
    this.memLimit = memLimit;
    return this;
  }

  /** List of networks that should be connected to service. */
  public List<String> getNetworks() {
    if (networks == null) {
      networks = new ArrayList<>();
    }
    return networks;
  }

  public void setNetworks(List<String> networks) {
    this.networks = networks;
  }

  public ComposeService withNetworks(List<String> networks) {
    this.networks = networks;
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ComposeService)) return false;
    ComposeService service = (ComposeService) o;
    return Objects.equals(containerName, service.containerName)
        && Objects.equals(command, service.command)
        && Objects.equals(entrypoint, service.entrypoint)
        && Objects.equals(image, service.image)
        && Objects.equals(dependsOn, service.dependsOn)
        && Objects.equals(environment, service.environment)
        && Objects.equals(expose, service.expose)
        && Objects.equals(ports, service.ports)
        && Objects.equals(labels, service.labels)
        && Objects.equals(links, service.links)
        && Objects.equals(volumes, service.volumes)
        && Objects.equals(volumesFrom, service.volumesFrom)
        && Objects.equals(memLimit, service.memLimit)
        && Objects.equals(build, service.build)
        && Objects.equals(networks, service.networks);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        containerName,
        build,
        command,
        entrypoint,
        image,
        dependsOn,
        environment,
        expose,
        ports,
        labels,
        links,
        volumes,
        volumesFrom,
        memLimit,
        networks);
  }

  @Override
  public String toString() {
    return "ComposeServiceImpl{"
        + "containerName='"
        + containerName
        + '\''
        + ", command="
        + command
        + ", entrypoint="
        + entrypoint
        + ", image='"
        + image
        + '\''
        + ", dependsOn="
        + dependsOn
        + ", environment="
        + environment
        + ", expose="
        + expose
        + ", ports="
        + ports
        + ", labels="
        + labels
        + ", links="
        + links
        + ", volumes="
        + volumes
        + ", volumesFrom="
        + volumesFrom
        + ", memLimit="
        + memLimit
        + ", build="
        + build
        + ", networks="
        + networks
        + '}';
  }
}
