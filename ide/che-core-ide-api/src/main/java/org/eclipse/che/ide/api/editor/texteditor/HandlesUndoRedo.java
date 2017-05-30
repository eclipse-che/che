/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.api.editor.texteditor;

/**
 * Interface for an editor that allow undo/redo operations.
 */
public interface HandlesUndoRedo {

    /**
     * Returns whether at least one text change can be repeated. A text change
     * can be repeated only if it was executed and rolled back.
     *
     * @return <code>true</code> if at least on text change can be repeated
     */
    boolean redoable();

    /**
     * Returns whether at least one text change can be rolled back.
     *
     * @return <code>true</code> if at least one text change can be rolled back
     */
    boolean undoable();

    /** Repeats the most recently rolled back text change. */
    void redo();

    /** Rolls back the most recently executed text change. */
    void undo();

    /**
     * Signals the UndoRedo that all subsequent changes until
     * <code>endCompoundChange</code> is called are to be undone in one piece.
     */
    void beginCompoundChange();

    /**
     * Signals the UndoRedo that the sequence of changes which started with
     * <code>beginCompoundChange</code> has been finished. All subsequent changes
     * are considered to be individually undo-able.
     */
    void endCompoundChange();
}
