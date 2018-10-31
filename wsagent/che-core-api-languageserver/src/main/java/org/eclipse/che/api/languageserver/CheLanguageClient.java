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

import com.google.inject.assistedinject.Assisted;
import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.languageserver.messager.ShowMessageJsonRpcTransmitter;
import org.eclipse.che.api.languageserver.shared.model.ExtendedPublishDiagnosticsParams;
import org.eclipse.lsp4j.MessageActionItem;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.ShowMessageRequestParams;
import org.eclipse.lsp4j.services.LanguageClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A LSP language client implementation for Che
 *
 * @author Thomas Mäder
 */
public class CheLanguageClient implements LanguageClient {
  private static final Logger LOG = LoggerFactory.getLogger(CheLanguageClient.class);

  private EventService eventService;
  private ShowMessageJsonRpcTransmitter transmitter;
  private String serverId;

  @Inject
  public CheLanguageClient(
      EventService eventService,
      ShowMessageJsonRpcTransmitter transmitter,
      @Assisted String serverId) {
    this.eventService = eventService;
    this.transmitter = transmitter;
    this.serverId = serverId;
  }

  @Override
  public void telemetryEvent(Object object) {
    LOG.debug("Telemetry: {}", object);
  }

  @Override
  public CompletableFuture<MessageActionItem> showMessageRequest(
      ShowMessageRequestParams requestParams) {
    return transmitter.sendShowMessageRequest(requestParams);
  }

  @Override
  public void showMessage(MessageParams messageParams) {
    eventService.publish(messageParams);
  }

  @Override
  public void publishDiagnostics(PublishDiagnosticsParams diagnostics) {
    eventService.publish(new ExtendedPublishDiagnosticsParams(serverId, diagnostics));
  }

  @Override
  public void logMessage(MessageParams message) {
    switch (message.getType()) {
      case Error:
        LOG.error(message.getMessage());
        break;
      case Warning:
        LOG.warn(message.getMessage());
        break;
      case Info:
        LOG.info(message.getMessage());
        break;
      case Log:
        LOG.debug(message.getMessage());
        break;
    }
  }
}
