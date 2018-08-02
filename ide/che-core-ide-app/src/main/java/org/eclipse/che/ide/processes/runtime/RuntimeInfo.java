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
package org.eclipse.che.ide.processes.runtime;

import java.util.Objects;

/**
 * POJO of server runtime. Used only for the internal staff of the server list.
 *
 * @author Vlad Zhukovskyi
 * @since 5.18.0
 */
public class RuntimeInfo {

  private String reference;
  private String port;
  private String protocol;
  private String url;

  public RuntimeInfo(String reference, String port, String protocol, String url) {
    this.reference = reference;
    this.port = port;
    this.protocol = protocol;
    this.url = url;
  }

  public String getReference() {
    return reference;
  }

  public void setReference(String reference) {
    this.reference = reference;
  }

  public String getPort() {
    return port;
  }

  public void setPort(String port) {
    this.port = port;
  }

  public String getProtocol() {
    return protocol;
  }

  public void setProtocol(String protocol) {
    this.protocol = protocol;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  @Override
  public String toString() {
    return "ServerRecord{"
        + "reference='"
        + reference
        + '\''
        + ", port="
        + port
        + ", protocol='"
        + protocol
        + '\''
        + ", url='"
        + url
        + '\''
        + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RuntimeInfo that = (RuntimeInfo) o;
    return Objects.equals(reference, that.reference)
        && Objects.equals(port, that.port)
        && Objects.equals(protocol, that.protocol)
        && Objects.equals(url, that.url);
  }

  @Override
  public int hashCode() {
    return Objects.hash(reference, port, protocol, url);
  }
}
