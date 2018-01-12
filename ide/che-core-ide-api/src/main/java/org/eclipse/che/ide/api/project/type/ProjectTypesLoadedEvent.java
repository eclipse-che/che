/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.api.project.type;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

/** Fired when {@link ProjectTypeRegistry} is initialized. */
public class ProjectTypesLoadedEvent extends GwtEvent<ProjectTypesLoadedEvent.Handler> {

  public static final Type<ProjectTypesLoadedEvent.Handler> TYPE = new Type<>();

  @Override
  public Type<Handler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onProjectTypesInitialized(this);
  }

  public interface Handler extends EventHandler {
    void onProjectTypesInitialized(ProjectTypesLoadedEvent event);
  }
}
