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
package org.eclipse.che.infrastructure.docker.client.params;

import static java.util.Objects.requireNonNull;

import java.util.Objects;
import javax.validation.constraints.NotNull;
import org.eclipse.che.infrastructure.docker.client.DockerConnector;
import org.eclipse.che.infrastructure.docker.client.json.ContainerConfig;

/**
 * Arguments holder for {@link DockerConnector#createContainer(CreateContainerParams)}.
 *
 * @author Mykola Morhun
 */
public class CreateContainerParams {

  private ContainerConfig containerConfig;
  private String containerName;

  /**
   * Creates arguments holder with required parameters.
   *
   * @param containerConfig configuration of future container
   * @return arguments holder with required parameters
   * @throws NullPointerException if {@code containerConfig} is null
   */
  public static CreateContainerParams create(@NotNull ContainerConfig containerConfig) {
    return new CreateContainerParams().withContainerConfig(containerConfig);
  }

  private CreateContainerParams() {}

  /**
   * Adds future container configuration to this parameters.
   *
   * @param containerConfig configuration of future container
   * @return this params instance
   * @throws NullPointerException if {@code containerConfig} is null
   */
  public CreateContainerParams withContainerConfig(@NotNull ContainerConfig containerConfig) {
    requireNonNull(containerConfig);
    this.containerConfig = containerConfig;
    return this;
  }

  /**
   * Adds container name to this parameters.
   *
   * @param containerName assign the specified name to the container. Must match /?[a-zA-Z0-9_-]+
   * @return this params instance
   */
  public CreateContainerParams withContainerName(String containerName) {
    this.containerName = containerName;
    return this;
  }

  public ContainerConfig getContainerConfig() {
    return containerConfig;
  }

  public String getContainerName() {
    return containerName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    CreateContainerParams that = (CreateContainerParams) o;
    return Objects.equals(containerConfig, that.containerConfig)
        && Objects.equals(containerName, that.containerName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(containerConfig, containerName);
  }
}
