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

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.Widget;

import org.eclipse.che.ide.api.editor.EditorWithErrors;
import org.eclipse.che.ide.api.editor.codeassist.CompletionsSource;
import org.eclipse.che.ide.api.editor.keymap.Keymap;
import org.eclipse.che.ide.api.editor.text.TextPosition;
import org.eclipse.che.ide.api.editor.texteditor.EditorWidget;
import org.eclipse.che.ide.api.editor.texteditor.HasNotificationPanel;

/**
 * View interface for the text editors components.
 *
 * @author "Mickaël Leduque"
 */
public interface TextEditorPartView extends RequiresResize, IsWidget, HasNotificationPanel {

    /**
     * Invoke the code complete dialog.
     *
     * @param editorWidget
     *         the editor widget
     * @param completionsSource
     *         the completion source
     */
    void showCompletionProposals(EditorWidget editorWidget, CompletionsSource completionsSource);

    /**
     * Invoke the code complete dialog with default completion.
     *
     * @param editorWidget
     *         the editor widget
     */
    void showCompletionProposals(EditorWidget editorWidget);

    /**
     * Sets the view delegate.
     *
     * @param delegate
     *         the delegate
     */
    void setDelegate(Delegate delegate);

    /**
     * Sets the editor widget.
     *
     * @param editorWidget
     *         the widget
     */
    void setEditorWidget(EditorWidget editorWidget);

    /**
     * Display a placeholder in place of the editor widget.
     *
     * @param placeHolder
     *         the widget to display
     */
    void showPlaceHolder(Widget placeHolder);

    /**
     * Sets the initial state of the info panel.
     *
     * @param mode
     *         the file mode
     * @param keymap
     *         the current keymap
     * @param lineCount
     *         the number of lines
     * @param tabSize
     *         the tab size in this editor
     */
    void initInfoPanel(String mode, Keymap keymap, int lineCount, int tabSize);

    /**
     * Update the location displayed in the info panel.
     *
     * @param position
     *         the new position
     */
    void updateInfoPanelPosition(TextPosition position);

    /**
     * Update the values in the info panel for when the editor is not focused (i.e. show line count and not char part).
     *
     * @param lineCount
     *         the number of lines in the file
     */
    void updateInfoPanelUnfocused(int lineCount);

    /** Delegate interface for this view. */
    interface Delegate extends EditorWithErrors, RequiresResize {
        /** Reaction on loss of focus. */
        void editorLostFocus();

        /** Reaction when the editor gains focus. */
        void editorGotFocus();

        /** Reaction when the cursor position changes. */
        void editorCursorPositionChanged();
    }
}
