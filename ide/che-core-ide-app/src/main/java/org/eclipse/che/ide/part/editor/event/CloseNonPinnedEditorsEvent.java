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
package org.eclipse.che.ide.part.editor.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import org.eclipse.che.ide.api.parts.EditorTab;

/**
 * Close non pinned editor tabs event.
 *
 * @author Vlad Zhukovskiy
 */
public class CloseNonPinnedEditorsEvent
    extends GwtEvent<CloseNonPinnedEditorsEvent.CloseNonPinnedEditorsHandler> {

  public interface CloseNonPinnedEditorsHandler extends EventHandler {
    void onCloseNonPinnedEditors(CloseNonPinnedEditorsEvent event);
  }

  private static Type<CloseNonPinnedEditorsHandler> TYPE;

  public static Type<CloseNonPinnedEditorsHandler> getType() {
    if (TYPE == null) {
      TYPE = new Type<>();
    }
    return TYPE;
  }

  private final EditorTab editorTab;

  public CloseNonPinnedEditorsEvent(EditorTab editorTab) {
    this.editorTab = editorTab;
  }

  public EditorTab getEditorTab() {
    return editorTab;
  }

  /** {@inheritDoc} */
  @Override
  public Type<CloseNonPinnedEditorsHandler> getAssociatedType() {
    return getType();
  }

  /** {@inheritDoc} */
  @Override
  protected void dispatch(CloseNonPinnedEditorsHandler handler) {
    handler.onCloseNonPinnedEditors(this);
  }
}
