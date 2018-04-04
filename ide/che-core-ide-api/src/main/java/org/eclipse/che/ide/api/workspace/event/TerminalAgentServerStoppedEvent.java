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
package org.eclipse.che.ide.api.workspace.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

/**
 * Fired when terminal-agent server in some machine goes into a stopped state. Allows to avoid
 * checking server's name unlike {@link ServerStoppedEvent}.
 *
 * @see ServerStoppedEvent
 */
public class TerminalAgentServerStoppedEvent
    extends GwtEvent<TerminalAgentServerStoppedEvent.Handler> {

  public static final Type<TerminalAgentServerStoppedEvent.Handler> TYPE = new Type<>();

  private final String machineName;

  public TerminalAgentServerStoppedEvent(String machineName) {
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
    handler.onTerminalAgentServerStopped(this);
  }

  public interface Handler extends EventHandler {
    void onTerminalAgentServerStopped(TerminalAgentServerStoppedEvent event);
  }
}
