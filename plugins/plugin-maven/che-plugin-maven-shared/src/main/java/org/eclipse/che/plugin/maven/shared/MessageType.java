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
package org.eclipse.che.plugin.maven.shared;

/** @author Evgen Vidolob */
public enum MessageType {
  NOTIFICATION(1),
  UPDATE(2),
  START_STOP(3);

  private final int type;

  MessageType(int type) {
    this.type = type;
  }

  public int getType() {
    return type;
  }

  public static MessageType valueOf(int type) {
    for (MessageType messageType : values()) {
      if (messageType.type == type) {
        return messageType;
      }
    }
    throw new IllegalArgumentException();
  }
}
