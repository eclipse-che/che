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
