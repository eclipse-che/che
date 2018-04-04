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
package org.eclipse.che.infrastructure.docker.client.params.volume;

import static java.util.Objects.requireNonNull;

import java.util.Objects;
import javax.validation.constraints.NotNull;
import org.eclipse.che.infrastructure.docker.client.DockerConnector;

/**
 * Arguments holder for {@link DockerConnector#removeVolume(RemoveVolumeParams)}.
 *
 * @author Alexander Garagatyi
 */
public class RemoveVolumeParams {
  private String volumeName;

  private RemoveVolumeParams() {}

  /**
   * Creates arguments holder with required parameters.
   *
   * @param volumeName name of a volume to remove
   * @return arguments holder with required parameters
   * @throws NullPointerException if {@code volumeName} is null
   */
  public static RemoveVolumeParams create(@NotNull String volumeName) {
    return new RemoveVolumeParams().withVolumeName(volumeName);
  }

  /**
   * Adds the name of a volume to this parameters.
   *
   * @param volumeName name of a volume
   * @return this params instance
   * @throws NullPointerException if {@code volumeName} is null
   */
  public RemoveVolumeParams withVolumeName(@NotNull String volumeName) {
    requireNonNull(volumeName);
    this.volumeName = volumeName;
    return this;
  }

  public String getVolumeName() {
    return volumeName;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof RemoveVolumeParams)) {
      return false;
    }
    final RemoveVolumeParams that = (RemoveVolumeParams) obj;
    return Objects.equals(volumeName, that.volumeName);
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 31 * hash + Objects.hashCode(volumeName);
    return hash;
  }

  @Override
  public String toString() {
    return "RemoveVolumeParams{" + "volumeName='" + volumeName + '\'' + '}';
  }
}
