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
package org.eclipse.che.ide.command.toolbar.processes;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

/** Event for activating particular process's output panel. */
public class ActivateProcessOutputEvent extends GwtEvent<ActivateProcessOutputEvent.Handler> {

  public static final Type<ActivateProcessOutputEvent.Handler> TYPE = new Type<>();

  private final int pid;

  /** Creates new event with the given PID. */
  public ActivateProcessOutputEvent(int pid) {
    this.pid = pid;
  }

  public int getPid() {
    return pid;
  }

  @Override
  public Type<ActivateProcessOutputEvent.Handler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onActivateProcessOutput(this);
  }

  public interface Handler extends EventHandler {

    /** Called when activating process's output is requested. */
    void onActivateProcessOutput(ActivateProcessOutputEvent event);
  }
}
