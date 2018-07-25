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
package org.eclipse.che.api.workspace.server.model.impl;

import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import org.eclipse.che.api.core.model.workspace.config.Volume;

/** @author Alexander Garagatyi */
@Entity(name = "MachineVolume")
@Table(name = "machine_volume")
public class VolumeImpl implements Volume {

  @Id
  @GeneratedValue
  @Column(name = "id")
  private Long id;

  @Column(name = "path")
  private String path;

  public VolumeImpl() {}

  public VolumeImpl(Volume value) {
    path = value.getPath();
  }

  @Override
  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public VolumeImpl withPath(String path) {
    this.path = path;
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof VolumeImpl)) {
      return false;
    }
    VolumeImpl volume = (VolumeImpl) o;
    return Objects.equals(id, volume.id) && Objects.equals(getPath(), volume.getPath());
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, getPath());
  }

  @Override
  public String toString() {
    return "VolumeImpl{" + "id=" + id + ", path='" + path + '\'' + '}';
  }
}
