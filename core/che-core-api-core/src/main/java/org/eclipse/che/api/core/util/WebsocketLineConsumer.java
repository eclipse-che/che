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
package org.eclipse.che.api.core.util;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import org.everrest.websockets.WSConnectionContext;
import org.everrest.websockets.message.ChannelBroadcastMessage;
import org.slf4j.Logger;

/**
 * Line consumer that send messages to specified websocket channel
 *
 * @author Alexander Garagatyi
 */
public class WebsocketLineConsumer implements LineConsumer {
  private static final Logger LOG = getLogger(WebsocketLineConsumer.class);
  private final String channel;

  public WebsocketLineConsumer(String channel) {
    this.channel = channel;
  }

  @Override
  public void writeLine(String line) throws IOException {
    final ChannelBroadcastMessage bm = new ChannelBroadcastMessage();
    bm.setChannel(channel);
    bm.setBody(line);
    try {
      WSConnectionContext.sendMessage(bm);
    } catch (Exception e) {
      LOG.error("A problem occurred while sending websocket message", e);
    }
  }

  @Override
  public void close() throws IOException {}
}
