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
package org.eclipse.che.plugin.jsonexample.ide;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import org.eclipse.che.ide.api.editor.EditorRegistry;
import org.eclipse.che.ide.api.extension.Extension;
import org.eclipse.che.ide.api.filetypes.FileType;
import org.eclipse.che.plugin.jsonexample.ide.editor.JsonExampleEditorProvider;

/**
 * The editor extension for the JSON Example
 */
@Extension(title = "JSON Example Editor")
public class JsonExampleEditorExtension {

    /**
     * Constructor.
     *
     * @param editorRegistry
     *         the {@link EditorRegistry} for registering our editor
     * @param jsonFile
     *         the {@link FileType} that is associated with our editor
     * @param editorProvider
     *         the editor provider
     */
    @Inject
    public JsonExampleEditorExtension(final EditorRegistry editorRegistry,
                                      final @Named("JsonFileType") FileType jsonFile,
                                      final JsonExampleEditorProvider editorProvider) {
        editorRegistry.registerDefaultEditor(jsonFile, editorProvider);
    }
}
