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
package org.eclipse.che.ide.api.workspace.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

/**
 * Fired when some server in some machine goes into a running state.
 *
 * @see WsAgentServerRunningEvent
 * @see TerminalAgentServerRunningEvent
 * @see ExecAgentServerRunningEvent
 */
public class ServerRunningEvent extends GwtEvent<ServerRunningEvent.Handler> {

  public static final Type<ServerRunningEvent.Handler> TYPE = new Type<>();

  private final String serverName;
  private final String machineName;

  public ServerRunningEvent(String serverName, String machineName) {
    this.serverName = serverName;
    this.machineName = machineName;
  }

  /** Returns the running server's name. */
  public String getServerName() {
    return serverName;
  }

  /** Returns the related machine's name. */
  public String getMachineName() {
    return machineName;
  }

  @Override
  public Type<Handler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onServerRunning(this);
  }

  public interface Handler extends EventHandler {
    void onServerRunning(ServerRunningEvent event);
  }
}
