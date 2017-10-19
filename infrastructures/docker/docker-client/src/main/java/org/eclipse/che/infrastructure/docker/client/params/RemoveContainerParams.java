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
package org.eclipse.che.infrastructure.docker.client.params;

import static java.util.Objects.requireNonNull;

import java.util.Objects;
import javax.validation.constraints.NotNull;
import org.eclipse.che.infrastructure.docker.client.DockerConnector;

/**
 * Arguments holder for {@link DockerConnector#removeContainer(RemoveContainerParams)}.
 *
 * @author Mykola Morhun
 */
public class RemoveContainerParams {

  private String container;
  private Boolean force;
  private Boolean removeVolumes;

  /**
   * Creates arguments holder with required parameters.
   *
   * @param container container identifier, either id or name
   * @return arguments holder with required parameters
   * @throws NullPointerException if {@code container} is null
   */
  public static RemoveContainerParams create(@NotNull String container) {
    return new RemoveContainerParams().withContainer(container);
  }

  private RemoveContainerParams() {}

  /**
   * Adds container to this parameters.
   *
   * @param container container identifier, either id or name
   * @return this params instance
   * @throws NullPointerException if {@code container} is null
   */
  public RemoveContainerParams withContainer(@NotNull String container) {
    requireNonNull(container);
    this.container = container;
    return this;
  }

  /**
   * Adds force flag to this parameters.
   *
   * @param force if {@code true} kills the running container then remove it
   * @return this params instance
   */
  public RemoveContainerParams withForce(boolean force) {
    this.force = force;
    return this;
  }

  /**
   * Adds remove volumes flag to this parameters.
   *
   * @param removeVolumes if {@code true} removes volumes associated to the container
   * @return this params instance
   */
  public RemoveContainerParams withRemoveVolumes(boolean removeVolumes) {
    this.removeVolumes = removeVolumes;
    return this;
  }

  public String getContainer() {
    return container;
  }

  public Boolean isForce() {
    return force;
  }

  public Boolean isRemoveVolumes() {
    return removeVolumes;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    RemoveContainerParams that = (RemoveContainerParams) o;
    return Objects.equals(container, that.container)
        && Objects.equals(force, that.force)
        && Objects.equals(removeVolumes, that.removeVolumes);
  }

  @Override
  public int hashCode() {
    return Objects.hash(container, force, removeVolumes);
  }
}
