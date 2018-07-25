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
package org.eclipse.che.ide.processes;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

/**
 * Fires when machine output should be displayed.
 *
 * @author Vlad Zhukovskyi
 * @since 6.0.0
 */
public class DisplayMachineOutputEvent
    extends GwtEvent<DisplayMachineOutputEvent.DisplayMachineOutputHandler> {

  public interface DisplayMachineOutputHandler extends EventHandler {
    void onDisplayMachineOutput(DisplayMachineOutputEvent event);
  }

  public static final Type<DisplayMachineOutputEvent.DisplayMachineOutputHandler> TYPE =
      new Type<>();

  private final String machineName;

  public DisplayMachineOutputEvent(String machineName) {
    this.machineName = machineName;
  }

  public String getMachineName() {
    return machineName;
  }

  @Override
  public Type<DisplayMachineOutputHandler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(DisplayMachineOutputHandler handler) {
    handler.onDisplayMachineOutput(this);
  }
}
