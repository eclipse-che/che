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
package org.eclipse.che.ide.websocket.impl;

import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.websocket.commons.WebSocketMessageTransmitter;
import org.eclipse.che.ide.util.loging.Log;

/**
 * Web socket transmitter implementation that can transmit a message into opened connection or send
 * a message to pending message re-sender so it could be possible to send it later
 *
 * @author Dmitry Kuleshov
 */
@Singleton
public class BasicWebSocketMessageTransmitter implements WebSocketMessageTransmitter {
  private final WebSocketConnectionManager connectionManager;
  private final MessagesReSender reSender;
  private final UrlResolver urlResolver;

  @Inject
  public BasicWebSocketMessageTransmitter(
      WebSocketConnectionManager connectionManager,
      MessagesReSender reSender,
      UrlResolver resolver) {
    this.connectionManager = connectionManager;
    this.reSender = reSender;
    this.urlResolver = resolver;
  }

  @Override
  public void transmit(String endpointId, String message) {
    final String url = urlResolver.getUrl(endpointId);

    if (connectionManager.isConnectionOpen(url)) {
      Log.debug(getClass(), "Connection is opened, transmitting: " + message);
      connectionManager.sendMessage(url, message);

    } else {
      Log.debug(getClass(), "Connection is closed, adding to pending: " + message);
      reSender.add(endpointId, message);
    }
  }
}
