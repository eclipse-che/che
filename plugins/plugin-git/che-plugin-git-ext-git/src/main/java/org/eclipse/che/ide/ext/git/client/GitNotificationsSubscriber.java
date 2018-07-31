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
package org.eclipse.che.ide.ext.git.client;

import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.RUNNING;
import static org.eclipse.che.ide.api.jsonrpc.Constants.WS_AGENT_JSON_RPC_ENDPOINT_ID;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import org.eclipse.che.api.core.jsonrpc.commons.RequestTransmitter;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.workspace.event.WsAgentServerRunningEvent;

/** Subscribes on receiving notifications from git. */
@Singleton
public class GitNotificationsSubscriber {

  private final EventBus eventBus;
  private final AppContext appContext;
  private final RequestTransmitter requestTransmitter;

  @Inject
  public GitNotificationsSubscriber(
      EventBus eventBus, AppContext appContext, RequestTransmitter requestTransmitter) {
    this.eventBus = eventBus;
    this.appContext = appContext;
    this.requestTransmitter = requestTransmitter;
  }

  void subscribe() {
    eventBus.addHandler(WsAgentServerRunningEvent.TYPE, event -> initialize());

    if (appContext.getWorkspace().getStatus() == RUNNING) {
      initialize();
    }
  }

  private void initialize() {
    initializeGitCheckoutWatcher();
    initializeGitIndexWatcher();
    initializeGitEventsWatcher();
  }

  private void initializeGitCheckoutWatcher() {
    requestTransmitter
        .newRequest()
        .endpointId(WS_AGENT_JSON_RPC_ENDPOINT_ID)
        .methodName("track/git-checkout")
        .noParams()
        .sendAndSkipResult();
  }

  private void initializeGitIndexWatcher() {
    requestTransmitter
        .newRequest()
        .endpointId(WS_AGENT_JSON_RPC_ENDPOINT_ID)
        .methodName("track/git-index")
        .noParams()
        .sendAndSkipResult();
  }

  private void initializeGitEventsWatcher() {
    requestTransmitter
        .newRequest()
        .endpointId(WS_AGENT_JSON_RPC_ENDPOINT_ID)
        .methodName("event/git/subscribe")
        .noParams()
        .sendAndSkipResult();
  }
}
