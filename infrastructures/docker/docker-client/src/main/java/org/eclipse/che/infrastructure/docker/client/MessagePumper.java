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
package org.eclipse.che.infrastructure.docker.client;

import java.io.IOException;

/**
 * Pumps messages from {@code JsonMessageReader} to {@code MessageProcessor}.
 *
 * @author Alexander Garagatyi
 */
class MessagePumper<T> {
  private final JsonMessageReader<T> messageReader;
  private final MessageProcessor<T> messageProcessor;

  MessagePumper(JsonMessageReader<T> messageReader, MessageProcessor<T> messageProcessor) {
    this.messageReader = messageReader;
    this.messageProcessor = messageProcessor;
  }

  void start() throws IOException {
    T message;
    for (; (message = messageReader.next()) != null; ) {
      messageProcessor.process(message);
    }
  }
}
