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
package org.eclipse.che.infrastructure.docker.client.json.volume;

import java.util.Objects;

/** @author Alexander Garagatyi */
public class Volume {
  private String name;
  private String driver;
  private String mountpoint;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Volume withName(String name) {
    this.name = name;
    return this;
  }

  public String getDriver() {
    return driver;
  }

  public void setDriver(String driver) {
    this.driver = driver;
  }

  public Volume withDriver(String driver) {
    this.driver = driver;
    return this;
  }

  public String getMountpoint() {
    return mountpoint;
  }

  public void setMountpoint(String mountpoint) {
    this.mountpoint = mountpoint;
  }

  public Volume withMountpoint(String mountpoint) {
    this.mountpoint = mountpoint;
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Volume)) {
      return false;
    }
    Volume volume = (Volume) o;
    return Objects.equals(getName(), volume.getName())
        && Objects.equals(getDriver(), volume.getDriver())
        && Objects.equals(getMountpoint(), volume.getMountpoint());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getName(), getDriver(), getMountpoint());
  }

  @Override
  public String toString() {
    return "Volume{"
        + "name='"
        + name
        + '\''
        + ", driver='"
        + driver
        + '\''
        + ", mountpoint='"
        + mountpoint
        + '\''
        + '}';
  }
}
