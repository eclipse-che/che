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
package org.eclipse.che.ide.debug;

/**
 * Represents general information about debugger.
 *
 * @author Anatoliy Bazko
 */
public class DebuggerDescriptor {

  /** Info about debugger. Generally contains of name and version. */
  private String info;

  /** The address of the application is being debugged: host, port, pid etc. */
  private String address;

  public DebuggerDescriptor(String info, String address) {
    this.info = info;
    this.address = address;
  }

  public String getInfo() {
    return info;
  }

  public String getAddress() {
    return address;
  }

  public void setInfo(String info) {
    this.info = info;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    DebuggerDescriptor that = (DebuggerDescriptor) o;

    if (info != null ? !info.equals(that.info) : that.info != null) return false;
    return address != null ? address.equals(that.address) : that.address == null;
  }

  @Override
  public int hashCode() {
    int result = info != null ? info.hashCode() : 0;
    result = 31 * result + (address != null ? address.hashCode() : 0);
    return result;
  }
}
