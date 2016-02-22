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

import org.eclipse.che.commons.lang.Size;

import java.util.Arrays;

/**
 * @author andrew00x
 */
public class SystemInfo {
    public static final String DRIVER_STATE_DATA_SPACE_TOTAL     = "Data Space Total";
    public static final String DRIVER_STATE_DATA_SPACE_USED      = "Data Space Used";
    public static final String DRIVER_STATE_METADATA_SPACE_TOTAL = "Metadata Space Total";
    public static final String DRIVER_STATE_METADATA_SPACE_USED  = "Metadata Space Used";

    private int        containers;
    private int        images;
    private int        debug;
    private String     driver;
    private String     executionDriver;
    private int        IPv4Forwarding;
    private String     indexServerAddress;
    private String     initPath;
    private String     initSha1;
    private String     kernelVersion;
    private long       memoryLimit;
    private int        nEventsListener;
    private int        nFd;
    private int        nGoroutines;
    private String     operatingSystem;
    private long       swapLimit;
    // Json has ugly format for this parameters, e.g.
    // "DriverStatus": [
    //     [
    //         "Pool Name",
    //         "docker-253:0-8390592-pool"
    //     ],
    //     [
    //         "Pool Blocksize",
    //         "65.54 kB"
    //     ],
    //     [
    //         "Data file",
    //         "/var/lib/docker/devicemapper/devicemapper/data"
    //     ],
    //     [
    //        "Metadata file",
    //         "/var/lib/docker/devicemapper/devicemapper/metadata"
    //     ],
    //     ....
    // ]
    // So seems two-dimensional array is simplest solution for model.
    private String[][] driverStatus;
    // data was moved here from driver status in recent docker-swarm versions
    private String[][] systemStatus;
    private int        nCPU;
    private long       memTotal;
    private String     name;
    private String     iD;
    private String     systemTime;
    private String[]   labels;
    private String     dockerRootDir;
    private String     httpProxy;
    private String     httpsProxy;
    private String     noProxy;

    public int getContainers() {
        return containers;
    }

    public void setContainers(int containers) {
        this.containers = containers;
    }

    public int getImages() {
        return images;
    }

    public void setImages(int images) {
        this.images = images;
    }

    public int getDebug() {
        return debug;
    }

    public void setDebug(int debug) {
        this.debug = debug;
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public String getExecutionDriver() {
        return executionDriver;
    }

    public void setExecutionDriver(String executionDriver) {
        this.executionDriver = executionDriver;
    }

    public int getIPv4Forwarding() {
        return IPv4Forwarding;
    }

    public void setIPv4Forwarding(int IPv4Forwarding) {
        this.IPv4Forwarding = IPv4Forwarding;
    }

    public String getIndexServerAddress() {
        return indexServerAddress;
    }

    public void setIndexServerAddress(String indexServerAddress) {
        this.indexServerAddress = indexServerAddress;
    }

    public String getInitPath() {
        return initPath;
    }

    public void setInitPath(String initPath) {
        this.initPath = initPath;
    }

    public String getInitSha1() {
        return initSha1;
    }

    public void setInitSha1(String initSha1) {
        this.initSha1 = initSha1;
    }

    public String getKernelVersion() {
        return kernelVersion;
    }

    public void setKernelVersion(String kernelVersion) {
        this.kernelVersion = kernelVersion;
    }

    public long getMemoryLimit() {
        return memoryLimit;
    }

    public void setMemoryLimit(long memoryLimit) {
        this.memoryLimit = memoryLimit;
    }

    public int getnEventsListener() {
        return nEventsListener;
    }

    public void setnEventsListener(int nEventsListener) {
        this.nEventsListener = nEventsListener;
    }

    public int getnFd() {
        return nFd;
    }

    public void setnFd(int nFd) {
        this.nFd = nFd;
    }

    public int getnGoroutines() {
        return nGoroutines;
    }

    public void setnGoroutines(int nGoroutines) {
        this.nGoroutines = nGoroutines;
    }

    public String getOperatingSystem() {
        return operatingSystem;
    }

    public void setOperatingSystem(String operatingSystem) {
        this.operatingSystem = operatingSystem;
    }

    public long getSwapLimit() {
        return swapLimit;
    }

    public void setSwapLimit(long swapLimit) {
        this.swapLimit = swapLimit;
    }

    public String[][] getDriverStatus() {
        return driverStatus;
    }

    public void setDriverStatus(String[][] driverStatus) {
        this.driverStatus = driverStatus;
    }

    // ---------------------------------------------------------------------------
    // Methods for access DriverStatus information in comfortable way.
    // Getters don't have "get" prefixes to avoid mixing with methods for accessing info provided by docker API in json format.

    public String statusField(String fieldName) {
        if (driverStatus == null) {
            return null;
        }
        for (int i = 0, l = driverStatus.length; i < l; i++) {
            String[] driverStatusEntry = driverStatus[i];
            if (driverStatusEntry.length == 2) {
                if (fieldName.equals(driverStatusEntry[0])) {
                    return driverStatusEntry[1];
                }
            }
        }
        return null;
    }

    public String[][] getSystemStatus() {
        return systemStatus;
    }

    public void setSystemStatus(String[][] systemStatus) {
        this.systemStatus = systemStatus;
    }

    /** Gets total space for storing images or {@code -1} if required information is not available from docker API or has unexpected format. */
    public long dataSpaceTotal() {
        final String str = statusField(DRIVER_STATE_DATA_SPACE_TOTAL);
        if (str == null) {
            return -1;
        }
        return Size.parseSize(str);
    }

    /** Gets used space for storing images or {@code -1} if required information is not available from docker API or has unexpected format. */
    public long dataSpaceUsed() {
        final String str = statusField(DRIVER_STATE_DATA_SPACE_USED);
        if (str == null) {
            return -1;
        }
        return Size.parseSize(str);
    }

    /**
     * Gets total space for storing images' metadata or {@code -1} if required information is not available from docker API or has
     * unexpected format.
     */
    public long metadataSpaceTotal() {
        final String str = statusField(DRIVER_STATE_METADATA_SPACE_TOTAL);
        if (str == null) {
            return -1;
        }
        return Size.parseSize(str);
    }

    /**
     * Gets used space for storing images' metadata or {@code -1} if required information is not available from docker API or has
     * unexpected format.
     */
    public long metadataSpaceUsed() {
        final String str = statusField(DRIVER_STATE_METADATA_SPACE_USED);
        if (str == null) {
            return -1;
        }
        return Size.parseSize(str);
    }

    public int getnCPU() {
        return nCPU;
    }

    public void setnCPU(int nCPU) {
        this.nCPU = nCPU;
    }

    public long getMemTotal() {
        return memTotal;
    }

    public void setMemTotal(long memTotal) {
        this.memTotal = memTotal;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getiD() {
        return iD;
    }

    public void setiD(String iD) {
        this.iD = iD;
    }

    public String getSystemTime() {
        return systemTime;
    }

    public void setSystemTime(String systemTime) {
        this.systemTime = systemTime;
    }

    public String[] getLabels() {
        return labels;
    }

    public void setLabels(String[] labels) {
        this.labels = labels;
    }

    public String getDockerRootDir() {
        return dockerRootDir;
    }

    public void setDockerRootDir(String dockerRootDir) {
        this.dockerRootDir = dockerRootDir;
    }

    public String getHttpProxy() {
        return httpProxy;
    }

    public void setHttpProxy(String httpProxy) {
        this.httpProxy = httpProxy;
    }

    public String getHttpsProxy() {
        return httpsProxy;
    }

    public void setHttpsProxy(String httpsProxy) {
        this.httpsProxy = httpsProxy;
    }

    public String getNoProxy() {
        return noProxy;
    }

    public void setNoProxy(String noProxy) {
        this.noProxy = noProxy;
    }

    @Override
    public String toString() {
        return "SystemInfo{" +
               "containers=" + containers +
               ", images=" + images +
               ", debug=" + debug +
               ", driver='" + driver + '\'' +
               ", executionDriver='" + executionDriver + '\'' +
               ", IPv4Forwarding=" + IPv4Forwarding +
               ", indexServerAddress='" + indexServerAddress + '\'' +
               ", initPath='" + initPath + '\'' +
               ", initSha1='" + initSha1 + '\'' +
               ", kernelVersion='" + kernelVersion + '\'' +
               ", memoryLimit=" + memoryLimit +
               ", nEventsListener=" + nEventsListener +
               ", nFd=" + nFd +
               ", nGoroutines=" + nGoroutines +
               ", operatingSystem='" + operatingSystem + '\'' +
               ", swapLimit=" + swapLimit +
               ", driverStatus=" + Arrays.toString(driverStatus) +
               ", nCPU=" + nCPU +
               ", memTotal=" + memTotal +
               ", name='" + name + '\'' +
               ", iD='" + iD + '\'' +
               ", systemTime='" + systemTime + '\'' +
               ", labels=" + Arrays.toString(labels) +
               ", dockerRootDir='" + dockerRootDir + '\'' +
               ", httpProxy='" + httpProxy + '\'' +
               ", httpsProxy='" + httpsProxy + '\'' +
               ", noProxy='" + noProxy + '\'' +
               '}';
    }
}
