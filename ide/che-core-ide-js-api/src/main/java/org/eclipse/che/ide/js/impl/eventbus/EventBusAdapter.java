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

package org.eclipse.che.ide.js.impl.eventbus;

import com.google.web.bindery.event.shared.EventBus;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.ide.api.editor.events.FileEvent;
import org.eclipse.che.ide.api.workspace.event.ExecAgentServerRunningEvent;
import org.eclipse.che.ide.api.workspace.event.ExecAgentServerStoppedEvent;
import org.eclipse.che.ide.api.workspace.event.ServerRunningEvent;
import org.eclipse.che.ide.api.workspace.event.ServerStoppedEvent;
import org.eclipse.che.ide.api.workspace.event.TerminalAgentServerRunningEvent;
import org.eclipse.che.ide.api.workspace.event.TerminalAgentServerStoppedEvent;
import org.eclipse.che.ide.api.workspace.event.WsAgentServerRunningEvent;
import org.eclipse.che.ide.api.workspace.event.WsAgentServerStoppedEvent;
import org.eclipse.che.ide.js.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.js.api.editor.event.EditorOpenedEvent;
import org.eclipse.che.ide.js.api.editor.event.FileOperationEvent;
import org.eclipse.che.ide.js.api.workspace.event.JsExecAgentServerRunningEvent;
import org.eclipse.che.ide.js.api.workspace.event.JsExecAgentServerStoppedEvent;
import org.eclipse.che.ide.js.api.workspace.event.JsServerRunningEvent;
import org.eclipse.che.ide.js.api.workspace.event.JsServerStoppedEvent;
import org.eclipse.che.ide.js.api.workspace.event.JsTerminalAgentServerRunningEvent;
import org.eclipse.che.ide.js.api.workspace.event.JsTerminalAgentServerStoppedEvent;
import org.eclipse.che.ide.js.api.workspace.event.JsWsAgentServerRunningEvent;
import org.eclipse.che.ide.js.api.workspace.event.JsWsAgentServerStoppedEvent;

/** @author Yevhen Vydolob */
@Singleton
public class EventBusAdapter {
  private final EventBus eventBus;
  private final org.eclipse.che.ide.api.eventbus.EventBus jsBus;

  @Inject
  public EventBusAdapter(EventBus eventBus, org.eclipse.che.ide.api.eventbus.EventBus jsBus) {
    this.eventBus = eventBus;
    this.jsBus = jsBus;
    eventBus.addHandler(
        ServerRunningEvent.TYPE,
        event -> {
          JsServerRunningEvent runningEvent = new JsServerRunningEvent(event);
          jsBus.fire(JsServerRunningEvent.TYPE, runningEvent);
        });
    eventBus.addHandler(
        WsAgentServerRunningEvent.TYPE,
        event -> {
          JsWsAgentServerRunningEvent runningEvent = new JsWsAgentServerRunningEvent(event);
          jsBus.fire(JsWsAgentServerRunningEvent.TYPE, runningEvent);
        });

    eventBus.addHandler(
        TerminalAgentServerRunningEvent.TYPE,
        event -> {
          JsTerminalAgentServerRunningEvent runningEvent =
              new JsTerminalAgentServerRunningEvent(event);
          jsBus.fire(JsTerminalAgentServerRunningEvent.TYPE, runningEvent);
        });

    eventBus.addHandler(
        ExecAgentServerRunningEvent.TYPE,
        event -> {
          JsExecAgentServerRunningEvent runningEvent = new JsExecAgentServerRunningEvent(event);
          jsBus.fire(JsExecAgentServerRunningEvent.TYPE, runningEvent);
        });

    eventBus.addHandler(
        ServerStoppedEvent.TYPE,
        event -> {
          JsServerStoppedEvent stoppedEvent = new JsServerStoppedEvent(event);
          jsBus.fire(JsServerStoppedEvent.TYPE, stoppedEvent);
        });

    eventBus.addHandler(
        WsAgentServerStoppedEvent.TYPE,
        event -> {
          JsWsAgentServerStoppedEvent stoppedEvent = new JsWsAgentServerStoppedEvent(event);
          jsBus.fire(JsWsAgentServerStoppedEvent.TYPE, stoppedEvent);
        });

    eventBus.addHandler(
        TerminalAgentServerStoppedEvent.TYPE,
        event -> {
          JsTerminalAgentServerStoppedEvent stoppedEvent =
              new JsTerminalAgentServerStoppedEvent(event);
          jsBus.fire(JsTerminalAgentServerStoppedEvent.TYPE, stoppedEvent);
        });

    eventBus.addHandler(
        ExecAgentServerStoppedEvent.TYPE,
        event -> {
          JsExecAgentServerStoppedEvent stoppedEvent = new JsExecAgentServerStoppedEvent(event);
          jsBus.fire(JsExecAgentServerStoppedEvent.TYPE, stoppedEvent);
        });
    eventBus.addHandler(
        FileEvent.TYPE,
        event -> {
          FileOperationEvent operationEvent =
              new FileOperationEvent(event.getFile(), event.getOperationType());
          jsBus.fire(FileOperationEvent.TYPE, operationEvent);
        });

    eventBus.addHandler(
        org.eclipse.che.ide.api.editor.EditorOpenedEvent.TYPE,
        event -> {
          EditorOpenedEvent openedEvent =
              new EditorOpenedEvent(event.getFile(), new EditorPartPresenter() {});
          jsBus.fire(EditorOpenedEvent.TYPE, openedEvent);
        });
  }
}
