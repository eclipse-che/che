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
package org.eclipse.che.ide.api.command.exec;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

/** @author Dmitry Shnurenko */
public class ProcessFinishedEvent extends GwtEvent<ProcessFinishedEvent.Handler> {

  public interface Handler extends EventHandler {

    /** Implement this method to handle ProcessFinishedEvent. */
    void onProcessFinished(ProcessFinishedEvent event);
  }

  public static final Type<ProcessFinishedEvent.Handler> TYPE = new Type<>();

  private final int processID;
  private final String machineName;

  public ProcessFinishedEvent(int processID, String machineName) {
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
  public Type<ProcessFinishedEvent.Handler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onProcessFinished(this);
  }
}
