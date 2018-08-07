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
package org.eclipse.che.ide.ext.java.client.progressor;

import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.RUNNING;
import static org.eclipse.che.ide.api.jsonrpc.Constants.WS_AGENT_JSON_RPC_ENDPOINT_ID;
import static org.eclipse.che.ide.ext.java.shared.Constants.PROGRESS_OUTPUT_SUBSCRIBE;
import static org.eclipse.che.ide.ext.java.shared.Constants.PROGRESS_REPORT_METHOD;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerConfigurator;
import org.eclipse.che.api.core.jsonrpc.commons.RequestTransmitter;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.workspace.event.WorkspaceRunningEvent;
import org.eclipse.che.jdt.ls.extension.api.dto.ProgressReport;

/**
 * The mechanism for handling all messages from the jdt.ls server and applying registered consumers.
 */
@Singleton
public class ProgressorJsonRpcHandler {
  private final RequestHandlerConfigurator configurator;
  private final RequestTransmitter requestTransmitter;

  private Set<Consumer<ProgressReport>> progressReportConsumers = new HashSet<>();

  @Inject
  public ProgressorJsonRpcHandler(
      RequestHandlerConfigurator configurator,
      AppContext appContext,
      EventBus eventBus,
      RequestTransmitter requestTransmitter) {
    this.configurator = configurator;
    this.requestTransmitter = requestTransmitter;

    handleProgressesReports();
    eventBus.addHandler(WorkspaceRunningEvent.TYPE, e -> subscribe());
    if (appContext.getWorkspace().getStatus() == RUNNING) {
      subscribe();
    }
  }

  private void subscribe() {
    requestTransmitter
        .newRequest()
        .endpointId(WS_AGENT_JSON_RPC_ENDPOINT_ID)
        .methodName(PROGRESS_OUTPUT_SUBSCRIBE)
        .noParams()
        .sendAndSkipResult();
  }

  /**
   * Adds consumer for the event with {@link ProgressReport}.
   *
   * @param consumer new consumer
   */
  void addProgressReportHandler(Consumer<ProgressReport> consumer) {
    progressReportConsumers.add(consumer);
  }

  private void handleProgressesReports() {
    configurator
        .newConfiguration()
        .methodName(PROGRESS_REPORT_METHOD)
        .paramsAsDto(ProgressReport.class)
        .noResult()
        .withConsumer(
            progressNotification ->
                progressReportConsumers.forEach(it -> it.accept(progressNotification)));
  }
}
