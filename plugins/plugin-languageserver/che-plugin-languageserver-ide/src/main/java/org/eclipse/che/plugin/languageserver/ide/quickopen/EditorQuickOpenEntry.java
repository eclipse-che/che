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
package org.eclipse.che.plugin.languageserver.ide.quickopen;

import org.eclipse.che.ide.api.editor.text.TextRange;
import org.eclipse.che.plugin.languageserver.ide.util.OpenFileInEditorHelper;

/**
 * Quick open entry that can be opened in editor.
 *
 * @author Evgen Vidolob
 */
public class EditorQuickOpenEntry extends QuickOpenEntry {


    private final OpenFileInEditorHelper editorHelper;

    public EditorQuickOpenEntry(OpenFileInEditorHelper editorHelper) {
        this.editorHelper = editorHelper;
    }

    protected String getFilePath() {
        return null;
    }

    protected TextRange getTextRange() {
        return null;
    }

    @Override
    public boolean run(Mode mode) {
        if (mode == Mode.OPEN) {
            String filePath = getFilePath();
            editorHelper.openFile(filePath, getTextRange());
            return true;
        }
        return false;
    }
}
