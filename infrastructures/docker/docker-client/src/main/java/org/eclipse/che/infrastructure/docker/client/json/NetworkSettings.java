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
package org.eclipse.che.infrastructure.docker.client.json;

import com.google.gson.annotations.SerializedName;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** @author andrew00x */
public class NetworkSettings {
  @SerializedName("IPAddress")
  private String ipAddress;

  @SerializedName("IPPrefixLen")
  private int ipPrefixLen;

  private String gateway;
  private String bridge;
  private String[] portMapping;
  private String macAddress;
  private int linkLocalIPv6PrefixLen;
  private String globalIPv6Address;
  private int globalIPv6PrefixLen;

  @SerializedName("IPv6Gateway")
  private String ipV6Gateway;

  private String linkLocalIPv6Address;

  private Map<String, List<PortBinding>> ports = new HashMap<>();

  public String getIpAddress() {
    return ipAddress;
  }

  public void setIpAddress(String ipAddress) {
    this.ipAddress = ipAddress;
  }

  public int getIpPrefixLen() {
    return ipPrefixLen;
  }

  public void setIpPrefixLen(int iPPrefixLen) {
    this.ipPrefixLen = iPPrefixLen;
  }

  public String getGateway() {
    return gateway;
  }

  public void setGateway(String gateway) {
    this.gateway = gateway;
  }

  public String getBridge() {
    return bridge;
  }

  public void setBridge(String bridge) {
    this.bridge = bridge;
  }

  public String[] getPortMapping() {
    return portMapping;
  }

  public void setPortMapping(String[] portMapping) {
    this.portMapping = portMapping;
  }

  public String getMacAddress() {
    return macAddress;
  }

  public void setMacAddress(String macAddress) {
    this.macAddress = macAddress;
  }

  public Map<String, List<PortBinding>> getPorts() {
    return ports;
  }

  public void setPorts(Map<String, List<PortBinding>> ports) {
    this.ports = ports;
  }

  public int getLinkLocalIPv6PrefixLen() {
    return linkLocalIPv6PrefixLen;
  }

  public void setLinkLocalIPv6PrefixLen(int linkLocalIPv6PrefixLen) {
    this.linkLocalIPv6PrefixLen = linkLocalIPv6PrefixLen;
  }

  public String getGlobalIPv6Address() {
    return globalIPv6Address;
  }

  public void setGlobalIPv6Address(String globalIPv6Address) {
    this.globalIPv6Address = globalIPv6Address;
  }

  public int getGlobalIPv6PrefixLen() {
    return globalIPv6PrefixLen;
  }

  public void setGlobalIPv6PrefixLen(int globalIPv6PrefixLen) {
    this.globalIPv6PrefixLen = globalIPv6PrefixLen;
  }

  public String getIpV6Gateway() {
    return ipV6Gateway;
  }

  public void setIpV6Gateway(String ipV6Gateway) {
    this.ipV6Gateway = ipV6Gateway;
  }

  public String getLinkLocalIPv6Address() {
    return linkLocalIPv6Address;
  }

  public void setLinkLocalIPv6Address(String linkLocalIPv6Address) {
    this.linkLocalIPv6Address = linkLocalIPv6Address;
  }

  @Override
  public String toString() {
    return "NetworkSettings{"
        + "ipAddress='"
        + ipAddress
        + '\''
        + ", ipPrefixLen="
        + ipPrefixLen
        + ", gateway='"
        + gateway
        + '\''
        + ", bridge='"
        + bridge
        + '\''
        + ", portMapping="
        + Arrays.toString(portMapping)
        + ", macAddress='"
        + macAddress
        + '\''
        + ", linkLocalIPv6PrefixLen="
        + linkLocalIPv6PrefixLen
        + ", globalIPv6Address='"
        + globalIPv6Address
        + '\''
        + ", globalIPv6PrefixLen="
        + globalIPv6PrefixLen
        + ", ipV6Gateway='"
        + ipV6Gateway
        + '\''
        + ", linkLocalIPv6Address='"
        + linkLocalIPv6Address
        + '\''
        + ", ports="
        + ports
        + '}';
  }
}
