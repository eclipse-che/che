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
package org.eclipse.che.ide.websocket;

/**
 * Thrown when there was a problem with WebSocket connection.
 *
 * @author Artem Zatsarynnyi
 */
@SuppressWarnings("serial")
public class WebSocketException extends Exception {

  public WebSocketException() {
    super();
  }

  public WebSocketException(String message) {
    super(message);
  }

  public WebSocketException(Throwable cause) {
    super(cause);
  }

  public WebSocketException(String message, Throwable cause) {
    super(message, cause);
  }
}
