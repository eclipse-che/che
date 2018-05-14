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
package org.eclipse.che.workspace.infrastructure.kubernetes.namespace.event;

import java.util.Objects;

/**
 * The event that should be published when container change occurs, e.g. image pulled, container
 * started.
 *
 * @author Sergii Leshchenko
 */
public class ContainerEvent {
  private final String podName;
  private final String containerName;
  private final String reason;
  private final String message;
  private final String time;

  public ContainerEvent(
      String podName, String containerName, String reason, String message, String time) {
    this.podName = podName;
    this.containerName = containerName;
    this.reason = reason;
    this.message = message;
    this.time = time;
  }

  /** Returns name of pod related to container. */
  public String getPodName() {
    return podName;
  }

  /** Returns container name which produced event. */
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

  /** Returns time in format '2017-06-27T17:11:09.306+03:00' */
  public String getTime() {
    return time;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ContainerEvent that = (ContainerEvent) o;
    return Objects.equals(podName, that.podName)
        && Objects.equals(containerName, that.containerName)
        && Objects.equals(reason, that.reason)
        && Objects.equals(message, that.message)
        && Objects.equals(time, that.time);
  }

  @Override
  public int hashCode() {
    return Objects.hash(podName, containerName, reason, message, time);
  }

  @Override
  public String toString() {
    return "ContainerEvent{"
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
        + ", time='"
        + time
        + '\''
        + '}';
  }
}
