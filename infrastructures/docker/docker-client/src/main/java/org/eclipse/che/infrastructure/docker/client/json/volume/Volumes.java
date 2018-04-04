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

import java.util.List;
import java.util.Objects;

/** @author Alexander Garagatyi */
public class Volumes {
  private List<Volume> volumes;

  public List<Volume> getVolumes() {
    return volumes;
  }

  public void setVolumes(List<Volume> volumes) {
    this.volumes = volumes;
  }

  public Volumes withVolumes(List<Volume> volumes) {
    this.volumes = volumes;
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Volumes)) {
      return false;
    }
    Volumes volumes1 = (Volumes) o;
    return Objects.equals(getVolumes(), volumes1.getVolumes());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getVolumes());
  }

  @Override
  public String toString() {
    return "Volumes{" + "volumes=" + volumes + '}';
  }
}
