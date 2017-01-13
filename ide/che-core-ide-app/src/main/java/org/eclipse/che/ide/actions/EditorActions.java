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
package org.eclipse.che.ide.actions;

/**
 * Contains IDs for editor's actions
 *
 * @author Roman Nikitenko
 */
public final class EditorActions {
    public static final String CLOSE                   = "closeEditor";
    public static final String CLOSE_ALL               = "closeAllEditors";
    public static final String CLOSE_ALL_EXCEPT_PINNED = "closeAllEditorExceptPinned";
    public static final String CLOSE_OTHER             = "closeOtherEditorExceptCurrent";
    public static final String REOPEN_CLOSED           = "reopenClosedEditorTab";
    public static final String PIN_TAB                 = "pinEditorTab";
    public static final String SPLIT_HORIZONTALLY      = "splitHorizontally";
    public static final String SPLIT_VERTICALLY        = "splitVertically";
}
