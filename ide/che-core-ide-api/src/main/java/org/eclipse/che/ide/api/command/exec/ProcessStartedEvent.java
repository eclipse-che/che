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
package org.eclipse.che.ide.api.command.exec;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

public class ProcessStartedEvent extends GwtEvent<ProcessStartedEvent.Handler> {

  public static final Type<ProcessStartedEvent.Handler> TYPE = new Type<>();

  private final int processID;
  private final String machineName;

  public ProcessStartedEvent(int processID, String machineName) {
    this.processID = processID;
    this.machineName = machineName;
  }

  public int getProcessID() {
    return processID;
  }

  public String getMachineName() {
    return machineName;
  }

  @Override
  public Type<ProcessStartedEvent.Handler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onProcessStarted(this);
  }

  public interface Handler extends EventHandler {

    void onProcessStarted(ProcessStartedEvent event);
  }
}
