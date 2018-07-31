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
package org.eclipse.che.infrastructure.docker.client.params;

import static java.util.Objects.requireNonNull;

import java.util.Objects;
import javax.validation.constraints.NotNull;
import org.eclipse.che.infrastructure.docker.client.DockerConnector;

/**
 * Arguments holder for {@link DockerConnector#removeImage(RemoveImageParams)}.
 *
 * @author Mykola Morhun
 */
public class RemoveImageParams {

  private String image;
  private Boolean force;

  /**
   * Creates arguments holder with required parameters.
   *
   * @param image image identifier, either id or name
   * @return arguments holder with required parameters
   * @throws NullPointerException if {@code image} is null
   */
  public static RemoveImageParams create(@NotNull String image) {
    return new RemoveImageParams().withImage(image);
  }

  private RemoveImageParams() {}

  /**
   * Adds image to this parameters.
   *
   * @param image image identifier, either id or name
   * @return this params instance
   * @throws NullPointerException if {@code image} is null
   */
  public RemoveImageParams withImage(@NotNull String image) {
    requireNonNull(image);
    this.image = image;
    return this;
  }

  /**
   * Adds force flag to this parameters.
   *
   * @param force {@code true} means remove an image anyway, despite using of this image
   * @return this params instance
   */
  public RemoveImageParams withForce(boolean force) {
    this.force = force;
    return this;
  }

  public String getImage() {
    return image;
  }

  public Boolean isForce() {
    return force;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof RemoveImageParams)) {
      return false;
    }
    final RemoveImageParams that = (RemoveImageParams) obj;
    return Objects.equals(image, that.image) && Objects.equals(force, that.force);
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 31 * hash + Objects.hashCode(image);
    hash = 31 * hash + Objects.hashCode(force);
    return hash;
  }

  @Override
  public String toString() {
    return "RemoveImageParams{" + "image='" + image + '\'' + ", force=" + force + '}';
  }
}
