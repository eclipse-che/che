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

package org.eclipse.che.ide.js.impl.workspace;

import com.google.web.bindery.event.shared.EventBus;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.ide.api.workspace.event.ExecAgentServerRunningEvent;
import org.eclipse.che.ide.api.workspace.event.ExecAgentServerStoppedEvent;
import org.eclipse.che.ide.api.workspace.event.ServerRunningEvent;
import org.eclipse.che.ide.api.workspace.event.ServerStoppedEvent;
import org.eclipse.che.ide.api.workspace.event.TerminalAgentServerRunningEvent;
import org.eclipse.che.ide.api.workspace.event.TerminalAgentServerStoppedEvent;
import org.eclipse.che.ide.api.workspace.event.WsAgentServerRunningEvent;
import org.eclipse.che.ide.api.workspace.event.WsAgentServerStoppedEvent;
import org.eclipse.che.ide.js.api.Disposable;
import org.eclipse.che.ide.js.api.event.Listener;
import org.eclipse.che.ide.js.api.workspace.WorkspaceRuntime;
import org.eclipse.che.ide.js.api.workspace.event.JsExecAgentServerRunningEvent;
import org.eclipse.che.ide.js.api.workspace.event.JsExecAgentServerStoppedEvent;
import org.eclipse.che.ide.js.api.workspace.event.JsServerRunningEvent;
import org.eclipse.che.ide.js.api.workspace.event.JsServerStoppedEvent;
import org.eclipse.che.ide.js.api.workspace.event.JsTerminalAgentServerRunningEvent;
import org.eclipse.che.ide.js.api.workspace.event.JsTerminalAgentServerStoppedEvent;
import org.eclipse.che.ide.js.api.workspace.event.JsWsAgentServerRunningEvent;
import org.eclipse.che.ide.js.api.workspace.event.JsWsAgentServerStoppedEvent;
import org.eclipse.che.ide.util.ListenerManager;
import org.eclipse.che.ide.util.ListenerRegistrar.Remover;

/** @author Yevhen Vydolob */
@Singleton
public class JsWorkspaceRuntime implements WorkspaceRuntime {

  private final EventBus eventBus;

  private final ListenerManager<Listener<JsServerRunningEvent>> serverRunningListeners =
      ListenerManager.create();
  private final ListenerManager<Listener<JsWsAgentServerRunningEvent>>
      wsAgentServerRunningListeners = ListenerManager.create();
  private final ListenerManager<Listener<JsTerminalAgentServerRunningEvent>>
      terminalAgentServerRunningListeners = ListenerManager.create();
  private final ListenerManager<Listener<JsExecAgentServerRunningEvent>>
      execAgentServerRunningListeners = ListenerManager.create();
  private final ListenerManager<Listener<JsServerStoppedEvent>> serverStoppedListeners =
      ListenerManager.create();
  private final ListenerManager<Listener<JsWsAgentServerStoppedEvent>>
      wsAgentServerStoppedListeners = ListenerManager.create();
  private final ListenerManager<Listener<JsTerminalAgentServerStoppedEvent>>
      terminalAgentServerStoppedListeners = ListenerManager.create();
  private final ListenerManager<Listener<JsExecAgentServerStoppedEvent>>
      execAgentServerStoppedListeners = ListenerManager.create();

  @Inject
  public JsWorkspaceRuntime(EventBus eventBus) {
    this.eventBus = eventBus;
    eventBus.addHandler(
        ServerRunningEvent.TYPE,
        event -> {
          JsServerRunningEvent runningEvent = new JsServerRunningEvent(event);
          serverRunningListeners.dispatch(listener -> listener.on(runningEvent));
        });
    eventBus.addHandler(
        WsAgentServerRunningEvent.TYPE,
        event -> {
          JsWsAgentServerRunningEvent runningEvent = new JsWsAgentServerRunningEvent(event);
          wsAgentServerRunningListeners.dispatch(listener -> listener.on(runningEvent));
        });

    eventBus.addHandler(
        TerminalAgentServerRunningEvent.TYPE,
        event -> {
          JsTerminalAgentServerRunningEvent runningEvent =
              new JsTerminalAgentServerRunningEvent(event);
          terminalAgentServerRunningListeners.dispatch(listener -> listener.on(runningEvent));
        });

    eventBus.addHandler(
        ExecAgentServerRunningEvent.TYPE,
        event -> {
          JsExecAgentServerRunningEvent runningEvent = new JsExecAgentServerRunningEvent(event);
          execAgentServerRunningListeners.dispatch(listener -> listener.on(runningEvent));
        });

    eventBus.addHandler(
        ServerStoppedEvent.TYPE,
        event -> {
          JsServerStoppedEvent stoppedEvent = new JsServerStoppedEvent(event);
          serverStoppedListeners.dispatch(listener -> listener.on(stoppedEvent));
        });

    eventBus.addHandler(
        WsAgentServerStoppedEvent.TYPE,
        event -> {
          JsWsAgentServerStoppedEvent stoppedEvent = new JsWsAgentServerStoppedEvent(event);
          wsAgentServerStoppedListeners.dispatch(listener -> listener.on(stoppedEvent));
        });

    eventBus.addHandler(
        TerminalAgentServerStoppedEvent.TYPE,
        event -> {
          JsTerminalAgentServerStoppedEvent stoppedEvent =
              new JsTerminalAgentServerStoppedEvent(event);
          terminalAgentServerStoppedListeners.dispatch(listener -> listener.on(stoppedEvent));
        });

    eventBus.addHandler(
        ExecAgentServerStoppedEvent.TYPE,
        event -> {
          JsExecAgentServerStoppedEvent stoppedEvent = new JsExecAgentServerStoppedEvent(event);
          execAgentServerStoppedListeners.dispatch(listener -> listener.on(stoppedEvent));
        });
  }

  @Override
  public Disposable addServerRunningListener(Listener<JsServerRunningEvent> listener) {
    return addHandler(serverRunningListeners, listener);
  }

  @Override
  public Disposable addWsAgentServerRunningListener(
      Listener<JsWsAgentServerRunningEvent> listener) {
    return addHandler(wsAgentServerRunningListeners, listener);
  }

  @Override
  public Disposable addTerminalAgentServerRunningListener(
      Listener<JsTerminalAgentServerRunningEvent> listener) {
    return addHandler(terminalAgentServerRunningListeners, listener);
  }

  @Override
  public Disposable addExecAgentServerRunningListener(
      Listener<JsExecAgentServerRunningEvent> listener) {
    return addHandler(execAgentServerRunningListeners, listener);
  }

  @Override
  public Disposable addServerStoppedListener(Listener<JsServerStoppedEvent> listener) {
    return addHandler(serverStoppedListeners, listener);
  }

  @Override
  public Disposable addWsAgentServerStoppedListener(
      Listener<JsWsAgentServerStoppedEvent> listener) {
    return addHandler(wsAgentServerStoppedListeners, listener);
  }

  @Override
  public Disposable addTerminalAgentServerStoppedListener(
      Listener<JsTerminalAgentServerStoppedEvent> listener) {
    return addHandler(terminalAgentServerStoppedListeners, listener);
  }

  @Override
  public Disposable addExecAgentServerStoppedListener(
      Listener<JsExecAgentServerStoppedEvent> listener) {
    return addHandler(execAgentServerStoppedListeners, listener);
  }

  private <T> Disposable addHandler(ListenerManager<Listener<T>> manager, Listener<T> listener) {
    Remover remover = manager.add(listener);
    return remover::remove;
  }
}
