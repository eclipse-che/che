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

import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.machine.CommandPropertyValueProvider;

/**
 * Base macro provider which belongs to the current opened editor. Provides easy access to the opened virtual file
 * to allow fetch necessary information to use in custom commands, preview urls, etc.
 *
 * @author Vlad Zhukovskyi
 * @see EditorAgent
 * @see CommandPropertyValueProvider
 * @see EditorCurrentFileNameProvider
 * @see EditorCurrentFilePathProvider
 * @see EditorCurrentFileRelativePathProvider
 * @see EditorCurrentProjectNameProvider
 * @see EditorCurrentProjectTypeProvider
 * @since 4.7.0
 */
@Beta
public abstract class AbstractEditorMacroProvider implements CommandPropertyValueProvider {

    private EditorAgent editorAgent;

    public AbstractEditorMacroProvider(EditorAgent editorAgent) {
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
