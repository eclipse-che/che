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
package org.eclipse.che.ide.jseditor.client.defaulteditor;

import com.google.inject.Inject;

import org.eclipse.che.ide.api.editor.EditorProvider;
import org.eclipse.che.ide.jseditor.client.JsEditorExtension;
import org.eclipse.che.ide.jseditor.client.editorconfig.AutoSaveTextEditorConfiguration;
import org.eclipse.che.ide.jseditor.client.editorconfig.TextEditorConfiguration;
import org.eclipse.che.ide.jseditor.client.editortype.EditorType;
import org.eclipse.che.ide.jseditor.client.editortype.EditorTypeRegistry;
import org.eclipse.che.ide.jseditor.client.texteditor.ConfigurableTextEditor;
import org.eclipse.che.ide.util.loging.Log;

import javax.inject.Named;

/**
 * This class provides an abstract implementation of the {@link EditorProvider}
 * interface to minimize the effort required to implement this interface.
 * <p>To implement an editor provider, the programmer needs only to extend this class and provide an
 * implementation for the {@link #getId()} and {@link #getDescription()} methods.
 * <p>The method {@link #getEditor()} returns {@link ConfigurableTextEditor}
 * that is initialized by configuration returned by {@link #getEditorConfiguration()} method.
 * <p>The method {@link #getEditorConfiguration()} returns {@link AutoSaveTextEditorConfiguration}
 * instance and may be overridden in order to provide another configuration for the editor
 * which is returned by {@link #getEditor()} method.
 *
 * @author Artem Zatsarynnyi
 */
public abstract class AbstractEditorProvider implements EditorProvider {

    @Inject
    private EditorTypeRegistry editorTypeRegistry;

    @Inject
    @Named(JsEditorExtension.DEFAULT_EDITOR_TYPE_INSTANCE)
    private EditorType defaultEditorType;

    /** Returns configuration for initializing an editor returned by {@link #getEditor()} method. */
    protected TextEditorConfiguration getEditorConfiguration() {
        return new AutoSaveTextEditorConfiguration();
    }

    @Override
    public ConfigurableTextEditor getEditor() {
        EditorBuilder builder = editorTypeRegistry.getRegisteredBuilder(defaultEditorType);
        if (builder == null) {
            Log.debug(AbstractEditorProvider.class, "No builder registered for default editor type - giving up.");
            return null;
        }

        ConfigurableTextEditor editor = builder.buildEditor();
        editor.initialize(getEditorConfiguration());
        return editor;
    }
}
