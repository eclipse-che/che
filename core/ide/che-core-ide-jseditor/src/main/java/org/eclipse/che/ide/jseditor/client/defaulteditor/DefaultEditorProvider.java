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

import javax.inject.Named;

import org.eclipse.che.ide.api.editor.EditorProvider;
import org.eclipse.che.ide.jseditor.client.JsEditorExtension;
import org.eclipse.che.ide.jseditor.client.editortype.EditorType;
import org.eclipse.che.ide.jseditor.client.editortype.EditorTypeRegistry;
import org.eclipse.che.ide.jseditor.client.prefmodel.DefaultEditorTypePrefReader;
import org.eclipse.che.ide.jseditor.client.texteditor.ConfigurableTextEditor;
import org.eclipse.che.ide.util.loging.Log;
import com.google.inject.Inject;


public class DefaultEditorProvider implements EditorProvider {

    private final DefaultEditorTypePrefReader defaultEditorTypePrefReader;
    private final EditorTypeRegistry          editorTypeRegistry;
    private final EditorType                  defaultEditorType;

    @Inject
    public DefaultEditorProvider(final DefaultEditorTypePrefReader defaultEditorTypePrefReader,
                                 final EditorTypeRegistry editorTypeRegistry,
                                 final @Named(JsEditorExtension.DEFAULT_EDITOR_TYPE_INSTANCE) EditorType defaultEditorType) {
        this.defaultEditorTypePrefReader = defaultEditorTypePrefReader;
        this.editorTypeRegistry = editorTypeRegistry;
        this.defaultEditorType = defaultEditorType;
    }

    @Override
    public String getId() {
        return "codenvyDefaultEditor";
    }

    @Override
    public String getDescription() {
        return "Codenvy Default Editor";
    }

    @Override
    public ConfigurableTextEditor getEditor() {
        final EditorType editorType = this.defaultEditorTypePrefReader.readPref();
        Log.debug(DefaultEditorProvider.class, "Editor type used: " + editorType);
        EditorBuilder provider = this.editorTypeRegistry.getRegisteredBuilder(editorType);
        if (provider == null) {
            Log.debug(DefaultEditorProvider.class, "No builder registered for editor type " + editorType
                                                   + " - attempt to fallback to " + defaultEditorType);
            provider = this.editorTypeRegistry.getRegisteredBuilder(defaultEditorType);
            if (provider == null) {
                Log.debug(DefaultEditorProvider.class, "No builder registered for default editor type - giving up.");
                return null;
            }
        }
        return provider.buildEditor();
    }
}
