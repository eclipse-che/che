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
 * Arguments holder for {@link DockerConnector#getResource(GetResourceParams)}.
 *
 * @author Mykola Morhun
 */
public class GetResourceParams {

  private String container;
  private String sourcePath;

  /**
   * Creates arguments holder with required parameters.
   *
   * @param container container id or name
   * @param sourcePath info about this parameter see {@link #withSourcePath(String)}
   * @return arguments holder with required parameters
   * @throws NullPointerException if {@code container} or {@code sourcePath} is null
   */
  public static GetResourceParams create(@NotNull String container, @NotNull String sourcePath) {
    return new GetResourceParams().withContainer(container).withSourcePath(sourcePath);
  }

  private GetResourceParams() {}

  /**
   * Adds container to this parameters.
   *
   * @param container container id or name
   * @return this params instance
   * @throws NullPointerException if {@code container} is null
   */
  public GetResourceParams withContainer(@NotNull String container) {
    requireNonNull(container);
    this.container = container;
    return this;
  }

  /**
   * Adds path to source archive to this parameters.
   *
   * @param sourcePath resource in the container’s filesystem to archive. Required. The resource
   *     specified by path must exist. It should end in '/' or '/.'.<br>
   *     A symlink is always resolved to its target.
   * @return this params instance
   * @throws NullPointerException if {@code sourcePath} is null
   */
  public GetResourceParams withSourcePath(@NotNull String sourcePath) {
    requireNonNull(sourcePath);
    this.sourcePath = sourcePath;
    return this;
  }

  public String getContainer() {
    return container;
  }

  public String getSourcePath() {
    return sourcePath;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    GetResourceParams that = (GetResourceParams) o;
    return Objects.equals(container, that.container) && Objects.equals(sourcePath, that.sourcePath);
  }

  @Override
  public int hashCode() {
    return Objects.hash(container, sourcePath);
  }
}
