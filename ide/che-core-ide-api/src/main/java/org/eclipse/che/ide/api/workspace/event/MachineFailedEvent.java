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
import org.eclipse.che.ide.api.workspace.model.MachineImpl;

/** Fired when some machine goes into a failed state. */
public class MachineFailedEvent extends GwtEvent<MachineFailedEvent.Handler> {

  public static final Type<MachineFailedEvent.Handler> TYPE = new Type<>();

  private final MachineImpl machine;
  private final String error;

  public MachineFailedEvent(MachineImpl machine, String error) {
    this.machine = machine;
    this.error = error;
  }

  /** Returns the failed machine. */
  public MachineImpl getMachine() {
    return machine;
  }

  /** Returns the error message describes the reason of fail. */
  public String getError() {
    return error;
  }

  @Override
  public Type<Handler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onMachineFailed(this);
  }

  public interface Handler extends EventHandler {
    void onMachineFailed(MachineFailedEvent event);
  }
}
