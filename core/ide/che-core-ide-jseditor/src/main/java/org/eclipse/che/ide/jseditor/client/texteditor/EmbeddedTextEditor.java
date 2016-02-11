/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.jseditor.client.texteditor;

import org.eclipse.che.ide.jseditor.client.editorconfig.EditorUpdateAction;
import org.eclipse.che.ide.jseditor.client.position.PositionConverter;
import org.eclipse.che.ide.texteditor.selection.CursorModelWithHandler;

public interface EmbeddedTextEditor extends TextEditor, ConfigurableTextEditor {

    /**
     * @return the text editor view implementation //todo need to introduce more simple way to use TextEditorPartView interface
     */
    @Deprecated
    EmbeddedTextEditorPartView getView();

    /** Calls all editor update actions for this editor. */
    void refreshEditor();

    /**
     *  Adds an editor update action for this editor.
     *  @param action the action to add
     */
    void addEditorUpdateAction(EditorUpdateAction action);

    /**
     * Returns the cursor model for the editor.
     * @return the cursor model
     */
    CursorModelWithHandler getCursorModel();

    /**
     * Returns a position converter relative to this editor (pixel coordinates <-> line char positions).
     *
     * @return a position converter
     */
    PositionConverter getPositionConverter();
}
