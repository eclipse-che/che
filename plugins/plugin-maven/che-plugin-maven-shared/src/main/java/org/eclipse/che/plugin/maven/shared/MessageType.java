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
