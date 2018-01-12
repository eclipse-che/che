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
import java.util.concurrent.TimeUnit;
import javax.validation.constraints.NotNull;
import org.eclipse.che.infrastructure.docker.client.DockerConnector;

/**
 * Arguments holder for {@link DockerConnector#stopContainer(StopContainerParams)}.
 *
 * @author Mykola Morhun
 */
public class StopContainerParams {

  private String container;
  private Long timeout;
  private TimeUnit timeunit;

  /**
   * Creates arguments holder with required parameters.
   *
   * @param container container identifier, either id or name
   * @return arguments holder with required parameters
   * @throws NullPointerException if {@code container} is null
   */
  public static StopContainerParams create(@NotNull String container) {
    return new StopContainerParams().withContainer(container);
  }

  private StopContainerParams() {}

  /**
   * Adds container to this parameters.
   *
   * @param container container identifier, either id or name
   * @return this params instance
   * @throws NullPointerException if {@code container} is null
   */
  public StopContainerParams withContainer(@NotNull String container) {
    requireNonNull(container);
    this.container = container;
    return this;
  }

  /**
   * Adds timeout to this parameters.
   *
   * @param timeout time in seconds to wait for the container to stop before killing it
   * @return this params instance
   */
  public StopContainerParams withTimeout(long timeout) {
    withTimeout(timeout, TimeUnit.SECONDS);
    return this;
  }

  /**
   * Adds timeout in specified time unit to this parameters.
   *
   * @param timeout time to wait for the container to stop before killing it
   * @param timeunit time unit of the timeout parameter
   * @return this params instance
   * @throws NullPointerException if {@code timeunit} is null
   */
  public StopContainerParams withTimeout(long timeout, TimeUnit timeunit) {
    requireNonNull(timeunit);
    this.timeout = timeout;
    this.timeunit = timeunit;
    return this;
  }

  public String getContainer() {
    return container;
  }

  public Long getTimeout() {
    return timeout;
  }

  public TimeUnit getTimeunit() {
    return timeunit;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    StopContainerParams that = (StopContainerParams) o;
    return Objects.equals(container, that.container)
        && Objects.equals(timeout, that.timeout)
        && timeunit == that.timeunit;
  }

  @Override
  public int hashCode() {
    return Objects.hash(container, timeout, timeunit);
  }
}
