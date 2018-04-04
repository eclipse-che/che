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
package org.eclipse.che.ide.status.message;

import java.util.Objects;

/**
 * The message to show about Orion editor status.
 *
 * @author Alexander Andrienko
 */
public class StatusMessage {

  private String message;
  private String type;
  private boolean isAccessible;

  public StatusMessage(String message, String type, boolean isAccessible) {
    this.message = message;
    this.type = type;
    this.isAccessible = isAccessible;
  }

  /** Returns the message to show. */
  public String getMessage() {
    return message;
  }

  /** Returns the message type. Either normal or "progress" or "error". */
  public String getType() {
    return type;
  }

  /** If returns <code>true</code>, a screen reader will read this message. */
  public boolean isAccessible() {
    return isAccessible;
  }

  @Override
  public String toString() {
    return "StatusMessage{"
        + "message='"
        + message
        + '\''
        + ", type='"
        + type
        + '\''
        + ", isAccessible="
        + isAccessible
        + '}';
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof StatusMessage)) {
      return false;
    }
    StatusMessage that = (StatusMessage) obj;

    return Objects.equals(message, that.message)
        && Objects.equals(type, that.type)
        && isAccessible == that.isAccessible;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 31 * hash + message.hashCode();
    hash = 31 * hash + type.hashCode();
    hash = 31 * hash + (isAccessible ? 1 : 0);
    return hash;
  }
}
