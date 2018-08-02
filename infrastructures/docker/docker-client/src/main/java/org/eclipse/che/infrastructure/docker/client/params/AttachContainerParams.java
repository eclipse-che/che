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
import org.eclipse.che.infrastructure.docker.client.MessageProcessor;

/**
 * Arguments holder for {@link DockerConnector#attachContainer(AttachContainerParams,
 * MessageProcessor)} .
 *
 * @author Mykola Morhun
 */
public class AttachContainerParams {

  private String container;
  private Boolean stream;

  /**
   * Creates arguments holder with required parameters.
   *
   * @param container id or name of container
   * @return arguments holder with required parameters
   */
  public static AttachContainerParams create(@NotNull String container) {
    return new AttachContainerParams().withContainer(container);
  }

  private AttachContainerParams() {}

  /**
   * Adds container to this parameters.
   *
   * @param container id or name of container
   * @return this params instance
   * @throws NullPointerException if {@code container} is null
   */
  public AttachContainerParams withContainer(@NotNull String container) {
    requireNonNull(container);
    this.container = container;
    return this;
  }

  /**
   * Flag for getting output stream from a container.
   *
   * @param stream if {@code true} gets output stream from container.<br>
   *     Note, that live stream blocks until container is running.<br>
   *     When using the TTY setting is enabled when from container, the stream is the raw data from
   *     the process PTY and client’s stdin. When the TTY is disabled, then the stream is
   *     multiplexed to separate stdout and stderr.
   * @return this params instance
   */
  public AttachContainerParams withStream(boolean stream) {
    this.stream = stream;
    return this;
  }

  public String getContainer() {
    return container;
  }

  public Boolean isStream() {
    return stream;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    AttachContainerParams that = (AttachContainerParams) o;
    return Objects.equals(container, that.container) && Objects.equals(stream, that.stream);
  }

  @Override
  public int hashCode() {
    return Objects.hash(container, stream);
  }
}
