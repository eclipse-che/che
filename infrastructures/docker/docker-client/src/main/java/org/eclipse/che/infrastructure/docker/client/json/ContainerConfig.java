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
package org.eclipse.che.infrastructure.docker.client.json;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.eclipse.che.infrastructure.docker.client.json.container.NetworkingConfig;

/** @author andrew00x */
public class ContainerConfig {
  private String domainName;
  private int cpuShares;
  private String cpuset;
  private boolean attachStdin;
  private boolean attachStdout;
  private boolean attachStderr;
  private boolean tty;
  private boolean openStdin;
  private boolean stdinOnce;
  private String[] env;
  private String[] cmd;
  private String[] entrypoint;
  private String image;
  private boolean networkDisabled;
  private String macAddress;
  private String[] securityOpts;
  private HostConfig hostConfig;
  private NetworkingConfig networkingConfig;

  // from docs for 1.15 API
  // https://docs.docker.com/reference/api/docker_remote_api_v1.15/#create-a-container
  // An object mapping ports to an empty object in the form of: "ExposedPorts": { "<port>/<tcp|udp>:
  // {}" }
  private Map<String, Map<String, String>> exposedPorts = new HashMap<>();
  private String user = "";
  private String hostname = "";
  private String workingDir = "";
  private Map<String, Volume> volumes = new HashMap<>();
  private Map<String, String> labels = new HashMap<>();

  public String getHostname() {
    return hostname;
  }

  public void setHostname(String hostname) {
    this.hostname = hostname;
  }

  public String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
  }

  public String[] getEnv() {
    return env;
  }

  public void setEnv(String[] env) {
    this.env = env;
  }

  /** @deprecated Use {@link HostConfig#getCpuShares()} */
  @Deprecated
  public int getCpuShares() {
    return cpuShares;
  }

  /** @deprecated Use {@link HostConfig#setCpuShares(int)} */
  @Deprecated
  public void setCpuShares(int cpuShares) {
    this.cpuShares = cpuShares;
  }

  public boolean isAttachStdin() {
    return attachStdin;
  }

  public void setAttachStdin(boolean attachStdin) {
    this.attachStdin = attachStdin;
  }

  public boolean isAttachStdout() {
    return attachStdout;
  }

  public void setAttachStdout(boolean attachStdout) {
    this.attachStdout = attachStdout;
  }

  public boolean isAttachStderr() {
    return attachStderr;
  }

  public void setAttachStderr(boolean attachStderr) {
    this.attachStderr = attachStderr;
  }

  public boolean isTty() {
    return tty;
  }

  public void setTty(boolean tty) {
    this.tty = tty;
  }

  public boolean isOpenStdin() {
    return openStdin;
  }

  public void setOpenStdin(boolean openStdin) {
    this.openStdin = openStdin;
  }

  public boolean isStdinOnce() {
    return stdinOnce;
  }

  public void setStdinOnce(boolean stdinOnce) {
    this.stdinOnce = stdinOnce;
  }

  public String[] getCmd() {
    return cmd;
  }

  public void setCmd(String... cmd) {
    this.cmd = cmd;
  }

  public String getImage() {
    return image;
  }

  public void setImage(String image) {
    this.image = image;
  }

  public Map<String, Volume> getVolumes() {
    return volumes;
  }

  public void setVolumes(Map<String, Volume> volumes) {
    this.volumes = volumes;
  }

  public String getWorkingDir() {
    return workingDir;
  }

  public void setWorkingDir(String workingDir) {
    this.workingDir = workingDir;
  }

  public boolean isNetworkDisabled() {
    return networkDisabled;
  }

  public void setNetworkDisabled(boolean networkDisabled) {
    this.networkDisabled = networkDisabled;
  }

  public ContainerConfig withHostname(String hostname) {
    this.hostname = hostname;
    return this;
  }

  public ContainerConfig withUser(String user) {
    this.user = user;
    return this;
  }

  public ContainerConfig withEnv(String... env) {
    this.env = env;
    return this;
  }

  /** @deprecated Use {@link HostConfig#withCpuShares(int)} */
  @Deprecated
  public ContainerConfig withCpuShares(int cpuShares) {
    this.cpuShares = cpuShares;
    return this;
  }

  public ContainerConfig withAttachStdin(boolean attachStdin) {
    this.attachStdin = attachStdin;
    return this;
  }

  public ContainerConfig withAttachStdout(boolean attachStdout) {
    this.attachStdout = attachStdout;
    return this;
  }

  public ContainerConfig withAttachStderr(boolean attachStderr) {
    this.attachStderr = attachStderr;
    return this;
  }

  public ContainerConfig withTty(boolean tty) {
    this.tty = tty;
    return this;
  }

  public ContainerConfig withOpenStdin(boolean openStdin) {
    this.openStdin = openStdin;
    return this;
  }

  public ContainerConfig withStdinOnce(boolean stdinOnce) {
    this.stdinOnce = stdinOnce;
    return this;
  }

  public ContainerConfig withCmd(String... cmd) {
    this.cmd = cmd;
    return this;
  }

  public ContainerConfig withImage(String image) {
    this.image = image;
    return this;
  }

  public ContainerConfig withVolumes(Map<String, Volume> volumes) {
    this.volumes = volumes;
    return this;
  }

  public ContainerConfig withWorkingDir(String workingDir) {
    this.workingDir = workingDir;
    return this;
  }

  public ContainerConfig withNetworkDisabled(boolean networkDisabled) {
    this.networkDisabled = networkDisabled;
    return this;
  }

  public HostConfig getHostConfig() {
    return hostConfig;
  }

  public void setHostConfig(HostConfig hostConfig) {
    this.hostConfig = hostConfig;
  }

  public ContainerConfig withHostConfig(HostConfig hostConfig) {
    this.hostConfig = hostConfig;
    return this;
  }

  public String getDomainName() {
    return domainName;
  }

  public void setDomainName(String domainName) {
    this.domainName = domainName;
  }

  public ContainerConfig withDomainName(String domainName) {
    this.domainName = domainName;
    return this;
  }

  /** @deprecated Use {@link HostConfig#getCpusetCpus()} */
  @Deprecated
  public String getCpuset() {
    return cpuset;
  }

  /** @deprecated Use {@link HostConfig#setCpusetCpus(String)} */
  @Deprecated
  public void setCpuset(String cpuset) {
    this.cpuset = cpuset;
  }

  /** @deprecated Use {@link HostConfig#withCpusetCpus(String)} */
  @Deprecated
  public ContainerConfig withCpuset(String cpuset) {
    this.cpuset = cpuset;
    return this;
  }

  public String[] getEntrypoint() {
    return entrypoint;
  }

  public void setEntrypoint(String... entrypoint) {
    this.entrypoint = entrypoint;
  }

  public ContainerConfig withEntrypoint(String... entrypoint) {
    this.entrypoint = entrypoint;
    return this;
  }

  public String getMacAddress() {
    return macAddress;
  }

  public void setMacAddress(String macAddress) {
    this.macAddress = macAddress;
  }

  public ContainerConfig withMacAddress(String macAddress) {
    this.macAddress = macAddress;
    return this;
  }

  public Map<String, Map<String, String>> getExposedPorts() {
    return exposedPorts;
  }

  public void setExposedPorts(Map<String, Map<String, String>> exposedPorts) {
    this.exposedPorts = exposedPorts;
  }

  public ContainerConfig withExposedPorts(Map<String, Map<String, String>> exposedPorts) {
    this.exposedPorts = exposedPorts;
    return this;
  }

  /*
   * There is no "SecurityOpts" in the docker specification, only "HostConfig":{"SecurityOpt":[]}.
   */
  @Deprecated
  public String[] getSecurityOpts() {
    return securityOpts;
  }

  @Deprecated
  public void setSecurityOpts(String[] securityOpts) {
    this.securityOpts = securityOpts;
  }

  @Deprecated
  public ContainerConfig withSecurityOpts(String[] securityOpts) {
    this.securityOpts = securityOpts;
    return this;
  }

  public Map<String, String> getLabels() {
    return labels;
  }

  public void setLabels(Map<String, String> labels) {
    this.labels = labels;
  }

  public ContainerConfig withLabels(Map<String, String> labels) {
    this.labels = labels;
    return this;
  }

  public NetworkingConfig getNetworkingConfig() {
    return networkingConfig;
  }

  public void setNetworkingConfig(NetworkingConfig networkingConfig) {
    this.networkingConfig = networkingConfig;
  }

  public ContainerConfig withNetworkingConfig(NetworkingConfig networkingConfig) {
    this.networkingConfig = networkingConfig;
    return this;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof ContainerConfig)) {
      return false;
    }
    final ContainerConfig that = (ContainerConfig) obj;
    return cpuShares == that.cpuShares
        && attachStdin == that.attachStdin
        && attachStdout == that.attachStdout
        && attachStderr == that.attachStderr
        && tty == that.tty
        && openStdin == that.openStdin
        && stdinOnce == that.stdinOnce
        && networkDisabled == that.networkDisabled
        && Objects.equals(domainName, that.domainName)
        && Objects.equals(cpuset, that.cpuset)
        && Arrays.equals(env, that.env)
        && Arrays.equals(cmd, that.cmd)
        && Arrays.equals(entrypoint, that.entrypoint)
        && Objects.equals(image, that.image)
        && Objects.equals(macAddress, that.macAddress)
        && Arrays.equals(securityOpts, that.securityOpts)
        && Objects.equals(hostConfig, that.hostConfig)
        && Objects.equals(networkingConfig, that.networkingConfig)
        && getExposedPorts().equals(that.getExposedPorts())
        && Objects.equals(user, that.user)
        && Objects.equals(hostname, that.hostname)
        && Objects.equals(workingDir, that.workingDir)
        && getVolumes().equals(that.getVolumes())
        && getLabels().equals(that.getLabels());
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 31 * hash + Objects.hashCode(domainName);
    hash = 31 * hash + cpuShares;
    hash = 31 * hash + Objects.hashCode(cpuset);
    hash = 31 * hash + Boolean.hashCode(attachStdin);
    hash = 31 * hash + Boolean.hashCode(attachStdout);
    hash = 31 * hash + Boolean.hashCode(attachStderr);
    hash = 31 * hash + Boolean.hashCode(tty);
    hash = 31 * hash + Boolean.hashCode(openStdin);
    hash = 31 * hash + Boolean.hashCode(stdinOnce);
    hash = 31 * hash + Arrays.hashCode(env);
    hash = 31 * hash + Arrays.hashCode(cmd);
    hash = 31 * hash + Arrays.hashCode(entrypoint);
    hash = 31 * hash + Objects.hashCode(image);
    hash = 31 * hash + Boolean.hashCode(networkDisabled);
    hash = 31 * hash + Objects.hashCode(macAddress);
    hash = 31 * hash + Arrays.hashCode(securityOpts);
    hash = 31 * hash + Objects.hashCode(hostConfig);
    hash = 31 * hash + Objects.hashCode(networkingConfig);
    hash = 31 * hash + getExposedPorts().hashCode();
    hash = 31 * hash + Objects.hashCode(user);
    hash = 31 * hash + Objects.hashCode(hostname);
    hash = 31 * hash + Objects.hashCode(workingDir);
    hash = 31 * hash + getVolumes().hashCode();
    hash = 31 * hash + getLabels().hashCode();
    return hash;
  }

  @Override
  public String toString() {
    return "ContainerConfig{"
        + "domainName='"
        + domainName
        + '\''
        + ", cpuShares="
        + cpuShares
        + ", cpuset='"
        + cpuset
        + '\''
        + ", attachStdin="
        + attachStdin
        + ", attachStdout="
        + attachStdout
        + ", attachStderr="
        + attachStderr
        + ", tty="
        + tty
        + ", openStdin="
        + openStdin
        + ", stdinOnce="
        + stdinOnce
        + ", env="
        + Arrays.toString(env)
        + ", cmd="
        + Arrays.toString(cmd)
        + ", entrypoint="
        + Arrays.toString(entrypoint)
        + ", image='"
        + image
        + '\''
        + ", networkDisabled="
        + networkDisabled
        + ", macAddress='"
        + macAddress
        + '\''
        + ", securityOpts="
        + Arrays.toString(securityOpts)
        + ", hostConfig="
        + hostConfig
        + ", networkingConfig="
        + networkingConfig
        + ", exposedPorts="
        + exposedPorts
        + ", user='"
        + user
        + '\''
        + ", hostname='"
        + hostname
        + '\''
        + ", workingDir='"
        + workingDir
        + '\''
        + ", volumes="
        + volumes
        + ", labels="
        + labels
        + '}';
  }
}
