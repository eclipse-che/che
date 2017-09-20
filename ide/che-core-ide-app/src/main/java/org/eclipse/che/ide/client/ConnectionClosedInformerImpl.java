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
package org.eclipse.che.ide.client;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.Arrays.asList;
import static org.eclipse.che.ide.websocket.events.WebSocketClosedEvent.CLOSE_ABNORMAL;
import static org.eclipse.che.ide.websocket.events.WebSocketClosedEvent.CLOSE_FAILURE_TLS_HANDSHAKE;
import static org.eclipse.che.ide.websocket.events.WebSocketClosedEvent.CLOSE_GOING_AWAY;
import static org.eclipse.che.ide.websocket.events.WebSocketClosedEvent.CLOSE_INCONSISTENT_DATA;
import static org.eclipse.che.ide.websocket.events.WebSocketClosedEvent.CLOSE_NEGOTIATE_EXTENSION;
import static org.eclipse.che.ide.websocket.events.WebSocketClosedEvent.CLOSE_NORMAL;
import static org.eclipse.che.ide.websocket.events.WebSocketClosedEvent.CLOSE_NO_STATUS;
import static org.eclipse.che.ide.websocket.events.WebSocketClosedEvent.CLOSE_PROTOCOL_ERROR;
import static org.eclipse.che.ide.websocket.events.WebSocketClosedEvent.CLOSE_TOO_LARGE;
import static org.eclipse.che.ide.websocket.events.WebSocketClosedEvent.CLOSE_UNEXPECTED_CONDITION;
import static org.eclipse.che.ide.websocket.events.WebSocketClosedEvent.CLOSE_UNSUPPORTED;
import static org.eclipse.che.ide.websocket.events.WebSocketClosedEvent.CLOSE_VIOLATE_POLICY;

import com.google.inject.Inject;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.ConnectionClosedInformer;
import org.eclipse.che.ide.api.dialogs.DialogFactory;
import org.eclipse.che.ide.websocket.events.WebSocketClosedEvent;

/**
 * Notify that WebSocket connection was closed.
 *
 * @author Roman Nikitenko
 */
public class ConnectionClosedInformerImpl implements ConnectionClosedInformer {

  private DialogFactory dialogFactory;
  private CoreLocalizationConstant localizationConstant;

  private static final Set<Integer> CLOSE_CODES =
      new HashSet<>(
          asList(
              CLOSE_ABNORMAL,
              CLOSE_NORMAL,
              CLOSE_GOING_AWAY,
              CLOSE_PROTOCOL_ERROR,
              CLOSE_UNSUPPORTED,
              CLOSE_NO_STATUS,
              CLOSE_INCONSISTENT_DATA,
              CLOSE_VIOLATE_POLICY,
              CLOSE_TOO_LARGE,
              CLOSE_NEGOTIATE_EXTENSION,
              CLOSE_UNEXPECTED_CONDITION,
              CLOSE_FAILURE_TLS_HANDSHAKE));

  @Inject
  public ConnectionClosedInformerImpl(
      DialogFactory dialogFactory, CoreLocalizationConstant localizationConstant) {
    this.dialogFactory = dialogFactory;
    this.localizationConstant = localizationConstant;
  }

  @Override
  public void onConnectionClosed(WebSocketClosedEvent event) {
    Integer code = event.getCode();
    String reason = event.getReason();

    if (CLOSE_ABNORMAL == code && isNullOrEmpty(reason)) {
      return;
    }

    if (CLOSE_CODES.contains(code)) {
      dialogFactory
          .createMessageDialog(
              localizationConstant.connectionClosedDialogTitle(),
              localizationConstant.messagesServerFailure(),
              null)
          .show();
    }
  }
}
