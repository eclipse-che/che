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
package org.eclipse.che.infrastructure.docker.client.json;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** @author andrew00x */
public class ImageConfig {
  private boolean attachStderr;
  private boolean attachStdin;
  private boolean attachStdout;
  private String[] cmd;
  private int cpuShares;
  private String cpuset;
  private String domainname;
  private String[] entrypoint;
  private String[] env;
  private Map<String, ExposedPort> exposedPorts;
  private String hostname;
  private String image;
  private long memory;
  private long memorySwap;
  private boolean networkDisabled;
  private String[] onBuild;
  private boolean openStdin;
  // From docker code:
  // We will receive port specs in the format of ip:public:private/proto
  private String[] portSpecs;
  private boolean stdinOnce;
  private boolean tty;
  private String user;
  private String macAddress;

  private Map<String, String> labels = new HashMap<>();
  private Map<String, Volume> volumes = new HashMap<>();
  private String workingDir = "";

  public boolean isAttachStderr() {
    return attachStderr;
  }

  public void setAttachStderr(boolean attachStderr) {
    this.attachStderr = attachStderr;
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

  public String[] getCmd() {
    return cmd;
  }

  public void setCmd(String[] cmd) {
    this.cmd = cmd;
  }

  public int getCpuShares() {
    return cpuShares;
  }

  public void setCpuShares(int cpuShares) {
    this.cpuShares = cpuShares;
  }

  public String getCpuset() {
    return cpuset;
  }

  public void setCpuset(String cpuset) {
    this.cpuset = cpuset;
  }

  public String getDomainname() {
    return domainname;
  }

  public void setDomainname(String domainname) {
    this.domainname = domainname;
  }

  public String[] getEntrypoint() {
    return entrypoint;
  }

  public void setEntrypoint(String[] entrypoint) {
    this.entrypoint = entrypoint;
  }

  public String[] getEnv() {
    return env;
  }

  public void setEnv(String[] env) {
    this.env = env;
  }

  public Map<String, ExposedPort> getExposedPorts() {
    return exposedPorts;
  }

  public void setExposedPorts(Map<String, ExposedPort> exposedPorts) {
    this.exposedPorts = exposedPorts;
  }

  public String getHostname() {
    return hostname;
  }

  public void setHostname(String hostname) {
    this.hostname = hostname;
  }

  public String getImage() {
    return image;
  }

  public void setImage(String image) {
    this.image = image;
  }

  public long getMemory() {
    return memory;
  }

  public void setMemory(long memory) {
    this.memory = memory;
  }

  public long getMemorySwap() {
    return memorySwap;
  }

  public void setMemorySwap(long memorySwap) {
    this.memorySwap = memorySwap;
  }

  public boolean isNetworkDisabled() {
    return networkDisabled;
  }

  public void setNetworkDisabled(boolean networkDisabled) {
    this.networkDisabled = networkDisabled;
  }

  public String[] getOnBuild() {
    return onBuild;
  }

  public void setOnBuild(String[] onBuild) {
    this.onBuild = onBuild;
  }

  public boolean isOpenStdin() {
    return openStdin;
  }

  public void setOpenStdin(boolean openStdin) {
    this.openStdin = openStdin;
  }

  public String[] getPortSpecs() {
    return portSpecs;
  }

  public void setPortSpecs(String[] portSpecs) {
    this.portSpecs = portSpecs;
  }

  public boolean isStdinOnce() {
    return stdinOnce;
  }

  public void setStdinOnce(boolean stdinOnce) {
    this.stdinOnce = stdinOnce;
  }

  public boolean isTty() {
    return tty;
  }

  public void setTty(boolean tty) {
    this.tty = tty;
  }

  public String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
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

  public String getMacAddress() {
    return macAddress;
  }

  public void setMacAddress(String macAddress) {
    this.macAddress = macAddress;
  }

  public Map<String, String> getLabels() {
    return labels;
  }

  public void setLabels(Map<String, String> labels) {
    this.labels = labels;
  }

  @Override
  public String toString() {
    return "ImageConfig{"
        + "attachStderr="
        + attachStderr
        + ", attachStdin="
        + attachStdin
        + ", attachStdout="
        + attachStdout
        + ", cmd="
        + Arrays.toString(cmd)
        + ", cpuShares="
        + cpuShares
        + ", cpuset='"
        + cpuset
        + '\''
        + ", domainname='"
        + domainname
        + '\''
        + ", entrypoint="
        + Arrays.toString(entrypoint)
        + ", env="
        + Arrays.toString(env)
        + ", exposedPorts="
        + exposedPorts
        + ", hostname='"
        + hostname
        + '\''
        + ", image='"
        + image
        + '\''
        + ", memory="
        + memory
        + ", memorySwap="
        + memorySwap
        + ", networkDisabled="
        + networkDisabled
        + ", onBuild="
        + Arrays.toString(onBuild)
        + ", openStdin="
        + openStdin
        + ", portSpecs="
        + Arrays.toString(portSpecs)
        + ", stdinOnce="
        + stdinOnce
        + ", tty="
        + tty
        + ", user='"
        + user
        + '\''
        + ", macAddress='"
        + macAddress
        + '\''
        + ", labels="
        + labels
        + ", volumes="
        + volumes
        + ", workingDir='"
        + workingDir
        + '\''
        + '}';
  }
}
