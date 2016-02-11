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
public class ContainerInfo {
    private String          id;
    // Date format: yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX
    private String          created;
    private String          appArmorProfile;
    private String          path;
    private String[]        args;
    private ContainerConfig config;
    private ContainerState  state;
    private String          image;
    private NetworkSettings networkSettings;
    private String          resolvConfPath;
    private HostConfig      hostConfig;
    private String          driver;
    private String          execDriver;
    private String          hostnamePath;
    private String          hostsPath;
    private String          mountLabel;
    private String          name;
    private String          processLabel;
    private String[]        execIDs;
    private int             restartCount;
    private String          logPath;

    private Map<String, String>  volumes   = new HashMap<>();
    private Map<String, Boolean> volumesRW = new HashMap<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String[] getArgs() {
        return args;
    }

    public void setArgs(String[] args) {
        this.args = args;
    }

    public ContainerConfig getConfig() {
        return config;
    }

    public void setConfig(ContainerConfig config) {
        this.config = config;
    }

    public ContainerState getState() {
        return state;
    }

    public void setState(ContainerState state) {
        this.state = state;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public NetworkSettings getNetworkSettings() {
        return networkSettings;
    }

    public void setNetworkSettings(NetworkSettings networkSettings) {
        this.networkSettings = networkSettings;
    }

    public String getResolvConfPath() {
        return resolvConfPath;
    }

    public void setResolvConfPath(String resolvConfPath) {
        this.resolvConfPath = resolvConfPath;
    }

    public Map<String, String> getVolumes() {
        return volumes;
    }

    public void setVolumes(Map<String, String> volumes) {
        this.volumes = volumes;
    }

    public HostConfig getHostConfig() {
        return hostConfig;
    }

    public void setHostConfig(HostConfig hostConfig) {
        this.hostConfig = hostConfig;
    }

    public Map<String, Boolean> getVolumesRW() {
        return volumesRW;
    }

    public void setVolumesRW(Map<String, Boolean> volumesRW) {
        this.volumesRW = volumesRW;
    }

    public String getAppArmorProfile() {
        return appArmorProfile;
    }

    public void setAppArmorProfile(String appArmorProfile) {
        this.appArmorProfile = appArmorProfile;
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public String getExecDriver() {
        return execDriver;
    }

    public void setExecDriver(String execDriver) {
        this.execDriver = execDriver;
    }

    public String getHostnamePath() {
        return hostnamePath;
    }

    public void setHostnamePath(String hostnamePath) {
        this.hostnamePath = hostnamePath;
    }

    public String getHostsPath() {
        return hostsPath;
    }

    public void setHostsPath(String hostsPath) {
        this.hostsPath = hostsPath;
    }

    public String getMountLabel() {
        return mountLabel;
    }

    public void setMountLabel(String mountLabel) {
        this.mountLabel = mountLabel;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProcessLabel() {
        return processLabel;
    }

    public void setProcessLabel(String processLabel) {
        this.processLabel = processLabel;
    }

    public String[] getExecIDs() {
        return execIDs;
    }

    public void setExecIDs(String[] execIDs) {
        this.execIDs = execIDs;
    }

    public int getRestartCount() {
        return restartCount;
    }

    public void setRestartCount(int restartCount) {
        this.restartCount = restartCount;
    }

    public String getLogPath() {
        return logPath;
    }

    public void setLogPath(String logPath) {
        this.logPath = logPath;
    }

    @Override
    public String toString() {
        return "ContainerInfo{" +
               "id='" + id + '\'' +
               ", created='" + created + '\'' +
               ", appArmorProfile='" + appArmorProfile + '\'' +
               ", path='" + path + '\'' +
               ", args=" + Arrays.toString(args) +
               ", config=" + config +
               ", state=" + state +
               ", image='" + image + '\'' +
               ", networkSettings=" + networkSettings +
               ", resolvConfPath='" + resolvConfPath + '\'' +
               ", hostConfig=" + hostConfig +
               ", driver='" + driver + '\'' +
               ", execDriver='" + execDriver + '\'' +
               ", hostnamePath='" + hostnamePath + '\'' +
               ", hostsPath='" + hostsPath + '\'' +
               ", mountLabel='" + mountLabel + '\'' +
               ", name='" + name + '\'' +
               ", processLabel='" + processLabel + '\'' +
               ", execIDs=" + Arrays.toString(execIDs) +
               ", restartCount=" + restartCount +
               ", logPath='" + logPath + '\'' +
               ", volumes=" + volumes +
               ", volumesRW=" + volumesRW +
               '}';
    }
}
