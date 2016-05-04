/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.extension.machine.client.perspective.terminal;

import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArrayInteger;
import com.google.gwt.core.client.ScriptInjector;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.Inject;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.promises.client.callback.AsyncPromiseHelper;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.machine.DevMachine;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.collections.Jso;
import org.eclipse.che.ide.extension.machine.client.MachineLocalizationConstant;
import org.eclipse.che.ide.extension.machine.client.perspective.widgets.tab.content.TabPresenter;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.ide.websocket.WebSocket;
import org.eclipse.che.ide.websocket.events.ConnectionErrorHandler;
import org.eclipse.che.ide.websocket.events.ConnectionOpenedHandler;
import org.eclipse.che.ide.websocket.events.MessageReceivedEvent;
import org.eclipse.che.ide.websocket.events.MessageReceivedHandler;

import javax.validation.constraints.NotNull;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;

/**
 * The class defines methods which contains business logic to control machine's terminal.
 *
 * @author Dmitry Shnurenko
 */
public class TerminalPresenter implements TabPresenter, TerminalView.ActionDelegate {

    //event which is performed when user input data into terminal
    private static final String DATA_EVENT_NAME          = "data";
    private static final String EXIT_COMMAND             = "\nexit";
    private static final int    TIME_BETWEEN_CONNECTIONS = 2_000;

    private final TerminalView                view;
    private final NotificationManager         notificationManager;
    private final MachineLocalizationConstant locale;
    private final DevMachine                  machine;
    private final Timer                       retryConnectionTimer;

    private Promise<Boolean>      promise;
    private WebSocket             socket;
    private boolean               isTerminalConnected;
    private int                   countRetry;
    private TerminalJso           terminal;
    private TerminalStateListener terminalStateListener;

    @Inject
    public TerminalPresenter(TerminalView view,
                             NotificationManager notificationManager,
                             MachineLocalizationConstant locale,
                             AppContext appContext) {
        this.view = view;
        view.setDelegate(this);
        this.notificationManager = notificationManager;
        this.locale = locale;
        this.machine = appContext.getDevMachine();

        isTerminalConnected = false;

        promise = AsyncPromiseHelper.createFromAsyncRequest(new AsyncPromiseHelper.RequestCall<Boolean>() {
            @Override
            public void makeCall(final AsyncCallback<Boolean> callback) {
                ScriptInjector.fromUrl(GWT.getModuleBaseURL() + "term/term.js")
                              .setWindow(ScriptInjector.TOP_WINDOW)
                              .setCallback(new Callback<Void, Exception>() {
                                  @Override
                                  public void onFailure(Exception reason) {
                                      callback.onFailure(reason);
                                  }

                                  @Override
                                  public void onSuccess(Void result) {
                                      callback.onSuccess(true);
                                  }
                              }).inject();
            }
        });

        countRetry = 2;

        retryConnectionTimer = new Timer() {
            @Override
            public void run() {
                connect();

                countRetry--;
            }
        };
    }

    /**
     * Connects to special WebSocket which allows get information from terminal on server side. The terminal is initialized only
     * when the method is called the first time.
     */
    public void connect() {
        if (!isTerminalConnected) {
            promise.then(new Operation<Boolean>() {
                @Override
                public void apply(Boolean arg) throws OperationException {
                    connectToTerminalWebSocket(machine.getTerminalUrl());
                }
            }).catchError(new Operation<PromiseError>() {
                @Override
                public void apply(PromiseError arg) throws OperationException {
                    isTerminalConnected = false;
                    notificationManager.notify(locale.failedToConnectTheTerminal(), locale.terminalCanNotLoadScript(), FAIL, FLOAT_MODE);

                    tryToReconnect();

                    if (arg != null) {
                        Log.error(TerminalViewImpl.class, arg);
                    }
                }
            });
        }
    }

    private void tryToReconnect() {
        view.showErrorMessage(locale.terminalTryRestarting());

        if (countRetry <= 0) {
            view.showErrorMessage(locale.terminalErrorStart());
        } else {
            retryConnectionTimer.schedule(TIME_BETWEEN_CONNECTIONS);
        }
    }

    private void connectToTerminalWebSocket(@NotNull String wsUrl) {
        socket = WebSocket.create(wsUrl);
        socket.setOnOpenHandler(new ConnectionOpenedHandler() {
            @Override
            public void onOpen() {
                terminal = TerminalJso.create(TerminalOptionsJso.createDefault());
                isTerminalConnected = true;

                view.openTerminal(terminal);

                terminal.on(DATA_EVENT_NAME, new Operation<String>() {
                    @Override
                    public void apply(String arg) throws OperationException {
                        Jso jso = Jso.create();
                        jso.addField("type", "data");
                        jso.addField("data", arg);
                        socket.send(jso.serialize());
                    }
                });
                socket.setOnMessageHandler(new MessageReceivedHandler() {
                    @Override
                    public void onMessageReceived(MessageReceivedEvent event) {
                        String message = event.getMessage();

                        terminal.write(message);

                        if (message.contains(EXIT_COMMAND) && terminalStateListener != null) {
                            terminalStateListener.onExit();
                        }
                    }
                });
            }
        });

        socket.setOnErrorHandler(new ConnectionErrorHandler() {
            @Override
            public void onError() {
                isTerminalConnected = false;

                notificationManager.notify(locale.connectionFailedWithTerminal(), locale.terminalErrorConnection(), FAIL, FLOAT_MODE);

                tryToReconnect();
            }
        });
    }

    /**
     * Sends 'exit' command on server side to stop terminal.
     */
    public void stopTerminal() {
        if (isTerminalConnected) {
            Jso jso = Jso.create();
            jso.addField("type", "data");
            jso.addField("data", "exit\n");
            socket.send(jso.serialize());
        }
    }

    /** {@inheritDoc} */
    @Override
    public void go(AcceptsOneWidget container) {
        container.setWidget(view);
    }

    /** {@inheritDoc} */
    @Override
    @NotNull
    public IsWidget getView() {
        return view;
    }

    /** {@inheritDoc} */
    @Override
    public void setVisible(boolean visible) {
        view.setVisible(visible);
    }

    @Override
    public void setTerminalSize(int x, int y) {
        if (!isTerminalConnected) {
            return;
        }

        terminal.resize(x, y);
        terminal.focus();
        Jso jso = Jso.create();
        JsArrayInteger arr = Jso.createArray().cast();
        arr.set(0, x);
        arr.set(1, y);
        jso.addField("type", "resize");
        jso.addField("data", arr);
        socket.send(jso.serialize());
    }

    /** Set focus on terminal */
    public void setFocus() {
        if (!isTerminalConnected) {
            return;
        }
        terminal.focus();
    }

    /**
     * Sets listener that will be called when a terminal state changed
     */
    public void setListener(TerminalStateListener listener) {
        this.terminalStateListener = listener;
    }

    /** Listener that will be called when a terminal state changed. */
    public interface TerminalStateListener {
        void onExit();
    }
}
