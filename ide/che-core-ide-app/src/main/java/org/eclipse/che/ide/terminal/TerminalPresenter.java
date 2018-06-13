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
import static org.eclipse.che.ide.terminal.TerminalInitializer.FIT_ADDON;
import static org.eclipse.che.ide.terminal.TerminalInitializer.XTERM_JS_MODULE;
import static org.eclipse.che.ide.websocket.events.WebSocketClosedEvent.CLOSE_NORMAL;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayInteger;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.mvp.Presenter;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.theme.Theme;
import org.eclipse.che.ide.api.theme.ThemeAgent;
import org.eclipse.che.ide.api.workspace.model.MachineImpl;
import org.eclipse.che.ide.api.workspace.model.ServerImpl;
import org.eclipse.che.ide.collections.Jso;
import org.eclipse.che.ide.core.AgentURLModifier;
import org.eclipse.che.ide.terminal.options.TerminalOptionsJso;
import org.eclipse.che.ide.terminal.options.TerminalThemeJso;
import org.eclipse.che.ide.websocket.WebSocket;
import org.eclipse.che.requirejs.ModuleHolder;

/**
 * The class defines methods which contains business logic to control machine's terminal.
 *
 * @author Dmitry Shnurenko
 */
public class TerminalPresenter implements Presenter, TerminalView.ActionDelegate {

  // event which is performed when user input data into terminal
  private static final String DATA_EVENT_NAME = "data";
  private static final String RESIZE_EVENT_NAME = "resize";
  private static final int TIME_BETWEEN_CONNECTIONS = 2_000;

  private final TerminalView                    view;
  private final TerminalOptionsJso              options;
  private final NotificationManager             notificationManager;
  private final CoreLocalizationConstant        locale;
  private final MachineImpl                     machine;
  private final TerminalInitializePromiseHolder terminalHolder;
  private final ModuleHolder                    moduleHolder;
  private final AgentURLModifier                agentURLModifier;
  private final ThemeAgent                      themeAgent;
  private final boolean focusOnOpen;

  private WebSocket socket;
  private boolean connected;
  private int countRetry;
  private TerminalStateListener terminalStateListener;

  @Inject
  public TerminalPresenter(
      TerminalView view,
      NotificationManager notificationManager,
      CoreLocalizationConstant locale,
      @NotNull @Assisted MachineImpl machine,
      @Assisted TerminalOptionsJso options,
      @Assisted boolean focusOnOpen,
      final TerminalInitializePromiseHolder terminalHolder,
      final ModuleHolder moduleHolder,
      AgentURLModifier agentURLModifier,
      ThemeAgent themeAgent) {
    this.view = view;
    this.options = options != null ? options : TerminalOptionsJso.create();
    this.focusOnOpen = focusOnOpen;
    this.agentURLModifier = agentURLModifier;
    view.setDelegate(this);
    this.notificationManager = notificationManager;
    this.locale = locale;
    this.machine = machine;

    connected = false;
    countRetry = 2;
    this.terminalHolder = terminalHolder;
    this.moduleHolder = moduleHolder;
    this.themeAgent = themeAgent;
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
      terminalHolder
          .getInitializerPromise()
          .then(
              aVoid -> {
                ServerImpl terminalServer =
                    machine
                        .getServerByName(SERVER_TERMINAL_REFERENCE)
                        .orElseThrow(
                            () ->
                                new OperationException(
                                    "Machine "
                                        + machine.getName()
                                        + " doesn't provide terminal server."));
                connectToTerminal(agentURLModifier.modify(terminalServer.getUrl()));
              })
          .catchError(
              arg -> {
                notificationManager.notify(
                    locale.failedToConnectTheTerminal(),
                    locale.terminalCanNotLoadScript(),
                    FAIL,
                    NOT_EMERGE_MODE);
                reconnect();
              });
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
    TerminalJso terminal = createTerminal();
    socket = WebSocket.create(wsUrl);

    socket.setOnMessageHandler(event -> terminal.write(event.getMessage()));

    socket.setOnCloseHandler(
        event -> {
          if (CLOSE_NORMAL == event.getCode()) {
            connected = false;
            terminalStateListener.onExit();
            terminal.destroy();
          }
        });

    socket.setOnOpenHandler(
        () -> {
          connected = true;

          view.setTerminal(terminal, focusOnOpen);

          terminal.on(
              RESIZE_EVENT_NAME,
              data -> {
                TerminalGeometryJso geometry = (TerminalGeometryJso)data;
                setTerminalSize(geometry.getCols(), geometry.getRows());
              });

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

  private TerminalJso createTerminal() {
    setUpTerminalTheme();

    JavaScriptObject terminalJso = moduleHolder.getModule(XTERM_JS_MODULE);
    TerminalJso terminal = TerminalJso.create(terminalJso, options);

    JavaScriptObject fitJso = moduleHolder.getModule(FIT_ADDON);
    terminal.applyAddon(fitJso);

    terminal.attachCustomKeyEventHandler(CustomKeyEventTerminalHandler.create());

    return terminal;
  }

  private void setUpTerminalTheme() {
    if (options.getTheme() == null) {
      Theme ideTheme = themeAgent.getTheme(themeAgent.getCurrentThemeId());
      TerminalThemeJso terminalTheme = TerminalThemeJso.create();
      terminalTheme.setCursor(ideTheme.getBlueIconColor());
      terminalTheme.setBackGround(ideTheme.outputBackgroundColor());
      terminalTheme.setForeGround(ideTheme.getOutputFontColor());

      options.setTheme(terminalTheme);
    }
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
