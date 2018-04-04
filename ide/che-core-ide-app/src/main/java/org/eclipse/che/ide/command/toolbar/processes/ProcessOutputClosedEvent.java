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

/** Event fired when process's output panel has been closed. */
public class ProcessOutputClosedEvent extends GwtEvent<ProcessOutputClosedEvent.Handler> {

  public static final Type<ProcessOutputClosedEvent.Handler> TYPE = new Type<>();

  private final int pid;

  /** Creates new event with the given PID. */
  public ProcessOutputClosedEvent(int pid) {
    this.pid = pid;
  }

  /** PID of the associated process. */
  public int getPid() {
    return pid;
  }

  @Override
  public Type<ProcessOutputClosedEvent.Handler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onProcessOutputClosed(this);
  }

  public interface Handler extends EventHandler {

    /** Called when process's output panel has been closed. */
    void onProcessOutputClosed(ProcessOutputClosedEvent event);
  }
}
