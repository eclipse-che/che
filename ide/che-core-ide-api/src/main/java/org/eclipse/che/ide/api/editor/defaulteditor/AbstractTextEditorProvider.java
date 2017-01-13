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
package org.eclipse.che.ide.api.editor.defaulteditor;

import com.google.inject.Inject;

import org.eclipse.che.ide.api.editor.EditorProvider;
import org.eclipse.che.ide.api.editor.editorconfig.AutoSaveTextEditorConfiguration;
import org.eclipse.che.ide.api.editor.editorconfig.TextEditorConfiguration;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.ide.util.loging.Log;

/**
 * This class provides an abstract implementation of the {@link EditorProvider}
 * interface to minimize the effort required to implement this interface.
 * <p>To implement an editor provider, the programmer needs only to extend this class and provide an
 * implementation for the {@link #getId()} and {@link #getDescription()} methods.
 * <p>The method {@link #getEditor()} returns {@link TextEditor}
 * that is initialized by configuration returned by {@link #getEditorConfiguration()} method.
 * <p>The method {@link #getEditorConfiguration()} returns {@link AutoSaveTextEditorConfiguration}
 * instance and may be overridden in order to provide another configuration for the editor
 * which is returned by {@link #getEditor()} method.
 *
 * @author Artem Zatsarynnyi
 */
public abstract class AbstractTextEditorProvider implements EditorProvider {

    @Inject
    private EditorBuilder editorBuilder;

    /** Returns configuration for initializing an editor returned by {@link #getEditor()} method. */
    protected TextEditorConfiguration getEditorConfiguration() {
        return new AutoSaveTextEditorConfiguration();
    }

    @Override
    public TextEditor getEditor() {
        if (editorBuilder == null) {
            Log.debug(AbstractTextEditorProvider.class, "No builder registered for default editor type - giving up.");
            return null;
        }

        final TextEditor editor = editorBuilder.buildEditor();
        editor.initialize(getEditorConfiguration());
        return editor;
    }
}
