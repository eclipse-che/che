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
package org.eclipse.che.infrastructure.docker.client.params;

import static java.util.Objects.requireNonNull;

import java.util.Objects;
import javax.validation.constraints.NotNull;
import org.eclipse.che.infrastructure.docker.client.DockerConnector;

/**
 * Arguments holder for{@link DockerConnector#inspectContainer(InspectContainerParams)}.
 *
 * @author Mykola Morhun
 */
public class InspectContainerParams {

  private String container;
  private Boolean returnContainerSize;

  /**
   * Creates arguments holder with required parameters.
   *
   * @param container id or name of container
   * @return arguments holder with required parameters
   * @throws NullPointerException if {@code container} is null
   */
  public static InspectContainerParams create(@NotNull String container) {
    return new InspectContainerParams().withContainer(container);
  }

  private InspectContainerParams() {}

  /**
   * Adds container to this parameters.
   *
   * @param container id or name of container
   * @return this params instance
   * @throws NullPointerException if {@code container} is null
   */
  public InspectContainerParams withContainer(@NotNull String container) {
    requireNonNull(container);
    this.container = container;
    return this;
  }

  /**
   * Adds return container size flag to this parameters.
   *
   * @param returnContainerSize if {@code true} it will return container size information
   * @return this params instance
   */
  public InspectContainerParams withReturnContainerSize(boolean returnContainerSize) {
    this.returnContainerSize = returnContainerSize;
    return this;
  }

  public String getContainer() {
    return container;
  }

  public Boolean isReturnContainerSize() {
    return returnContainerSize;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    InspectContainerParams that = (InspectContainerParams) o;
    return Objects.equals(container, that.container)
        && Objects.equals(returnContainerSize, that.returnContainerSize);
  }

  @Override
  public int hashCode() {
    return Objects.hash(container, returnContainerSize);
  }
}
