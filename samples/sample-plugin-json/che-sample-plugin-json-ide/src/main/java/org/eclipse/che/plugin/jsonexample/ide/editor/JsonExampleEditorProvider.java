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
package org.eclipse.che.plugin.jsonexample.ide.editor;

import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.editor.EditorProvider;
import org.eclipse.che.ide.api.editor.defaulteditor.AbstractTextEditorProvider;
import org.eclipse.che.ide.api.editor.defaulteditor.DefaultTextEditorProvider;
import org.eclipse.che.ide.api.editor.editorconfig.TextEditorConfiguration;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;

import javax.inject.Inject;

/**
 * The JSON Example specific {@link EditorProvider}.
 */
public class JsonExampleEditorProvider extends AbstractTextEditorProvider {

    private final JsonExampleEditorConfigurationFactory editorConfigurationFactory;

    /**
     * Constructor.
     *
     * @param editorConfigurationFactory
     *         the JSON Example Editor configuration factory
     */
    @Inject
    public JsonExampleEditorProvider(final JsonExampleEditorConfigurationFactory editorConfigurationFactory) {
        this.editorConfigurationFactory = editorConfigurationFactory;
    }

    @Override
    public String getId() {
        return "JsonExampleEditor";
    }

    @Override
    public String getDescription() {
        return "JSON Example Editor";
    }

    @Override
    public TextEditor getEditor() {
        TextEditor editor = super.getEditor();
        TextEditorConfiguration configuration = this.editorConfigurationFactory.create(editor);
        editor.initialize(configuration);
        return editor;
    }
}
