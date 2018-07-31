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
package org.eclipse.che.ide.editor.orion.client;

import org.eclipse.che.ide.api.editor.document.Document;
import org.eclipse.che.ide.api.editor.events.CursorActivityEvent;
import org.eclipse.che.ide.api.editor.events.CursorActivityHandler;
import org.eclipse.che.ide.api.editor.text.Position;
import org.eclipse.che.ide.api.editor.text.TextPosition;
import org.eclipse.che.ide.api.editor.texteditor.CursorModelWithHandler;
import org.eclipse.che.ide.util.ListenerManager;
import org.eclipse.che.ide.util.ListenerManager.Dispatcher;
import org.eclipse.che.ide.util.ListenerRegistrar.Remover;

/**
 * {@link CursorModelWithHandler} implementation for the text editors.
 *
 * @author "Mickaël Leduque"
 */
class OrionCursorModel implements CursorModelWithHandler, CursorActivityHandler {

  private final Document document;
  private final ListenerManager<CursorHandler> cursorHandlerManager = ListenerManager.create();

  public OrionCursorModel(final Document document) {
    this.document = document;
    this.document.addCursorHandler(this);
  }

  @Override
  public void setCursorPosition(int offset) {
    TextPosition position = document.getPositionFromIndex(offset);
    document.setCursorPosition(position);
  }

  @Override
  public Position getCursorPosition() {
    TextPosition position = document.getCursorPosition();
    int offset = document.getIndexFromPosition(position);
    return new Position(offset);
  }

  @Override
  public Remover addCursorHandler(CursorHandler handler) {
    return this.cursorHandlerManager.add(handler);
  }

  private void dispatchCursorChange(final boolean isExplicitChange) {
    final TextPosition position = this.document.getCursorPosition();

    cursorHandlerManager.dispatch(
        new Dispatcher<CursorHandler>() {
          @Override
          public void dispatch(CursorHandler listener) {
            listener.onCursorChange(position.getLine(), position.getCharacter(), isExplicitChange);
          }
        });
  }

  @Override
  public void onCursorActivity(final CursorActivityEvent event) {
    dispatchCursorChange(true);
  }
}
