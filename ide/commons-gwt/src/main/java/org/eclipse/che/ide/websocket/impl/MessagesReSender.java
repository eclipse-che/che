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
package org.eclipse.che.ide.websocket.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.ide.util.loging.Log;

/**
 * Caches messages that was transmitted when a web socket connection was not opened and resends them
 * when the connection is opened again.
 *
 * @author Dmitry Kuleshov
 */
@Singleton
public class MessagesReSender {
  private static final int MAX_MESSAGES = 100;

  private final Map<String, List<String>> messageRegistry = new HashMap<>();

  private final WebSocketConnectionManager connectionManager;
  private final UrlResolver urlResolver;

  @Inject
  public MessagesReSender(WebSocketConnectionManager connectionManager, UrlResolver urlResolver) {
    this.connectionManager = connectionManager;
    this.urlResolver = urlResolver;
  }

  /**
   * Add message that is to be sent when a connection defined be the URL is opened again.
   *
   * @param endpointId endpointId of websocket connection
   * @param message plain text message
   */
  public void add(String endpointId, String message) {
    List<String> messages = messageRegistry.computeIfAbsent(endpointId, k -> new LinkedList<>());

    if (messages.size() <= MAX_MESSAGES) {
      messages.add(message);
    }
  }

  public void reSend(String url) {
    String endpointId = urlResolver.resolve(url);

    if (!messageRegistry.containsKey(endpointId)) {
      return;
    }

    List<String> messages = messageRegistry.get(endpointId);
    if (messages.isEmpty()) {
      return;
    }

    Log.debug(getClass(), "Going to resend websocket messaged: " + messages);

    List<String> backing = new ArrayList<>(messages);
    messages.clear();

    for (String message : backing) {
      if (connectionManager.isConnectionOpen(url)) {
        connectionManager.sendMessage(url, message);
      } else {
        messages.add(message);
      }
    }
  }
}
