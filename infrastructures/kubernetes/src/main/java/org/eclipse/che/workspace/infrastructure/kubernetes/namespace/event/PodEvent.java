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
package org.eclipse.che.workspace.infrastructure.kubernetes.namespace.event;

import java.util.Objects;
import org.eclipse.che.commons.annotation.Nullable;

/**
 * The event that should be published when the pod event occurs, e.g. pulling image, created
 * container
 *
 * @author Sergii Leshchenko
 */
public class PodEvent {
  private final String podName;
  @Nullable private final String containerName;
  private final String reason;
  private final String message;
  private final String creationTimestamp;
  private final String lastTimestamp;

  public PodEvent(
      String podName,
      String containerName,
      String reason,
      String message,
      String creationTimestamp,
      String lastTimestamp) {
    this.podName = podName;
    this.containerName = containerName;
    this.reason = reason;
    this.message = message;
    this.creationTimestamp = creationTimestamp;
    this.lastTimestamp = lastTimestamp;
  }

  /** Returns name of pod related to the event. */
  public String getPodName() {
    return podName;
  }

  /**
   * Returns container name produced by the event. Could be null if the event is related to the pod
   * but not any particular 'container'
   */
  @Nullable
  public String getContainerName() {
    return containerName;
  }

  /** Returns the reason of the event. */
  public String getReason() {
    return reason;
  }

  /** Returns the contents of the event. */
  public String getMessage() {
    return message;
  }

  /** Returns creation timestamp in format '2018-05-15T16:17:54Z' */
  public String getCreationTimeStamp() {
    return creationTimestamp;
  }

  /** Returns last timestamp in format '2018-05-15T16:17:54Z' */
  public String getLastTimestamp() {
    return lastTimestamp;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PodEvent that = (PodEvent) o;
    return Objects.equals(podName, that.podName)
        && Objects.equals(containerName, that.containerName)
        && Objects.equals(reason, that.reason)
        && Objects.equals(message, that.message)
        && Objects.equals(creationTimestamp, that.creationTimestamp)
        && Objects.equals(lastTimestamp, that.lastTimestamp);
  }

  @Override
  public int hashCode() {
    return Objects.hash(podName, containerName, reason, message, creationTimestamp, lastTimestamp);
  }

  @Override
  public String toString() {
    return "PodEvent{"
        + "podName='"
        + podName
        + '\''
        + ", containerName='"
        + containerName
        + '\''
        + ", reason='"
        + reason
        + '\''
        + ", message='"
        + message
        + '\''
        + ", creationTimestamp='"
        + creationTimestamp
        + '\''
        + ", lastTimestamp='"
        + lastTimestamp
        + '\''
        + '}';
  }
}
