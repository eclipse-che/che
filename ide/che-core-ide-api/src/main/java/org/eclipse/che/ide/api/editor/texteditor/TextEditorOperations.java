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
 * Defines the target for text operations. The editor informs
 * the clients about the ability of the target to perform the specified
 * operation at the current point in time.
 *
 * @author <a href="mailto:evidolob@exoplatform.com">Evgen Vidolob</a>
 * @version $Id:
 */
public interface TextEditorOperations {

    /** Text operation code for undoing the last edit command. */
    int UNDO = 1;

    /** Text operation code for redoing the last undone edit command. */
    int REDO = 2;

    /** Text operation code for moving the selected text to the clipboard. */
    int CUT = 3;

    /** Text operation code for copying the selected text to the clipboard. */
    int COPY = 4;

    /**
     * Text operation code for inserting the clipboard content at the
     * current position.
     */
    int PASTE = 5;

    /**
     * Text operation code for deleting the selected text or if selection
     * is empty the character  at the right of the current position.
     */
    int DELETE = 6;

    /** Text operation code for selecting the complete text. */
    int SELECT_ALL = 7;

    /** Text operation code for shifting the selected text block to the right. */
    int SHIFT_RIGHT = 8;

    /** Text operation code for shifting the selected text block to the left. */
    int SHIFT_LEFT = 9;

    /** Text operation code for printing the complete text. */
    int PRINT = 10;

    /** Text operation code for prefixing the selected text block. */
    int PREFIX = 11;

    /** Text operation code for removing the prefix from the selected text block. */
    int STRIP_PREFIX = 12;

    /**
     * Text operation code for requesting content assist to show completion
     * proposals for the current insert position.
     */
    int CODEASSIST_PROPOSALS = 13;

    /**
     * Text operation code for formatting the selected text or complete document
     * of this viewer if the selection is empty.
     */
    int FORMAT = 14;

    /**
     * Text operation code for requesting quick assist. This will normally
     * show quick assist and quick fix proposals for the current position.
     */
    int QUICK_ASSIST = 15;

    /**
     * Text operation code for requesting signature help for current cursor position.
     */
    int SIGNATURE_HELP = 16;
}
