/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.openshift.project.event;

import java.util.Objects;

/**
 * The event that should be published when container change occurs, e.g. image pulled, container
 * started.
 *
 * @author Sergii Leshchenko
 */
public class ContainerEvent {
  private final String machineName;
  private final String message;
  private final String time;

  public ContainerEvent(String machineName, String message, String time) {
    this.machineName = machineName;
    this.message = message;
    this.time = time;
  }

  /** Returns the name of the machine that produces the logs. */
  public String getMachineName() {
    return machineName;
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
    return Objects.equals(machineName, that.machineName)
        && Objects.equals(message, that.message)
        && Objects.equals(time, that.time);
  }

  @Override
  public int hashCode() {
    return Objects.hash(machineName, message, time);
  }

  @Override
  public String toString() {
    return "ContainerEvent{"
        + "machineName='"
        + machineName
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
