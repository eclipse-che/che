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

/** @author andrew00x */
public class PortBinding {
  private String hostIp;
  private String hostPort;

  public PortBinding() {}

  public PortBinding(String hostIp, String hostPort) {
    this.hostIp = hostIp;
    this.hostPort = hostPort;
  }

  public String getHostIp() {
    return hostIp;
  }

  public void setHostIp(String hostIp) {
    this.hostIp = hostIp;
  }

  public String getHostPort() {
    return hostPort;
  }

  public void setHostPort(String hostPort) {
    this.hostPort = hostPort;
  }

  public PortBinding withHostIp(String hostIp) {
    this.hostIp = hostIp;
    return this;
  }

  public PortBinding withHostPort(String hostPort) {
    this.hostPort = hostPort;
    return this;
  }

  @Override
  public String toString() {
    return "PortBinding{" + "hostIp='" + hostIp + '\'' + ", hostPort='" + hostPort + '\'' + '}';
  }
}
