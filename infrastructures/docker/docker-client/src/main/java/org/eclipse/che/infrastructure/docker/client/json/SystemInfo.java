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
import java.util.Objects;
import org.eclipse.che.commons.lang.Size;

/**
 * @author andrew00x
 * @author Alexander Garagatyi
 */
public class SystemInfo {
  public static final String DRIVER_STATE_DATA_SPACE_TOTAL = "Data Space Total";
  public static final String DRIVER_STATE_DATA_SPACE_USED = "Data Space Used";
  public static final String DRIVER_STATE_METADATA_SPACE_TOTAL = "Metadata Space Total";
  public static final String DRIVER_STATE_METADATA_SPACE_USED = "Metadata Space Used";

  // Fields are sorted in alphabetical order to eas comparison with docker docs and output
  // Architecture
  // BridgeNfIptables
  // BridgeNfIp6tables
  // ClusterAdvertise
  // ClusterStore
  private int containers;
  // ContainersPaused
  // ContainersRunning
  // ContainersStopped
  // CPUSet
  // CpuCfsPeriod
  // CpuCfsQuota
  // CPUShares
  private boolean debug;
  private String dockerRootDir;
  private String driver;
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
  private String executionDriver;
  // ExperimentalBuild
  private String httpProxy;
  private String httpsProxy;
  private String iD;
  private boolean iPv4Forwarding;
  private int images;
  private String indexServerAddress;
  private String initPath;
  private String initSha1;
  // KernelMemory
  private String kernelVersion;
  private String[] labels;
  // LoggingDriver
  // CgroupDriver
  private long memTotal;
  private boolean memoryLimit;
  private int nCPU;
  private int nEventsListener;
  private int nFd;
  private int nGoroutines;
  private String name;
  private String noProxy;
  // OomKillDisable
  private String operatingSystem;
  // OSType
  // Plugins
  // RegistryConfig
  // ServerVersion
  private boolean swapLimit;
  private String systemTime;
  // data was moved here from driver status in recent docker-swarm versions
  private String[][] systemStatus;

  // ---------------------------------------------------------------------------
  // Methods for access DriverStatus information in comfortable way.
  // Getters don't have "get" prefixes to avoid mixing with methods for accessing info provided by
  // docker API in json format.
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

  /**
   * Gets total space for storing images or {@code -1} if required information is not available from
   * docker API or has unexpected format.
   */
  public long dataSpaceTotal() {
    final String str = statusField(DRIVER_STATE_DATA_SPACE_TOTAL);
    if (str == null) {
      return -1;
    }
    return Size.parseSize(str);
  }

  /**
   * Gets used space for storing images or {@code -1} if required information is not available from
   * docker API or has unexpected format.
   */
  public long dataSpaceUsed() {
    final String str = statusField(DRIVER_STATE_DATA_SPACE_USED);
    if (str == null) {
      return -1;
    }
    return Size.parseSize(str);
  }

  /**
   * Gets total space for storing images' metadata or {@code -1} if required information is not
   * available from docker API or has unexpected format.
   */
  public long metadataSpaceTotal() {
    final String str = statusField(DRIVER_STATE_METADATA_SPACE_TOTAL);
    if (str == null) {
      return -1;
    }
    return Size.parseSize(str);
  }

  /**
   * Gets used space for storing images' metadata or {@code -1} if required information is not
   * available from docker API or has unexpected format.
   */
  public long metadataSpaceUsed() {
    final String str = statusField(DRIVER_STATE_METADATA_SPACE_USED);
    if (str == null) {
      return -1;
    }
    return Size.parseSize(str);
  }
  // --------------------------------------------------------------------------- //

  public int getContainers() {
    return containers;
  }

  public void setContainers(int containers) {
    this.containers = containers;
  }

  public boolean isDebug() {
    return debug;
  }

  public void setDebug(boolean debug) {
    this.debug = debug;
  }

  public String getDockerRootDir() {
    return dockerRootDir;
  }

  public void setDockerRootDir(String dockerRootDir) {
    this.dockerRootDir = dockerRootDir;
  }

  public String getDriver() {
    return driver;
  }

  public void setDriver(String driver) {
    this.driver = driver;
  }

  public String[][] getDriverStatus() {
    return driverStatus;
  }

  public void setDriverStatus(String[][] driverStatus) {
    this.driverStatus = driverStatus;
  }

  public String getExecutionDriver() {
    return executionDriver;
  }

  public void setExecutionDriver(String executionDriver) {
    this.executionDriver = executionDriver;
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

  public String getID() {
    return iD;
  }

  public void setID(String iD) {
    this.iD = iD;
  }

  public boolean setIPv4Forwarding() {
    return iPv4Forwarding;
  }

  public void setiPv4Forwarding(boolean iPv4Forwarding) {
    this.iPv4Forwarding = iPv4Forwarding;
  }

  public int getImages() {
    return images;
  }

  public void setImages(int images) {
    this.images = images;
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

  public String[] getLabels() {
    return labels;
  }

  public void setLabels(String[] labels) {
    this.labels = labels;
  }

  public long getMemTotal() {
    return memTotal;
  }

  public void setMemTotal(long memTotal) {
    this.memTotal = memTotal;
  }

  public boolean isMemoryLimit() {
    return memoryLimit;
  }

  public void setMemoryLimit(boolean memoryLimit) {
    this.memoryLimit = memoryLimit;
  }

  public int getNCPU() {
    return nCPU;
  }

  public void setNCPU(int nCPU) {
    this.nCPU = nCPU;
  }

  public int getnEventsListener() {
    return nEventsListener;
  }

  public void setnEventsListener(int nEventsListener) {
    this.nEventsListener = nEventsListener;
  }

  public int getNFd() {
    return nFd;
  }

  public void setNFd(int nFd) {
    this.nFd = nFd;
  }

  public int getNGoroutines() {
    return nGoroutines;
  }

  public void setNGoroutines(int nGoroutines) {
    this.nGoroutines = nGoroutines;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getNoProxy() {
    return noProxy;
  }

  public void setNoProxy(String noProxy) {
    this.noProxy = noProxy;
  }

  public String getOperatingSystem() {
    return operatingSystem;
  }

  public void setOperatingSystem(String operatingSystem) {
    this.operatingSystem = operatingSystem;
  }

  public boolean isSwapLimit() {
    return swapLimit;
  }

  public void setSwapLimit(boolean swapLimit) {
    this.swapLimit = swapLimit;
  }

  public String getSystemTime() {
    return systemTime;
  }

  public void setSystemTime(String systemTime) {
    this.systemTime = systemTime;
  }

  public String[][] getSystemStatus() {
    return systemStatus;
  }

  public void setSystemStatus(String[][] systemStatus) {
    this.systemStatus = systemStatus;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof SystemInfo)) return false;
    SystemInfo that = (SystemInfo) o;
    return containers == that.containers
        && debug == that.debug
        && iPv4Forwarding == that.iPv4Forwarding
        && images == that.images
        && memTotal == that.memTotal
        && memoryLimit == that.memoryLimit
        && nCPU == that.nCPU
        && nEventsListener == that.nEventsListener
        && nFd == that.nFd
        && nGoroutines == that.nGoroutines
        && swapLimit == that.swapLimit
        && Objects.equals(dockerRootDir, that.dockerRootDir)
        && Objects.equals(driver, that.driver)
        && Arrays.equals(driverStatus, that.driverStatus)
        && Objects.equals(executionDriver, that.executionDriver)
        && Objects.equals(httpProxy, that.httpProxy)
        && Objects.equals(httpsProxy, that.httpsProxy)
        && Objects.equals(iD, that.iD)
        && Objects.equals(indexServerAddress, that.indexServerAddress)
        && Objects.equals(initPath, that.initPath)
        && Objects.equals(initSha1, that.initSha1)
        && Objects.equals(kernelVersion, that.kernelVersion)
        && Arrays.equals(labels, that.labels)
        && Objects.equals(name, that.name)
        && Objects.equals(noProxy, that.noProxy)
        && Objects.equals(operatingSystem, that.operatingSystem)
        && Objects.equals(systemTime, that.systemTime)
        && Arrays.equals(systemStatus, that.systemStatus);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        containers,
        debug,
        dockerRootDir,
        driver,
        Arrays.deepHashCode(driverStatus),
        executionDriver,
        httpProxy,
        httpsProxy,
        iD,
        iPv4Forwarding,
        images,
        indexServerAddress,
        initPath,
        initSha1,
        kernelVersion,
        Arrays.hashCode(labels),
        memTotal,
        memoryLimit,
        nCPU,
        nEventsListener,
        nFd,
        nGoroutines,
        name,
        noProxy,
        operatingSystem,
        swapLimit,
        systemTime,
        Arrays.deepHashCode(systemStatus));
  }

  @Override
  public String toString() {
    return "SystemInfo{"
        + "containers="
        + containers
        + ", debug="
        + debug
        + ", dockerRootDir='"
        + dockerRootDir
        + '\''
        + ", driver='"
        + driver
        + '\''
        + ", driverStatus="
        + Arrays.toString(driverStatus)
        + ", executionDriver='"
        + executionDriver
        + '\''
        + ", httpProxy='"
        + httpProxy
        + '\''
        + ", httpsProxy='"
        + httpsProxy
        + '\''
        + ", iD='"
        + iD
        + '\''
        + ", iPv4Forwarding="
        + iPv4Forwarding
        + ", images="
        + images
        + ", indexServerAddress='"
        + indexServerAddress
        + '\''
        + ", initPath='"
        + initPath
        + '\''
        + ", initSha1='"
        + initSha1
        + '\''
        + ", kernelVersion='"
        + kernelVersion
        + '\''
        + ", labels="
        + Arrays.toString(labels)
        + ", memTotal="
        + memTotal
        + ", memoryLimit="
        + memoryLimit
        + ", nCPU="
        + nCPU
        + ", nEventsListener="
        + nEventsListener
        + ", nFd="
        + nFd
        + ", nGoroutines="
        + nGoroutines
        + ", name='"
        + name
        + '\''
        + ", noProxy='"
        + noProxy
        + '\''
        + ", operatingSystem='"
        + operatingSystem
        + '\''
        + ", swapLimit="
        + swapLimit
        + ", systemTime='"
        + systemTime
        + '\''
        + ", systemStatus="
        + Arrays.toString(systemStatus)
        + '}';
  }
}
