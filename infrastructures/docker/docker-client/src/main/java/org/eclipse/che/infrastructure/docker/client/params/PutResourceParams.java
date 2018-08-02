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

import java.io.InputStream;
import java.util.Objects;
import javax.validation.constraints.NotNull;
import org.eclipse.che.infrastructure.docker.client.DockerConnector;

/**
 * Arguments holder for {@link DockerConnector#putResource(PutResourceParams)}.
 *
 * @author Mykola Morhun
 */
public class PutResourceParams {

  private String container;
  private String targetPath;
  private InputStream sourceStream;
  private Boolean noOverwriteDirNonDir;

  /**
   * Creates arguments holder with required parameters.
   *
   * @param container container id or name
   * @param targetPath info about this parameter see {@link #withTargetPath(String)}
   * @return arguments holder with required parameters
   * @throws NullPointerException if {@code container} or {@code targetPath} is null
   */
  @Deprecated
  public static PutResourceParams create(@NotNull String container, @NotNull String targetPath) {
    return new PutResourceParams().withContainer(container).withTargetPath(targetPath);
  }

  public static PutResourceParams create(
      @NotNull String container, @NotNull String targetPath, @NotNull InputStream sourceStream) {
    return new PutResourceParams()
        .withContainer(container)
        .withTargetPath(targetPath)
        .withSourceStream(sourceStream);
  }

  private PutResourceParams() {}

  /**
   * Adds container to this parameters.
   *
   * @param container container id or name
   * @return this params instance
   * @throws NullPointerException if {@code container} is null
   */
  public PutResourceParams withContainer(@NotNull String container) {
    requireNonNull(container);
    this.container = container;
    return this;
  }

  /**
   * Adds path to a directory to extract archive into to this parameters.
   *
   * @param targetPath path to a directory in the container to extract the archive’s contents into.
   *     Required. If not an absolute path, it is relative to the container’s root directory. The
   *     path resource must exist.
   * @return this params instance
   * @throws NullPointerException if {@code targetPath} is null
   */
  public PutResourceParams withTargetPath(@NotNull String targetPath) {
    requireNonNull(targetPath);
    this.targetPath = targetPath;
    return this;
  }

  /**
   * Adds stream of files to this parameters.
   *
   * @param sourceStream stream of files from source container, must be obtained from another
   *     container using {@link DockerConnector#getResource(GetResourceParams)}
   * @return this params instance
   * @throws NullPointerException if {@code sourceStream} is null
   */
  public PutResourceParams withSourceStream(@NotNull InputStream sourceStream) {
    requireNonNull(sourceStream);
    this.sourceStream = sourceStream;
    return this;
  }

  /**
   * Adds allowing replace flag to this parameters.
   *
   * @param noOverwriteDirNonDir if {@code true} then it will be an error if unpacking the given
   *     content would cause an existing directory to be replaced with a non-directory and vice
   *     versa.
   * @return this params instance
   */
  public PutResourceParams withNoOverwriteDirNonDir(Boolean noOverwriteDirNonDir) {
    this.noOverwriteDirNonDir = noOverwriteDirNonDir;
    return this;
  }

  public String getContainer() {
    return container;
  }

  public String getTargetPath() {
    return targetPath;
  }

  public InputStream getSourceStream() {
    return sourceStream;
  }

  public Boolean isNoOverwriteDirNonDir() {
    return noOverwriteDirNonDir;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof PutResourceParams)) {
      return false;
    }
    final PutResourceParams that = (PutResourceParams) obj;
    return Objects.equals(container, that.container)
        && Objects.equals(targetPath, that.targetPath)
        && Objects.equals(sourceStream, that.sourceStream)
        && Objects.equals(noOverwriteDirNonDir, that.noOverwriteDirNonDir);
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 31 * hash + Objects.hashCode(container);
    hash = 31 * hash + Objects.hashCode(targetPath);
    hash = 31 * hash + Objects.hashCode(sourceStream);
    hash = 31 * hash + Objects.hashCode(noOverwriteDirNonDir);
    return hash;
  }

  @Override
  public String toString() {
    return "PutResourceParams{"
        + "container='"
        + container
        + '\''
        + ", targetPath='"
        + targetPath
        + '\''
        + ", sourceStream="
        + sourceStream
        + ", noOverwriteDirNonDir="
        + noOverwriteDirNonDir
        + '}';
  }
}
