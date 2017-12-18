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
package org.eclipse.che.ide.api.workspace.model;

import java.util.Objects;
import org.eclipse.che.api.core.model.workspace.config.Volume;

/** @author Alexander Garagatyi */
public class VolumeImpl implements Volume {
  private final String path;

  public VolumeImpl(String path) {
    this.path = path;
  }

  public VolumeImpl(Volume value) {
    path = value.getPath();
  }

  @Override
  public String getPath() {
    return path;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof VolumeImpl)) {
      return false;
    }
    final VolumeImpl that = (VolumeImpl) obj;
    return Objects.equals(path, that.path);
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 31 * hash + Objects.hashCode(path);
    return hash;
  }

  @Override
  public String toString() {
    return "VolumeImpl{" + "path='" + path + '\'' + '}';
  }
}
