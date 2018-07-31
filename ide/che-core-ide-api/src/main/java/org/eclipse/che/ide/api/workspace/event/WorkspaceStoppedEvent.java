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

/** Fired when the current workspace goes into a stopped state. */
public class WorkspaceStoppedEvent extends GwtEvent<WorkspaceStoppedEvent.Handler> {

  public static final Type<WorkspaceStoppedEvent.Handler> TYPE = new Type<>();

  private final boolean error;
  private final String errorMessage;

  public WorkspaceStoppedEvent(boolean error, String errorMessage) {
    this.error = error;
    this.errorMessage = errorMessage;
  }

  public boolean isError() {
    return error;
  }

  /**
   * Returns an error message if workspace was stopped due to error.
   *
   * @return error message if workspace was stopped due to error or an empty string if workspace was
   *     stopped normally
   */
  public String getErrorMessage() {
    return errorMessage;
  }

  @Override
  public Type<WorkspaceStoppedEvent.Handler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(WorkspaceStoppedEvent.Handler handler) {
    handler.onWorkspaceStopped(this);
  }

  public interface Handler extends EventHandler {
    void onWorkspaceStopped(WorkspaceStoppedEvent event);
  }
}
