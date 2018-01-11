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
package org.eclipse.che.ide.terminal;

import static org.eclipse.che.api.workspace.shared.Constants.SERVER_TERMINAL_REFERENCE;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.NOT_EMERGE_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.websocket.events.WebSocketClosedEvent.CLOSE_NORMAL;

import com.google.gwt.core.client.JsArrayInteger;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import java.util.Optional;
import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.mvp.Presenter;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.workspace.model.MachineImpl;
import org.eclipse.che.ide.api.workspace.model.ServerImpl;
import org.eclipse.che.ide.collections.Jso;
import org.eclipse.che.ide.core.AgentURLModifier;
import org.eclipse.che.ide.websocket.WebSocket;

/**
 * The class defines methods which contains business logic to control machine's terminal.
 *
 * @author Dmitry Shnurenko
 */
public class TerminalPresenter implements Presenter, TerminalView.ActionDelegate {

  // event which is performed when user input data into terminal
  private static final String DATA_EVENT_NAME = "data";
  private static final int TIME_BETWEEN_CONNECTIONS = 2_000;

  private final TerminalView view;
  private final TerminalOptions options;
  private final NotificationManager notificationManager;
  private final CoreLocalizationConstant locale;
  private final MachineImpl machine;
  private final AgentURLModifier agentURLModifier;

  private WebSocket socket;
  private boolean connected;
  private int countRetry;
  private Terminal terminal;
  private TerminalStateListener terminalStateListener;
  private int width;
  private int height;

  @Inject
  public TerminalPresenter(
      TerminalView view,
      NotificationManager notificationManager,
      CoreLocalizationConstant locale,
      @NotNull @Assisted MachineImpl machine,
      @Assisted TerminalOptions options,
      AgentURLModifier agentURLModifier) {
    this.view = view;
    this.options = options;
    this.agentURLModifier = agentURLModifier;
    view.setDelegate(this);
    this.notificationManager = notificationManager;
    this.locale = locale;
    this.machine = machine;

    connected = false;
    countRetry = 2;
  }

  /**
   * Connects to special WebSocket which allows get information from terminal on server side. The
   * terminal is initialized only when the method is called the first time.
   */
  public void connect() {
    if (countRetry == 0) {
      return;
    }

    if (!connected) {
      Optional<ServerImpl> termServerOptional = machine.getServerByName(SERVER_TERMINAL_REFERENCE);

      if (termServerOptional.isPresent()) {
        String serverUrl = termServerOptional.get().getUrl();
        connectToTerminal(agentURLModifier.modify(serverUrl));
      } else {
        notificationManager.notify(
            locale.failedToConnectTheTerminal(),
            locale.terminalCanNotLoadScript(),
            FAIL,
            NOT_EMERGE_MODE);
        reconnect();
      }
    }
  }

  private void reconnect() {
    if (countRetry <= 0) {
      view.showErrorMessage(locale.terminalErrorStart());
    } else {
      view.showErrorMessage(locale.terminalTryRestarting());
      new Timer() {
        @Override
        public void run() {
          connect();
        }
      }.schedule(TIME_BETWEEN_CONNECTIONS);
    }
  }

  private void connectToTerminal(@NotNull String wsUrl) {
    countRetry--;

    socket = WebSocket.create(wsUrl);

    socket.setOnMessageHandler(event -> terminal.write(event.getMessage()));

    socket.setOnCloseHandler(
        event -> {
          if (CLOSE_NORMAL == event.getCode()) {
            connected = false;
            terminalStateListener.onExit();
          }
        });

    socket.setOnOpenHandler(
        () -> {
          terminal = new Terminal(options);

          connected = true;

          view.openTerminal(terminal);

          terminal.on(
              DATA_EVENT_NAME,
              data -> {
                Jso jso = Jso.create();
                jso.addField("type", "data");
                jso.addField("data", data);
                socket.send(jso.serialize());
              });
        });

    socket.setOnErrorHandler(
        () -> {
          connected = false;

          if (countRetry == 0) {
            view.showErrorMessage(locale.terminalErrorStart());
            notificationManager.notify(
                locale.connectionFailedWithTerminal(),
                locale.terminalErrorConnection(),
                FAIL,
                FLOAT_MODE);
          } else {
            reconnect();
          }
        });
  }

  /** Sends 'close' message on server side to stop terminal. */
  public void stopTerminal() {
    if (connected) {
      Jso jso = Jso.create();
      jso.addField("type", "close");
      socket.send(jso.serialize());
    }
  }

  /** {@inheritDoc} */
  @Override
  public void go(AcceptsOneWidget container) {
    container.setWidget(view);
  }

  @NotNull
  public IsWidget getView() {
    return view;
  }

  public void setVisible(boolean visible) {
    view.setVisible(visible);
  }

  @Override
  public void setTerminalSize(int x, int y) {
    if (!connected) {
      return;
    }

    if (width == x && height == y) {
      return;
    }

    terminal.resize(x, y);
    width = x;
    height = y;

    Jso jso = Jso.create();
    JsArrayInteger arr = Jso.createArray().cast();
    arr.set(0, x);
    arr.set(1, y);
    jso.addField("type", "resize");
    jso.addField("data", arr);
    socket.send(jso.serialize());
  }

  /** Sets listener that will be called when a terminal state changed */
  public void setListener(TerminalStateListener listener) {
    this.terminalStateListener = listener;
  }

  /** Listener that will be called when a terminal state changed. */
  public interface TerminalStateListener {
    void onExit();
  }
}
