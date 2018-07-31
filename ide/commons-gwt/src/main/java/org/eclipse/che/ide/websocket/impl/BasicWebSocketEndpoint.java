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
package org.eclipse.che.ide.websocket.impl;

import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.ide.util.loging.Log;

/**
 * Duplex WEB SOCKET endpoint, handles messages, errors, session open/close events.
 *
 * @author Dmitry Kuleshov
 */
@Singleton
public class BasicWebSocketEndpoint implements WebSocketEndpoint {
  private final WebSocketConnectionSustainer sustainer;
  private final MessagesReSender reSender;
  private final WebSocketDispatcher dispatcher;
  private final WebSocketActionManager actionManager;

  @Inject
  public BasicWebSocketEndpoint(
      WebSocketConnectionSustainer sustainer,
      MessagesReSender reSender,
      WebSocketDispatcher dispatcher,
      WebSocketActionManager actionManager) {
    this.sustainer = sustainer;
    this.reSender = reSender;
    this.dispatcher = dispatcher;
    this.actionManager = actionManager;
  }

  @Override
  public void onOpen(String url) {
    Log.debug(getClass(), "Session opened.");

    actionManager.getOnOpenActions(url).forEach(Runnable::run);
    sustainer.reset(url);
    reSender.reSend(url);
  }

  @Override
  public void onClose(String url) {
    Log.debug(getClass(), "Session closed.");

    actionManager.getOnCloseActions(url).forEach(Runnable::run);
    sustainer.sustain(url);
  }

  @Override
  public void onError(String url) {
    Log.warn(getClass(), "Error occurred for endpoint " + url);
  }

  @Override
  public void onMessage(String url, String message) {
    Log.debug(getClass(), "Message received: " + message);

    dispatcher.dispatch(url, message);
  }
}
