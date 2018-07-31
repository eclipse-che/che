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
 * Arguments holder for {@link DockerConnector#inspectImage(InspectImageParams)}.
 *
 * @author Mykola Morhun
 */
public class InspectImageParams {

  private String image;

  /**
   * Creates arguments holder with required parameters.
   *
   * @param image id or full repository name of docker image
   * @return arguments holder with required parameters
   * @throws NullPointerException if {@code image} is null
   */
  public static InspectImageParams create(@NotNull String image) {
    return new InspectImageParams().withImage(image);
  }

  private InspectImageParams() {}

  /**
   * Adds image to this parameters.
   *
   * @param image id or full repository name of docker image
   * @return this params instance
   * @throws NullPointerException if {@code image} is null
   */
  public InspectImageParams withImage(@NotNull String image) {
    requireNonNull(image);
    this.image = image;
    return this;
  }

  public String getImage() {
    return image;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    InspectImageParams that = (InspectImageParams) o;
    return Objects.equals(image, that.image);
  }

  @Override
  public int hashCode() {
    return Objects.hash(image);
  }
}
