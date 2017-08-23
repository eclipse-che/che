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
package org.eclipse.che.ide.api.editor.events;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

/**
 * Event fired when editor's settings has been changed.
 *
 * @author Roman Nikitenko
 */
public class EditorSettingsChangedEvent
    extends GwtEvent<EditorSettingsChangedEvent.EditorSettingsChangedHandler> {

  /** Handles Editor Settings Changed Event */
  public interface EditorSettingsChangedHandler extends EventHandler {
    /**
     * Perform actions when editor's settings has been changed.
     *
     * @param event editor's settings changed event
     */
    void onEditorSettingsChanged(EditorSettingsChangedEvent event);
  }

  public static final Type<EditorSettingsChangedHandler> TYPE = new Type<>();

  @Override
  public Type<EditorSettingsChangedHandler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(EditorSettingsChangedHandler handler) {
    handler.onEditorSettingsChanged(this);
  }
}
