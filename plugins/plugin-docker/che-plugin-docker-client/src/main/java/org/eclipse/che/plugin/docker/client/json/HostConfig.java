/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.docker.client.json;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** @author andrew00x */
public class HostConfig {
    private String[]       binds;
    private String[]       links;
    private LxcConfParam[] lxcConf;
    private boolean        publishAllPorts;
    private boolean        privileged;
    private String[]       dns;
    private String[]       dnsSearch;
    private String[]       extraHosts;
    private String[]       volumesFrom;
    private String[]       capAdd;
    private String[]       capDrop;
    private RestartPolicy  restartPolicy;
    private String         networkMode;
    private String[]       devices;
    private String         containerIDFile;
    private long           memory;
    private long           memorySwap;
    private LogConfig      logConfig;
    private String         ipcMode;
    private String         cgroupParent;
    private int            cpuShares;
    private String         cpusetCpus;
    private String         pidMode;
    private boolean        readonlyRootfs;
    private Ulimit[]       ulimits;

    private Map<String, PortBinding[]> portBindings     = new HashMap<>();
    private int                        memorySwappiness = -1;
    private int                        pidsLimit        = -1;

    public String[] getBinds() {
        return binds;
    }

    public void setBinds(String[] binds) {
        this.binds = binds;
    }

    public LxcConfParam[] getLxcConf() {
        return lxcConf;
    }

    public void setLxcConf(LxcConfParam[] lxcConf) {
        this.lxcConf = lxcConf;
    }

    public boolean isPrivileged() {
        return privileged;
    }

    public void setPrivileged(boolean privileged) {
        this.privileged = privileged;
    }

    public Map<String, PortBinding[]> getPortBindings() {
        return portBindings;
    }

    public void setPortBindings(Map<String, PortBinding[]> portBindings) {
        this.portBindings = portBindings;
    }

    public boolean isPublishAllPorts() {
        return publishAllPorts;
    }

    public void setPublishAllPorts(boolean publishAllPorts) {
        this.publishAllPorts = publishAllPorts;
    }

    public void setPidsLimit(int pidsLimit) {
        this.pidsLimit = pidsLimit;
    }

    public HostConfig withBinds(String... binds) {
        this.binds = binds;
        return this;
    }

    public HostConfig withLxcConf(LxcConfParam... lxcConf) {
        this.lxcConf = lxcConf;
        return this;
    }

    public HostConfig withPrivileged(boolean privileged) {
        this.privileged = privileged;
        return this;
    }

    public HostConfig withPortBindings(Map<String, PortBinding[]> portBindings) {
        this.portBindings = portBindings;
        return this;
    }

    public HostConfig withPublishAllPorts(boolean publishAllPorts) {
        this.publishAllPorts = publishAllPorts;
        return this;
    }

    public HostConfig withPidsLimit(int pidsLimit) {
        this.pidsLimit = pidsLimit;
        return this;
    }

    public String[] getDevices() {
        return devices;
    }

    public void setDevices(String[] devices) {
        this.devices = devices;
    }

    public HostConfig withDevices(String[] devices) {
        this.devices = devices;
        return this;
    }

    public String getNetworkMode() {
        return networkMode;
    }

    public void setNetworkMode(String networkMode) {
        this.networkMode = networkMode;
    }

    public HostConfig withNetworkMode(String networkMode) {
        this.networkMode = networkMode;
        return this;
    }

    public RestartPolicy getRestartPolicy() {
        return restartPolicy;
    }

    public void setRestartPolicy(RestartPolicy restartPolicy) {
        this.restartPolicy = restartPolicy;
    }

    public HostConfig withRestartPolicy(RestartPolicy restartPolicy) {
        this.restartPolicy = restartPolicy;
        return this;
    }

    public String[] getCapDrop() {
        return capDrop;
    }

    public void setCapDrop(String[] capDrop) {
        this.capDrop = capDrop;
    }

    public HostConfig withCapDrop(String[] capDrop) {
        this.capDrop = capDrop;
        return this;
    }

    public String[] getCapAdd() {
        return capAdd;
    }

    public void setCapAdd(String[] capAdd) {
        this.capAdd = capAdd;
    }

    public HostConfig withCapAdd(String[] capAdd) {
        this.capAdd = capAdd;
        return this;
    }

    public String[] getVolumesFrom() {
        return volumesFrom;
    }

    public void setVolumesFrom(String[] volumesFrom) {
        this.volumesFrom = volumesFrom;
    }

    public HostConfig withVolumesFrom(String[] volumesFrom) {
        this.volumesFrom = volumesFrom;
        return this;
    }

    public String[] getExtraHosts() {
        return extraHosts;
    }

    public void setExtraHosts(String[] extraHosts) {
        this.extraHosts = extraHosts;
    }

    public HostConfig withExtraHosts(String[] extraHosts) {
        this.extraHosts = extraHosts;
        return this;
    }

    public String[] getDnsSearch() {
        return dnsSearch;
    }

    public void setDnsSearch(String[] dnsSearch) {
        this.dnsSearch = dnsSearch;
    }

    public HostConfig withDnsSearch(String[] dnsSearch) {
        this.dnsSearch = dnsSearch;
        return this;
    }

    public String[] getDns() {
        return dns;
    }

    public void setDns(String[] dns) {
        this.dns = dns;
    }

    public HostConfig withDns(String[] dns) {
        this.dns = dns;
        return this;
    }

    public String[] getLinks() {
        return links;
    }

    public void setLinks(String[] links) {
        this.links = links;
    }

    public HostConfig withLinks(String[] links) {
        this.links = links;
        return this;
    }

    public String getContainerIDFile() {
        return containerIDFile;
    }

    public void setContainerIDFile(String containerIDFile) {
        this.containerIDFile = containerIDFile;
    }

    public HostConfig withContainerIDFile(String containerIDFile) {
        this.containerIDFile = containerIDFile;
        return this;
    }

    public long getMemory() {
        return memory;
    }

    public long getPidsLimit() {
        return pidsLimit;
    }

    public void setMemory(long memory) {
        this.memory = memory;
    }

    public HostConfig withMemory(long memory) {
        this.memory = memory;
        return this;
    }

    public long getMemorySwap() {
        return memorySwap;
    }

    public void setMemorySwap(long memorySwap) {
        this.memorySwap = memorySwap;
    }

    public HostConfig withMemorySwap(long memorySwap) {
        this.memorySwap = memorySwap;
        return this;
    }

    public int getMemorySwappiness() {
        return memorySwappiness;
    }

    public void setMemorySwappiness(int memorySwappiness) {
        this.memorySwappiness = memorySwappiness;
    }

    public HostConfig withMemorySwappiness(int memorySwappiness) {
        this.memorySwappiness = memorySwappiness;
        return this;
    }

    public LogConfig getLogConfig() {
        return logConfig;
    }

    public void setLogConfig(LogConfig logConfig) {
        this.logConfig = logConfig;
    }

    public HostConfig withLogConfig(LogConfig logConfig) {
        this.logConfig = logConfig;
        return this;
    }

    public String getIpcMode() {
        return ipcMode;
    }

    public void setIpcMode(String ipcMode) {
        this.ipcMode = ipcMode;
    }

    public HostConfig withIpcMode(String ipcMode) {
        this.ipcMode = ipcMode;
        return this;
    }

    public String getCgroupParent() {
        return cgroupParent;
    }

    public void setCgroupParent(String cgroupParent) {
        this.cgroupParent = cgroupParent;
    }

    public HostConfig withCgroupParent(String cgroupParent) {
        this.cgroupParent = cgroupParent;
        return this;
    }

    public int getCpuShares() {
        return cpuShares;
    }

    public void setCpuShares(int cpuShares) {
        this.cpuShares = cpuShares;
    }

    public HostConfig withCpuShares(int cpuShares) {
        this.cpuShares = cpuShares;
        return this;
    }

    public String getCpusetCpus() {
        return cpusetCpus;
    }

    public void setCpusetCpus(String cpusetCpus) {
        this.cpusetCpus = cpusetCpus;
    }

    public HostConfig withCpusetCpus(String cpusetCpus) {
        this.cpusetCpus = cpusetCpus;
        return this;
    }

    public String getPidMode() {
        return pidMode;
    }

    public void setPidMode(String pidMode) {
        this.pidMode = pidMode;
    }

    public HostConfig withPidMode(String pidMode) {
        this.pidMode = pidMode;
        return this;
    }

    public boolean isReadonlyRootfs() {
        return readonlyRootfs;
    }

    public void setReadonlyRootfs(boolean readonlyRootfs) {
        this.readonlyRootfs = readonlyRootfs;
    }

    public HostConfig withReadonlyRootfs(boolean readonlyRootfs) {
        this.readonlyRootfs = readonlyRootfs;
        return this;
    }

    public Ulimit[] getUlimits() {
        return ulimits;
    }

    public void setUlimits(Ulimit[] ulimits) {
        this.ulimits = ulimits;
    }

    public HostConfig withUlimits(Ulimit[] ulimits) {
        this.ulimits = ulimits;
        return this;
    }

    @Override
    public String toString() {
        return "HostConfig{" +
               "binds=" + Arrays.toString(binds) +
               ", links=" + Arrays.toString(links) +
               ", lxcConf=" + Arrays.toString(lxcConf) +
               ", publishAllPorts=" + publishAllPorts +
               ", privileged=" + privileged +
               ", dns=" + Arrays.toString(dns) +
               ", dnsSearch=" + Arrays.toString(dnsSearch) +
               ", extraHosts=" + Arrays.toString(extraHosts) +
               ", volumesFrom=" + Arrays.toString(volumesFrom) +
               ", capAdd=" + Arrays.toString(capAdd) +
               ", capDrop=" + Arrays.toString(capDrop) +
               ", restartPolicy=" + restartPolicy +
               ", networkMode='" + networkMode + '\'' +
               ", devices=" + Arrays.toString(devices) +
               ", containerIDFile='" + containerIDFile + '\'' +
               ", memory='" + memory + '\'' +
               ", memorySwap=" + memorySwap +
               ", logConfig=" + logConfig +
               ", ipcMode='" + ipcMode + '\'' +
               ", cgroupParent='" + cgroupParent + '\'' +
               ", cpuShares=" + cpuShares +
               ", cpusetCpus='" + cpusetCpus + '\'' +
               ", pidMode='" + pidMode + '\'' +
               ", pidsLimit='" + pidsLimit + '\'' +
               ", readonlyRootfs=" + readonlyRootfs +
               ", ulimits=" + Arrays.toString(ulimits) +
               ", portBindings=" + portBindings +
               ", memorySwappiness=" + memorySwappiness +
               '}';
    }
}
