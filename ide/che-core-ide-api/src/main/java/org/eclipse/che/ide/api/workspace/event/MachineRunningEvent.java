/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
