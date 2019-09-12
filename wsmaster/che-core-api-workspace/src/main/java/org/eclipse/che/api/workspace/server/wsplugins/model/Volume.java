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
package org.eclipse.che.api.workspace.server.wsplugins.model;

import java.util.Objects;

public class Volume {

  private String name = null;
  private String mountPath = null;
  private boolean persistVolume;

  /** */
  public Volume name(String name) {
    this.name = name;
    return this;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  /** mountPath of the volume in running container */
  public Volume mountPath(String path) {
    this.mountPath = path;
    return this;
  }

  public String getMountPath() {
    return mountPath;
  }

  public void setMountPath(String mountPath) {
    this.mountPath = mountPath;
  }

  public Volume persistVolume(boolean persistVolume) {
    this.persistVolume = persistVolume;
    return this;
  }

  public boolean isPersistVolume() {
    return persistVolume;
  }

  public void setPersistVolume(boolean persistVolume) {
    this.persistVolume = persistVolume;
  }

  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Volume volume = (Volume) o;
    return Objects.equals(name, volume.name)
        && Objects.equals(mountPath, volume.mountPath)
        && Objects.equals(persistVolume, volume.persistVolume);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, mountPath, persistVolume);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Volume {\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    mountPath: ").append(toIndentedString(mountPath)).append("\n");
    sb.append("    persistVolume: ").append(toIndentedString(persistVolume)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}
