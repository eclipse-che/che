/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.core.websocket.impl;

import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;
import static org.eclipse.che.api.core.websocket.impl.WebsocketIdService.randomClientId;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import org.eclipse.che.api.core.websocket.commons.WebSocketMessageReceiver;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Duplex WEB SOCKET endpoint, handles messages, errors, session open/close events.
 *
 * @author Dmitry Kuleshov
 */
public abstract class BasicWebSocketEndpoint {

  private static final Logger LOG = LoggerFactory.getLogger(BasicWebSocketEndpoint.class);

  private final WebSocketSessionRegistry registry;
  private final MessagesReSender reSender;
  private final WebSocketMessageReceiver receiver;
  private final WebsocketIdService identificationService;
  private final Map<Session, StringBuffer> sessionMessagesBuffer = new ConcurrentHashMap<>();

  public BasicWebSocketEndpoint(
      WebSocketSessionRegistry registry,
      MessagesReSender reSender,
      WebSocketMessageReceiver receiver,
      WebsocketIdService identificationService) {

    this.registry = registry;
    this.reSender = reSender;
    this.receiver = receiver;
    this.identificationService = identificationService;
  }

  @OnOpen
  public void onOpen(Session session) {
    String combinedEndpointId = getOrGenerateCombinedEndpointId(session);

    LOG.debug("Web socket session opened");
    LOG.debug("Endpoint: {}", combinedEndpointId);

    session.setMaxIdleTimeout(0);

    registry.add(combinedEndpointId, session);
    reSender.resend(combinedEndpointId);
    sessionMessagesBuffer.put(session, new StringBuffer());
  }

  @OnMessage
  public void onMessage(String messagePart, boolean last, Session session) {
    try {
      EnvironmentContext.getCurrent()
          .setSubject((Subject) session.getUserProperties().get("che_subject"));

      StringBuffer buffer = sessionMessagesBuffer.get(session);
      buffer.append(messagePart);
      if (last) {
        try {
          onMessage(buffer.toString(), session);
        } finally {
          buffer.setLength(0);
        }
      }
    } finally {
      EnvironmentContext.reset();
    }
  }

  public void onMessage(String message, Session session) {
    Optional<String> endpointIdOptional = registry.get(session);

    String combinedEndpointId;
    if (endpointIdOptional.isPresent()) {
      combinedEndpointId = endpointIdOptional.get();

      LOG.trace("Receiving a web socket message.");
      LOG.trace("Endpoint: {}", combinedEndpointId);
      LOG.trace("Message: {}", message);

    } else {
      combinedEndpointId = getOrGenerateCombinedEndpointId(session);

      LOG.warn("Process interferes with an unidentified session");
    }
    receiver.receive(combinedEndpointId, message);
  }

  @OnClose
  public void onClose(CloseReason closeReason, Session session) {
    Optional<String> endpointIdOptional = registry.get(session);

    String combinedEndpointId;
    if (endpointIdOptional.isPresent()) {
      combinedEndpointId = endpointIdOptional.get();

      LOG.debug("Web socket session closed");
      LOG.debug("Endpoint: {}", combinedEndpointId);
      LOG.debug("Close reason: {}:{}", closeReason.getReasonPhrase(), closeReason.getCloseCode());

      registry.remove(combinedEndpointId);
      sessionMessagesBuffer.remove(session);
    } else {
      LOG.warn("Closing unidentified session");
    }
  }

  @OnError
  public void onError(Throwable t, Session session) {
    Optional<String> endpointIdOptional = registry.get(session);

    if (endpointIdOptional.isPresent()) {
      LOG.debug("Web socket session error");
      LOG.debug("Endpoint: {}", endpointIdOptional.get());
    } else {
      LOG.warn("Web socket session error");
      LOG.debug("Unidentified session");
    }
    LOG.debug("Error: ", t);
  }

  protected abstract String getEndpointId();

  private String getOrGenerateCombinedEndpointId(Session session) {
    Map<String, String> queryParamsMap = getQueryParamsMap(session.getQueryString());
    String clientId = queryParamsMap.getOrDefault("clientId", randomClientId());
    return registry
        .get(session)
        .orElse(identificationService.getCombinedId(getEndpointId(), clientId));
  }

  private Map<String, String> getQueryParamsMap(String queryParamsString) {
    Map<String, String> queryParamsMap = new HashMap<>();

    for (String queryParamsPair : Optional.ofNullable(queryParamsString).orElse("").split("&")) {
      String[] pair = queryParamsPair.split("=");
      if (pair.length == 2) {
        queryParamsMap.put(pair[0], pair[1]);
      }
    }

    return queryParamsMap.isEmpty() ? emptyMap() : unmodifiableMap(queryParamsMap);
  }
}
