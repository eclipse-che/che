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
package org.eclipse.che.ide.api.workspace.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import org.eclipse.che.ide.api.workspace.model.MachineImpl;

/** Fired when some machine goes into a running state. */
public class MachineRunningEvent extends GwtEvent<MachineRunningEvent.Handler> {

  public static final Type<MachineRunningEvent.Handler> TYPE = new Type<>();

  private final MachineImpl machine;

  public MachineRunningEvent(MachineImpl machine) {
    this.machine = machine;
  }

  /** Returns the running machine. */
  public MachineImpl getMachine() {
    return machine;
  }

  @Override
  public Type<Handler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onMachineRunning(this);
  }

  public interface Handler extends EventHandler {
    void onMachineRunning(MachineRunningEvent event);
  }
}
