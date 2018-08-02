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
package org.eclipse.che.ide.workspace.events;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import org.eclipse.che.api.core.model.workspace.runtime.MachineStatus;

/** Fired when state of machine has been changed. */
public class MachineStatusChangedEvent extends GwtEvent<MachineStatusChangedEvent.Handler> {

  public static final Type<MachineStatusChangedEvent.Handler> TYPE = new Type<>();

  private String machineName;

  private MachineStatus status;

  public MachineStatusChangedEvent(String machineName, MachineStatus status) {
    this.machineName = machineName;
    this.status = status;
  }

  public String getMachineName() {
    return machineName;
  }

  public MachineStatus getStatus() {
    return status;
  }

  @Override
  public Type<Handler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onMachineStatusChanged(this);
  }

  public interface Handler extends EventHandler {
    void onMachineStatusChanged(MachineStatusChangedEvent event);
  }
}
