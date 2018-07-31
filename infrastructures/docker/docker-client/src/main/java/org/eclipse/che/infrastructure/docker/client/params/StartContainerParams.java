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
 * Arguments holder for {@link DockerConnector#startContainer(StartContainerParams)}.
 *
 * @author Mykola Morhun
 */
public class StartContainerParams {

  private String container;

  /**
   * Creates arguments holder with required parameters.
   *
   * @param container id or name of container to start
   * @return arguments holder with required parameters
   * @throws NullPointerException if {@code container} is null
   */
  public static StartContainerParams create(@NotNull String container) {
    return new StartContainerParams().withContainer(container);
  }

  private StartContainerParams() {}

  /**
   * Adds container to this parameters.
   *
   * @param container id or name of container to start
   * @return this params instance
   * @throws NullPointerException if {@code container} is null
   */
  public StartContainerParams withContainer(@NotNull String container) {
    requireNonNull(container);
    this.container = container;
    return this;
  }

  public String getContainer() {
    return container;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    StartContainerParams that = (StartContainerParams) o;
    return Objects.equals(container, that.container);
  }

  @Override
  public int hashCode() {
    return Objects.hash(container);
  }
}
