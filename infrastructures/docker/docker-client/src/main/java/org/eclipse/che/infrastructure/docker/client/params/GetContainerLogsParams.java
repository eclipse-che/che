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
import org.eclipse.che.infrastructure.docker.client.MessageProcessor;

/**
 * Arguments holder for {@link DockerConnector#getContainerLogs(GetContainerLogsParams,
 * MessageProcessor)}.
 *
 * @author Mykola Morhun
 */
public class GetContainerLogsParams {

  private String container;
  private Boolean details;
  private Boolean follow;
  private Long since;
  private Boolean timestamps;
  private String tail;

  private GetContainerLogsParams() {}

  /**
   * Creates arguments holder with required parameters.
   *
   * @param container id or name of container
   * @return arguments holder with required parameters
   */
  public static GetContainerLogsParams create(@NotNull String container) {
    return new GetContainerLogsParams().withContainer(container);
  }

  /**
   * Adds container to this parameters.
   *
   * @param container id or name of container
   * @return this params instance
   * @throws NullPointerException if {@code container} is null
   */
  public GetContainerLogsParams withContainer(@NotNull String container) {
    requireNonNull(container);
    this.container = container;
    return this;
  }

  /**
   * Shows extra details provided to logs.
   *
   * @param details flag which indicates whether show extra details
   * @return this params instance
   */
  public GetContainerLogsParams withDetails(boolean details) {
    this.details = details;
    return this;
  }

  /**
   * Flag for getting output stream from a container.
   *
   * @param follow if {@code true} gets output stream from container.<br>
   *     Note, that live stream blocks until container is running.
   * @return this params instance
   */
  public GetContainerLogsParams withFollow(boolean follow) {
    this.follow = follow;
    return this;
  }

  /**
   * UNIX timestamp to filter logs.
   *
   * @param since specifying a timestamp will only output log-entries since that timestamp
   * @return this params instance
   */
  public GetContainerLogsParams withSince(long since) {
    this.since = since;
    return this;
  }

  /**
   * Prints timestamps for every log line.
   *
   * @param timestamps flag whether print timestamps for every log line.
   * @return this params instance
   */
  public GetContainerLogsParams withTimestamps(boolean timestamps) {
    this.timestamps = timestamps;
    return this;
  }

  /**
   * Output specified number of lines at the end of logs. Default is all logs.
   *
   * @param tail number of lines at the end of logs or {@code all}
   * @return this params instance
   */
  public GetContainerLogsParams withTail(String tail) {
    this.tail = tail;
    return this;
  }

  public String getContainer() {
    return container;
  }

  public Boolean isDetails() {
    return details;
  }

  public Boolean isFollow() {
    return follow;
  }

  public Long getSince() {
    return since;
  }

  public Boolean isTimestamps() {
    return timestamps;
  }

  public String getTail() {
    return tail;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    GetContainerLogsParams that = (GetContainerLogsParams) o;
    return Objects.equals(container, that.container)
        && Objects.equals(details, that.details)
        && Objects.equals(follow, that.follow)
        && Objects.equals(since, that.since)
        && Objects.equals(timestamps, that.timestamps)
        && Objects.equals(tail, that.tail);
  }

  @Override
  public int hashCode() {
    return Objects.hash(container, details, follow, since, timestamps, tail);
  }
}
