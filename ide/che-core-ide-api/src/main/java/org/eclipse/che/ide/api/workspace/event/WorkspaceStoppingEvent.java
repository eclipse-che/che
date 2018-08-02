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

/** Fired when the current workspace goes into a stopping state. */
public class WorkspaceStoppingEvent extends GwtEvent<WorkspaceStoppingEvent.Handler> {

  public static final Type<WorkspaceStoppingEvent.Handler> TYPE = new Type<>();

  @Override
  public Type<WorkspaceStoppingEvent.Handler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(WorkspaceStoppingEvent.Handler handler) {
    handler.onWorkspaceStopping(this);
  }

  public interface Handler extends EventHandler {
    void onWorkspaceStopping(WorkspaceStoppingEvent event);
  }
}
