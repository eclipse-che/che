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

/** Fired when some machine goes into a starting state. */
public class MachineStartingEvent extends GwtEvent<MachineStartingEvent.Handler> {

  public static final Type<MachineStartingEvent.Handler> TYPE = new Type<>();

  private final MachineImpl machine;

  public MachineStartingEvent(MachineImpl machine) {
    this.machine = machine;
  }

  /** Returns the starting machine. */
  public MachineImpl getMachine() {
    return machine;
  }

  @Override
  public Type<Handler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onMachineStarting(this);
  }

  public interface Handler extends EventHandler {
    void onMachineStarting(MachineStartingEvent event);
  }
}
