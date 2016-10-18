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
package org.eclipse.che.ide.editor.macro;

import com.google.common.annotations.Beta;

import org.eclipse.che.ide.api.macro.Macro;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;

/**
 * Base macro provider which belongs to the current opened editor. Provides easy access to the opened virtual file
 * to allow fetch necessary information to use in custom commands, preview urls, etc.
 *
 * @author Vlad Zhukovskyi
 * @see EditorAgent
 * @see Macro
 * @see EditorCurrentFileNameMacro
 * @see EditorCurrentFilePathMacro
 * @see EditorCurrentFileRelativePathMacro
 * @see EditorCurrentProjectNameMacro
 * @see EditorCurrentProjectTypeMacro
 * @since 4.7.0
 */
@Beta
public abstract class AbstractEditorMacro implements Macro {

    private EditorAgent editorAgent;

    public AbstractEditorMacro(EditorAgent editorAgent) {
        this.editorAgent = editorAgent;
    }

    /**
     * Returns the active editor or null if no active editor was found.
     *
     * @return active editor or {@code null}
     */
    public EditorPartPresenter getActiveEditor() {
        return editorAgent.getActiveEditor();
    }

}
