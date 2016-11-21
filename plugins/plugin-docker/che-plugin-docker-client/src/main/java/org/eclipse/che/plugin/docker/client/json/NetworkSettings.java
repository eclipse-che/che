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
import java.util.List;
import java.util.Map;

/** @author andrew00x */
public class NetworkSettings {
    private String   ipAddress;
    private int      iPPrefixLen;
    private String   gateway;
    private String   bridge;
    private String[] portMapping;
    private String   macAddress;
    private int      linkLocalIPv6PrefixLen;
    private String   globalIPv6Address;
    private int      globalIPv6PrefixLen;
    private String   iPv6Gateway;
    private String   linkLocalIPv6Address;

    private Map<String, List<PortBinding>> ports = new HashMap<>();

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public int getIpPrefixLen() {
        return iPPrefixLen;
    }

    public void setIpPrefixLen(int iPPrefixLen) {
        this.iPPrefixLen = iPPrefixLen;
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

    public String getiPv6Gateway() {
        return iPv6Gateway;
    }

    public void setiPv6Gateway(String iPv6Gateway) {
        this.iPv6Gateway = iPv6Gateway;
    }

    public String getLinkLocalIPv6Address() {
        return linkLocalIPv6Address;
    }

    public void setLinkLocalIPv6Address(String linkLocalIPv6Address) {
        this.linkLocalIPv6Address = linkLocalIPv6Address;
    }

    @Override
    public String toString() {
        return "NetworkSettings{" +
               "ipAddress='" + ipAddress + '\'' +
               ", iPPrefixLen=" + iPPrefixLen +
               ", gateway='" + gateway + '\'' +
               ", bridge='" + bridge + '\'' +
               ", portMapping=" + Arrays.toString(portMapping) +
               ", macAddress='" + macAddress + '\'' +
               ", linkLocalIPv6PrefixLen=" + linkLocalIPv6PrefixLen +
               ", globalIPv6Address='" + globalIPv6Address + '\'' +
               ", globalIPv6PrefixLen=" + globalIPv6PrefixLen +
               ", iPv6Gateway='" + iPv6Gateway + '\'' +
               ", linkLocalIPv6Address='" + linkLocalIPv6Address + '\'' +
               ", ports=" + ports +
               '}';
    }
}
