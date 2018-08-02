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
package org.eclipse.che.api.languageserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URI;
import org.eclipse.che.api.languageserver.LanguageServerConfig.CommunicationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This implementation provides communication to a socket that corresponds to a server that runs a
 * language server. Besides that it also lazily establishes a socket connection.
 *
 * @author Dmytro Kulieshov
 */
class SocketCommunicationProvider implements CommunicationProvider {
  private static final Logger LOG = LoggerFactory.getLogger(SocketCommunicationProvider.class);

  private final String host;
  private final int port;
  private final StatusChecker statusChecker;

  private Socket socket;

  SocketCommunicationProvider(URI uri) {
    this.host = uri.getHost();
    this.port = uri.getPort();

    this.statusChecker =
        new StatusChecker() {
          @Override
          public boolean isAlive() {
            return socket != null && !socket.isInputShutdown() && !socket.isOutputShutdown();
          }

          @Override
          public String getCause() {
            if (socket == null) {
              return "Socket is NULL";
            } else if (socket.isInputShutdown()) {
              return "Socket input is shut down";
            } else if (socket.isOutputShutdown()) {
              return "Socket output is shut down";
            } else {
              return "Unknown reason";
            }
          }
        };
  }

  @Override
  public InputStream getInputStream() throws LanguageServerException {
    establishSocketConnectionLazily();

    try {
      return socket.getInputStream();
    } catch (IOException e) {
      String error = String.format("Can't get socket input stream for: %s:%s", host, port);
      LOG.error(error, e);
      throw new LanguageServerException(error, e);
    }
  }

  @Override
  public OutputStream getOutputStream() throws LanguageServerException {
    establishSocketConnectionLazily();
    try {
      return socket.getOutputStream();
    } catch (IOException e) {
      String error = String.format("Can't get socket output stream for: %s:%s", host, port);
      LOG.error(error, e);
      throw new LanguageServerException(error, e);
    }
  }

  @Override
  public StatusChecker getStatusChecker() throws LanguageServerException {
    establishSocketConnectionLazily();

    return statusChecker;
  }

  private void establishSocketConnectionLazily() throws LanguageServerException {
    LOG.debug("Establishing socket connection lazily");
    try {
      if (socket == null) {
        LOG.debug("Not established, establishing");

        socket = new Socket(host, port);
        socket.setKeepAlive(true);
      } else {
        LOG.debug("Established, skipping");
      }
    } catch (IOException e) {
      String error = String.format("Can't establish socket connection for: %s:%s", host, port);
      LOG.error(error, e);
      throw new LanguageServerException(error, e);
    }
  }
}
