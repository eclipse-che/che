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
package org.eclipse.che.ide.websocket.events;

/**
 * Handler for {@link MessageReceivedEvent} event.
 *
 * @author Artem Zatsarynnyi
 */
public interface MessageReceivedHandler {
  /**
   * Perform actions, when a WebSocket message was received.
   *
   * @param event {@link MessageReceivedEvent}
   */
  void onMessageReceived(MessageReceivedEvent event);
}
