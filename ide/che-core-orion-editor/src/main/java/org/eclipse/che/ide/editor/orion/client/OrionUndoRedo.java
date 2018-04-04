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
package org.eclipse.che.ide.editor.orion.client;

import com.google.gwt.core.client.JavaScriptObject;
import org.eclipse.che.ide.api.editor.texteditor.HandlesUndoRedo;
import org.eclipse.che.ide.editor.orion.client.jso.OrionUndoStackOverlay;

/** Undo/redo handler for orion editors. */
class OrionUndoRedo implements HandlesUndoRedo {

  /** The document. */
  private final OrionUndoStackOverlay undoStack;

  private UndoRedoOperationsListener undoRedoOperationsListener;

  public OrionUndoRedo(final OrionUndoStackOverlay undoStack) {
    this.undoStack = undoStack;
  }

  @Override
  public boolean redoable() {
    return this.undoStack.canRedo();
  }

  @Override
  public boolean undoable() {
    return this.undoStack.canUndo();
  }

  @Override
  public void redo() {
    this.undoStack.redo();

    if (undoRedoOperationsListener != null) {
      undoRedoOperationsListener.onOperationExecuted();
    }
  }

  @Override
  public void undo() {
    this.undoStack.undo();

    if (undoRedoOperationsListener != null) {
      undoRedoOperationsListener.onOperationExecuted();
    }
  }

  @Override
  public void beginCompoundChange() {
    this.undoStack.startCompoundChange(JavaScriptObject.createObject());
  }

  @Override
  public void endCompoundChange() {
    this.undoStack.endCompoundChange();
  }

  @Override
  public void addUndoRedoOperationsListener(UndoRedoOperationsListener listener) {
    undoRedoOperationsListener = listener;
  }
}
