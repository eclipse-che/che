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
 * Fired when exec-agent server in some machine goes into a running state. Allows to avoid checking
 * server's name unlike {@link ServerRunningEvent}.
 *
 * @see ServerRunningEvent
 */
public class ExecAgentServerRunningEvent extends GwtEvent<ExecAgentServerRunningEvent.Handler> {

  public static final Type<ExecAgentServerRunningEvent.Handler> TYPE = new Type<>();

  private final String machineName;

  public ExecAgentServerRunningEvent(String machineName) {
    this.machineName = machineName;
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
    handler.onExecAgentServerRunning(this);
  }

  public interface Handler extends EventHandler {
    void onExecAgentServerRunning(ExecAgentServerRunningEvent event);
  }
}
