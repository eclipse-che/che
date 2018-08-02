/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.infrastructure.docker.client.json.network;

import java.util.Objects;

/** @author Alexander Garagatyi */
public class IpamConfig {
  private String subnet;
  private String gateway;
  private String iPRange;

  public String getSubnet() {
    return subnet;
  }

  public void setSubnet(String subnet) {
    this.subnet = subnet;
  }

  public IpamConfig withSubnet(String subnet) {
    this.subnet = subnet;
    return this;
  }

  public String getGateway() {
    return gateway;
  }

  public void setGateway(String gateway) {
    this.gateway = gateway;
  }

  public IpamConfig withGateway(String gateway) {
    this.gateway = gateway;
    return this;
  }

  public String getIPRange() {
    return iPRange;
  }

  public void setIPRange(String iPRange) {
    this.iPRange = iPRange;
  }

  public IpamConfig withIPRange(String iPRange) {
    this.iPRange = iPRange;
    return this;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof IpamConfig)) {
      return false;
    }
    final IpamConfig that = (IpamConfig) obj;
    return Objects.equals(subnet, that.subnet)
        && Objects.equals(gateway, that.gateway)
        && Objects.equals(iPRange, that.iPRange);
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 31 * hash + Objects.hashCode(subnet);
    hash = 31 * hash + Objects.hashCode(gateway);
    hash = 31 * hash + Objects.hashCode(iPRange);
    return hash;
  }

  @Override
  public String toString() {
    return "IpamConfig{"
        + "subnet='"
        + subnet
        + '\''
        + ", gateway='"
        + gateway
        + '\''
        + ", iPRange='"
        + iPRange
        + '\''
        + '}';
  }
}
