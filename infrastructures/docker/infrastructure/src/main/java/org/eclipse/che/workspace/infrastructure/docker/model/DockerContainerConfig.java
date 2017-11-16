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
import org.eclipse.che.commons.annotation.Nullable;

/**
 * Representation of configuration of docker container configuration.
 *
 * @author Alexander Garagatyi
 */
public class DockerContainerConfig {
  private DockerBuildContext build;
  private String cgroupParent;
  private List<String> command;
  private String containerName;
  private Long cpuPeriod;
  private Long cpuQuota;
  private String cpuSet;
  private List<String> dependsOn;
  private List<String> dns;
  private List<String> securityOpt;
  private List<String> entrypoint;
  private Map<String, String> environment;
  private Set<String> expose;
  private List<String> extraHosts;
  private String id;
  private String image;
  private Map<String, String> labels;
  private List<String> links;
  private Long memLimit;
  private Long memSwapLimit;
  private List<String> networks;
  private List<String> ports;
  private List<String> volumes;
  private List<String> volumesFrom;
  private String pidMode;
  private Integer pidsLimit;
  private Boolean privileged;

  public DockerContainerConfig() {}

  public DockerContainerConfig(DockerContainerConfig container) {
    if (container.getBuild() != null) {
      build = new DockerBuildContext(container.getBuild());
    }
    cgroupParent = container.getCgroupParent();
    if (container.getCommand() != null) {
      command = new ArrayList<>(container.getCommand());
    }
    containerName = container.getContainerName();
    cpuPeriod = container.getCpuPeriod();
    cpuQuota = container.getCpuQuota();
    cpuSet = container.getCpuSet();
    if (container.getDependsOn() != null) {
      dependsOn = new ArrayList<>(container.getDependsOn());
    }
    if (container.getDns() != null) {
      dns = new ArrayList<>(container.getDns());
    }
    if (container.getSecurityOpt() != null) {
      securityOpt = new ArrayList<>(container.getSecurityOpt());
    }
    if (container.getEntrypoint() != null) {
      entrypoint = new ArrayList<>(container.getEntrypoint());
    }
    if (container.getEnvironment() != null) {
      environment = new HashMap<>(container.getEnvironment());
    }

    setExpose(container.getExpose());

    if (container.getExtraHosts() != null) {
      extraHosts = new ArrayList<>(container.getExtraHosts());
    }
    id = container.getId();
    image = container.getImage();
    if (container.getLabels() != null) {
      labels = new HashMap<>(container.getLabels());
    }
    if (container.getLinks() != null) {
      links = new ArrayList<>(container.getLinks());
    }
    memLimit = container.getMemLimit();
    memSwapLimit = container.getMemSwapLimit();
    if (container.getNetworks() != null) {
      networks = new ArrayList<>(container.getNetworks());
    }
    if (container.getPorts() != null) {
      ports = new ArrayList<>(container.getPorts());
    }
    if (container.getVolumes() != null) {
      volumes = new ArrayList<>(container.getVolumes());
    }
    if (container.getVolumesFrom() != null) {
      volumesFrom = new ArrayList<>(container.getVolumesFrom());
    }
    pidMode = container.getPidMode();
    pidsLimit = container.getPidsLimit();
    privileged = container.getPrivileged();
  }

  /** Unique identifier of machine. */
  public String getId() {
    return id;
  }

  public DockerContainerConfig setId(String id) {
    this.id = id;
    return this;
  }

  /** Image for container creation. */
  public String getImage() {
    return image;
  }

  public DockerContainerConfig setImage(String image) {
    this.image = image;
    return this;
  }

  /** Build context for container image creation. */
  public DockerBuildContext getBuild() {
    return build;
  }

  public DockerContainerConfig setBuild(DockerBuildContext build) {
    if (build != null) {
      build = new DockerBuildContext(build);
    }
    this.build = build;
    return this;
  }

  /** Override the default entrypoint. */
  @Nullable
  public List<String> getEntrypoint() {
    return entrypoint;
  }

  public DockerContainerConfig setEntrypoint(List<String> entrypoint) {
    if (entrypoint != null) {
      entrypoint = new ArrayList<>(entrypoint);
    }
    this.entrypoint = entrypoint;
    return this;
  }

  /** Override the default command. */
  @Nullable
  public List<String> getCommand() {
    return command;
  }

  public DockerContainerConfig setCommand(List<String> command) {
    if (command != null) {
      command = new ArrayList<>(command);
    }
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

  public DockerContainerConfig setEnvironment(Map<String, String> environment) {
    if (environment != null) {
      environment = new HashMap<>(environment);
    }
    this.environment = environment;
    return this;
  }

  /**
   * Express dependency between containers.
   *
   * <p>Environment engine implementation should start containers in dependency order.
   */
  public List<String> getDependsOn() {
    if (dependsOn == null) {
      dependsOn = new ArrayList<>();
    }

    return dependsOn;
  }

  public DockerContainerConfig setDependsOn(List<String> dependsOn) {
    if (dependsOn != null) {
      dependsOn = new ArrayList<>(dependsOn);
    }
    this.dependsOn = dependsOn;
    return this;
  }

  /** Specify a custom container name, rather than a generated default name. */
  public String getContainerName() {
    return containerName;
  }

  public DockerContainerConfig setContainerName(String containerName) {
    this.containerName = containerName;
    return this;
  }

  /**
   * Link to other containers.
   *
   * <p>Either specify both the container name and a link alias (CONTAINER:ALIAS), or just the
   * container name. <br>
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

  public DockerContainerConfig setLinks(List<String> links) {
    if (links != null) {
      links = new ArrayList<>(links);
    }
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

  public DockerContainerConfig setLabels(Map<String, String> labels) {
    if (labels != null) {
      labels = new HashMap<>(labels);
    }
    this.labels = labels;
    return this;
  }

  /**
   * Expose ports without publishing them to the host machine - they’ll only be accessible to linked
   * containers.
   *
   * <p>Only the internal port can be specified. <br>
   * Examples:
   *
   * <ul>
   *   <li>3000
   *   <li>8000
   * </ul>
   */
  public Set<String> getExpose() {
    if (expose == null) {
      return Collections.emptySet();
    }
    return ImmutableSet.copyOf(expose);
  }

  public DockerContainerConfig setExpose(Set<String> expose) {
    if (expose == null) {
      this.expose = null;
    } else {
      this.expose =
          expose
              .stream()
              .map(this::normalizeExposeValue)
              .collect(Collectors.toCollection(HashSet::new));
    }
    return this;
  }

  private String normalizeExposeValue(String expose) {
    return expose.contains("/") ? expose : expose + "/tcp";
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

  public DockerContainerConfig setPorts(List<String> ports) {
    if (ports != null) {
      ports = new ArrayList<>(ports);
    }
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

  public DockerContainerConfig setVolumes(List<String> volumes) {
    if (volumes != null) {
      volumes = new ArrayList<>(volumes);
    }
    this.volumes = volumes;
    return this;
  }

  /**
   * Mount all of the volumes from another container.
   *
   * <p>Optionally access level can be specified: read-only access (ro) or read-write (rw). If no
   * access level is specified, then read-write will be used.
   *
   * <p>Examples:
   *
   * <ul>
   *   <li>container_name
   *   <li>container_name:ro
   *   <li>container_name:rw
   * </ul>
   */
  public List<String> getVolumesFrom() {
    if (volumesFrom == null) {
      volumesFrom = new ArrayList<>();
    }
    return volumesFrom;
  }

  public DockerContainerConfig setVolumesFrom(List<String> volumesFrom) {
    if (volumesFrom != null) {
      volumesFrom = new ArrayList<>(volumesFrom);
    }
    this.volumesFrom = volumesFrom;
    return this;
  }

  /** Memory limit for the container, specified in bytes. */
  public Long getMemLimit() {
    return memLimit;
  }

  public DockerContainerConfig setMemLimit(Long memLimit) {
    this.memLimit = memLimit;
    return this;
  }

  /** List of networks that should be connected to container. */
  public List<String> getNetworks() {
    if (networks == null) {
      networks = new ArrayList<>();
    }
    return networks;
  }

  public DockerContainerConfig setNetworks(List<String> networks) {
    if (networks != null) {
      networks = new ArrayList<>(networks);
    }
    this.networks = networks;
    return this;
  }

  public String getPidMode() {
    return pidMode;
  }

  public DockerContainerConfig setPidMode(String pidMode) {
    this.pidMode = pidMode;
    return this;
  }

  public List<String> getDns() {
    if (dns == null) {
      dns = new ArrayList<>();
    }
    return dns;
  }

  public DockerContainerConfig setDns(List<String> dns) {
    if (dns != null) {
      dns = new ArrayList<>(dns);
    }
    this.dns = dns;
    return this;
  }

  public List<String> getSecurityOpt() {
    if (securityOpt == null) {
      securityOpt = new ArrayList<>();
    }
    return securityOpt;
  }

  public DockerContainerConfig setSecurityOpt(List<String> securityOpt) {
    if (securityOpt != null) {
      securityOpt = new ArrayList<>(securityOpt);
    }
    this.securityOpt = securityOpt;
    return this;
  }

  public List<String> getExtraHosts() {
    if (extraHosts == null) {
      extraHosts = new ArrayList<>();
    }
    return extraHosts;
  }

  public DockerContainerConfig setExtraHosts(List<String> extraHosts) {
    if (extraHosts != null) {
      extraHosts = new ArrayList<>(extraHosts);
    }
    this.extraHosts = extraHosts;
    return this;
  }

  public Long getMemSwapLimit() {
    return memSwapLimit;
  }

  public DockerContainerConfig setMemSwapLimit(Long memSwapLimit) {
    this.memSwapLimit = memSwapLimit;
    return this;
  }

  public Integer getPidsLimit() {
    return pidsLimit;
  }

  public DockerContainerConfig setPidsLimit(Integer pidsLimit) {
    this.pidsLimit = pidsLimit;
    return this;
  }

  public String getCgroupParent() {
    return cgroupParent;
  }

  public DockerContainerConfig setCgroupParent(String cgroupParent) {
    this.cgroupParent = cgroupParent;
    return this;
  }

  public Long getCpuPeriod() {
    return cpuPeriod;
  }

  public DockerContainerConfig setCpuPeriod(Long cpuPeriod) {
    this.cpuPeriod = cpuPeriod;
    return this;
  }

  public Long getCpuQuota() {
    return cpuQuota;
  }

  public DockerContainerConfig setCpuQuota(Long cpuQuota) {
    this.cpuQuota = cpuQuota;
    return this;
  }

  public String getCpuSet() {
    return cpuSet;
  }

  public DockerContainerConfig setCpuSet(String cpuSet) {
    this.cpuSet = cpuSet;
    return this;
  }

  public Boolean getPrivileged() {
    return privileged;
  }

  public DockerContainerConfig setPrivileged(Boolean privileged) {
    this.privileged = privileged;
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof DockerContainerConfig)) return false;
    DockerContainerConfig that = (DockerContainerConfig) o;
    return Objects.equals(getBuild(), that.getBuild())
        && Objects.equals(getCgroupParent(), that.getCgroupParent())
        && Objects.equals(getCommand(), that.getCommand())
        && Objects.equals(getContainerName(), that.getContainerName())
        && Objects.equals(getCpuPeriod(), that.getCpuPeriod())
        && Objects.equals(getCpuQuota(), that.getCpuQuota())
        && Objects.equals(getCpuSet(), that.getCpuSet())
        && Objects.equals(getDependsOn(), that.getDependsOn())
        && Objects.equals(getDns(), that.getDns())
        && Objects.equals(getSecurityOpt(), that.getSecurityOpt())
        && Objects.equals(getEntrypoint(), that.getEntrypoint())
        && Objects.equals(getEnvironment(), that.getEnvironment())
        && Objects.equals(getExpose(), that.getExpose())
        && Objects.equals(getExtraHosts(), that.getExtraHosts())
        && Objects.equals(getId(), that.getId())
        && Objects.equals(getImage(), that.getImage())
        && Objects.equals(getLabels(), that.getLabels())
        && Objects.equals(getLinks(), that.getLinks())
        && Objects.equals(getMemLimit(), that.getMemLimit())
        && Objects.equals(getMemSwapLimit(), that.getMemSwapLimit())
        && Objects.equals(getNetworks(), that.getNetworks())
        && Objects.equals(getPorts(), that.getPorts())
        && Objects.equals(getVolumes(), that.getVolumes())
        && Objects.equals(getVolumesFrom(), that.getVolumesFrom())
        && Objects.equals(getPidMode(), that.getPidMode())
        && Objects.equals(getPidsLimit(), that.getPidsLimit())
        && Objects.equals(getPrivileged(), that.getPrivileged());
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        getBuild(),
        getCgroupParent(),
        getCommand(),
        getContainerName(),
        getCpuPeriod(),
        getCpuQuota(),
        getCpuSet(),
        getDependsOn(),
        getDns(),
        getSecurityOpt(),
        getEntrypoint(),
        getEnvironment(),
        getExpose(),
        getExtraHosts(),
        getId(),
        getImage(),
        getLabels(),
        getLinks(),
        getMemLimit(),
        getMemSwapLimit(),
        getNetworks(),
        getPorts(),
        getVolumes(),
        getVolumesFrom(),
        getPidMode(),
        getPidsLimit(),
        getPrivileged());
  }

  @Override
  public String toString() {
    return "DockerContainerConfig{"
        + "build="
        + build
        + ", cgroupParent='"
        + cgroupParent
        + '\''
        + ", command="
        + command
        + ", containerName='"
        + containerName
        + '\''
        + ", cpuPeriod="
        + cpuPeriod
        + ", cpuQuota="
        + cpuQuota
        + ", cpuSet='"
        + cpuSet
        + '\''
        + ", dependsOn="
        + dependsOn
        + ", dns="
        + dns
        + ", entrypoint="
        + entrypoint
        + ", environment="
        + environment
        + ", expose="
        + expose
        + ", extraHosts="
        + extraHosts
        + ", id='"
        + id
        + '\''
        + ", image='"
        + image
        + '\''
        + ", labels="
        + labels
        + ", links="
        + links
        + ", memLimit="
        + memLimit
        + ", memSwapLimit="
        + memSwapLimit
        + ", networks="
        + networks
        + ", ports="
        + ports
        + ", SecurityOpt="
        + securityOpt
        + ", volumes="
        + volumes
        + ", volumesFrom="
        + volumesFrom
        + ", pidMode='"
        + pidMode
        + '\''
        + ", pidsLimit="
        + pidsLimit
        + ", privileged="
        + privileged
        + '}';
  }
}
