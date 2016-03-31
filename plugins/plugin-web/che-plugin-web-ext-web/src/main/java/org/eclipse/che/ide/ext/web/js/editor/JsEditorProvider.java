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
package org.eclipse.che.ide.ext.web.js.editor;

import com.google.inject.Inject;

import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.editor.EditorProvider;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.jseditor.client.defaulteditor.DefaultEditorProvider;
import org.eclipse.che.ide.jseditor.client.texteditor.ConfigurableTextEditor;

/**
 * {@link EditorProvider} for JavaScript files.
 *
 * @author Evgen Vidolob
 */
public class JsEditorProvider implements EditorProvider {
    private final DefaultEditorProvider         editorProvider;
    private final NotificationManager           notificationManager;
    private final JsEditorConfigurationProvider configurationProvider;

    @Inject
    public JsEditorProvider(final DefaultEditorProvider editorProvider,
                            NotificationManager notificationManager,
                            JsEditorConfigurationProvider configurationProvider) {
        this.editorProvider = editorProvider;
        this.notificationManager = notificationManager;
        this.configurationProvider = configurationProvider;
    }

    @Override
    public String getId() {
        return "codenvyJavaScriptEditor";
    }

    @Override
    public String getDescription() {
        return "Codenvy JavaScript Editor";
    }

    @Override
    public EditorPartPresenter getEditor() {
        ConfigurableTextEditor textEditor = editorProvider.getEditor();
        textEditor.initialize(configurationProvider.get(), notificationManager);
        return textEditor;
    }
}
