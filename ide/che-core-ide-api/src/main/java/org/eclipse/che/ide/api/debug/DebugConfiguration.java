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
package org.eclipse.che.ide.api.debug;

import java.util.Map;
import java.util.Objects;

/**
 * Represents debug configuration.
 *
 * @author Artem Zatsarynnyi
 */
public class DebugConfiguration {

  private final DebugConfigurationType type;
  private String name;
  private String host;
  private int port;
  private Map<String, String> connectionProperties;

  /**
   * Creates new debug configuration.
   *
   * @param type type of the configuration
   * @param name configuration's name
   * @param host debugger host
   * @param port debugger port
   * @param connectionProperties additional properties for connecting to the debugger
   */
  public DebugConfiguration(
      DebugConfigurationType type,
      String name,
      String host,
      int port,
      Map<String, String> connectionProperties) {
    this.type = type;
    this.name = name;
    this.host = host;
    this.port = port;
    this.connectionProperties = connectionProperties;
  }

  /** Returns configuration's type. */
  public DebugConfigurationType getType() {
    return type;
  }

  /** Returns configuration's name. */
  public String getName() {
    return name;
  }

  /** Sets new name for this configuration. */
  public void setName(String name) {
    this.name = name;
  }

  /** Returns debugger host. */
  public String getHost() {
    return host;
  }

  /** Sets host for this configuration. */
  public void setHost(String host) {
    this.host = host;
  }

  /** Returns debugger port. */
  public int getPort() {
    return port;
  }

  /** Sets port for this configuration. */
  public void setPort(int port) {
    this.port = port;
  }

  /** Returns additional connection properties. */
  public Map<String, String> getConnectionProperties() {
    return connectionProperties;
  }

  /** Sets additional connection properties. */
  public void setConnectionProperties(Map<String, String> connectionProperties) {
    this.connectionProperties = connectionProperties;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (!(o instanceof DebugConfiguration)) {
      return false;
    }

    DebugConfiguration other = (DebugConfiguration) o;

    return Objects.equals(getType().getId(), other.getType().getId())
        && Objects.equals(getName(), other.getName())
        && Objects.equals(getHost(), other.getHost())
        && Objects.equals(getPort(), other.getPort())
        && Objects.equals(getConnectionProperties(), other.getConnectionProperties());
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        getName(), getType().getId(), getHost(), getPort(), getConnectionProperties());
  }
}
