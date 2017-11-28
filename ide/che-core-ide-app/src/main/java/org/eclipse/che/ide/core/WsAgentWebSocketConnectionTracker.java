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
package org.eclipse.che.ide.core;

import static java.util.Collections.emptyMap;
import static org.eclipse.che.api.core.model.workspace.runtime.ServerStatus.RUNNING;
import static org.eclipse.che.ide.api.jsonrpc.Constants.WS_AGENT_JSON_RPC_ENDPOINT_ID;
import static org.eclipse.che.ide.core.StandardComponentInitializer.STOP_WORKSPACE;
import static org.eclipse.che.ide.util.browser.BrowserUtils.reloadPage;
import static org.eclipse.che.wsagent.shared.Constants.WS_AGENT_TRACK_CONNECTION_HEARTBEAT;
import static org.eclipse.che.wsagent.shared.Constants.WS_AGENT_TRACK_CONNECTION_PERIOD_MILLISECONDS;
import static org.eclipse.che.wsagent.shared.Constants.WS_AGENT_TRACK_CONNECTION_SUBSCRIBE;
import static org.eclipse.che.wsagent.shared.Constants.WS_AGENT_TRACK_CONNECTION_UNSUBSCRIBE;

import com.google.gwt.user.client.Timer;
import com.google.inject.Provider;
import com.google.web.bindery.event.shared.EventBus;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerConfigurator;
import org.eclipse.che.api.core.jsonrpc.commons.RequestTransmitter;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.workspace.WsAgentServerUtil;
import org.eclipse.che.ide.api.workspace.event.WorkspaceStoppedEvent;
import org.eclipse.che.ide.api.workspace.event.WsAgentServerRunningEvent;
import org.eclipse.che.ide.api.workspace.event.WsAgentServerStoppedEvent;
import org.eclipse.che.ide.api.workspace.model.WorkspaceImpl;
import org.eclipse.che.ide.bootstrap.BasicIDEInitializedEvent;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;

/**
 * Tracks web socket connection to control ws agent state. Notifies client side by {@link
 * WsAgentServerStoppedEvent} when the connection is lost. Prompts to restart a workspace when
 * connection is lost, but workspace is running.
 *
 * @author Roman Nikitenko
 */
@Singleton
public class WsAgentWebSocketConnectionTracker {
  private static final int HEART_BEAT_PERIOD =
      3 * WS_AGENT_TRACK_CONNECTION_PERIOD_MILLISECONDS + 1_000;

  private EventBus eventBus;
  private AppContext appContext;
  private DialogFactory dialogFactory;
  private ActionManager actionManager;
  private RequestTransmitter requestTransmitter;
  private CoreLocalizationConstant localizationConstant;
  private Provider<WsAgentServerUtil> wsAgentServerUtilProvider;

  private final Timer heartBeatTimer =
      new Timer() {
        @Override
        public void run() {
          onWsAgentConnectionIsLost();
        }
      };

  @Inject
  public WsAgentWebSocketConnectionTracker(
      EventBus eventBus,
      AppContext appContext,
      DialogFactory dialogFactory,
      ActionManager actionManager,
      RequestTransmitter transmitter,
      RequestHandlerConfigurator requestHandler,
      CoreLocalizationConstant localizationConstant,
      Provider<WsAgentServerUtil> wsAgentServerUtilProvider) {
    this.appContext = appContext;
    this.eventBus = eventBus;
    this.actionManager = actionManager;
    this.requestTransmitter = transmitter;
    this.dialogFactory = dialogFactory;
    this.localizationConstant = localizationConstant;
    this.wsAgentServerUtilProvider = wsAgentServerUtilProvider;

    requestHandler
        .newConfiguration()
        .methodName(WS_AGENT_TRACK_CONNECTION_HEARTBEAT)
        .paramsAsBoolean()
        .resultAsBoolean()
        .withFunction(
            aBoolean -> {
              restartTimer();
              return true;
            });

    eventBus.addHandler(
        BasicIDEInitializedEvent.TYPE,
        event ->
            wsAgentServerUtilProvider
                .get()
                .getWsAgentHttpServer()
                .filter(server -> RUNNING == server.getStatus())
                .ifPresent(
                    server -> {
                      subscribe();
                      restartTimer();
                    }));
    eventBus.addHandler(
        WsAgentServerRunningEvent.TYPE,
        e -> {
          subscribe();
          restartTimer();
        });
    eventBus.addHandler(
        WsAgentServerStoppedEvent.TYPE,
        e -> {
          unsubscribe();
          heartBeatTimer.cancel();
        });
    eventBus.addHandler(
        WorkspaceStoppedEvent.TYPE,
        e -> {
          unsubscribe();
          heartBeatTimer.cancel();
        });
  }

  private void restartTimer() {
    heartBeatTimer.cancel();
    heartBeatTimer.schedule(HEART_BEAT_PERIOD);
  }

  private void onWsAgentConnectionIsLost() {
    WorkspaceImpl workspace = appContext.getWorkspace();
    if (workspace.getStatus() != WorkspaceStatus.RUNNING) {
      return;
    }

    wsAgentServerUtilProvider
        .get()
        .getWsAgentServerMachine()
        .ifPresent(machine -> eventBus.fireEvent(new WsAgentServerStoppedEvent(machine.getName())));

    dialogFactory
        .createChoiceDialog(
            localizationConstant.wsAgentNotRespondingDialogTitle(),
            localizationConstant.wsAgentNotRespondingDialogMessage(),
            localizationConstant.wsAgentNotRespondingDialogRestart(),
            localizationConstant.wsAgentNotRespondingDialogClose(),
            () -> {
              eventBus.addHandler(WorkspaceStoppedEvent.TYPE, event -> reloadPage(false));
              actionManager.performAction(STOP_WORKSPACE, emptyMap());
            },
            null)
        .show();
  }

  private void subscribe() {
    requestTransmitter
        .newRequest()
        .endpointId(WS_AGENT_JSON_RPC_ENDPOINT_ID)
        .methodName(WS_AGENT_TRACK_CONNECTION_SUBSCRIBE)
        .noParams()
        .sendAndSkipResult();
  }

  private void unsubscribe() {
    requestTransmitter
        .newRequest()
        .endpointId(WS_AGENT_JSON_RPC_ENDPOINT_ID)
        .methodName(WS_AGENT_TRACK_CONNECTION_UNSUBSCRIBE)
        .noParams()
        .sendAndSkipResult();
  }
}
