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
package org.eclipse.che.ide.websocket.events;

/**
 * Event is fired, when WebSocket message was received.
 *
 * @author Artem Zatsarynnyi
 */
public class MessageReceivedEvent {
  /** Received message. */
  private String message;

  public MessageReceivedEvent(String message) {
    this.message = message;
  }

  /**
   * Returns message.
   *
   * @return message
   */
  public String getMessage() {
    return message;
  }
}
