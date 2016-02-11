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
package org.eclipse.che.ide.ext.web.html.editor;

import com.google.inject.Inject;

import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.editor.EditorProvider;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.jseditor.client.defaulteditor.DefaultEditorProvider;
import org.eclipse.che.ide.jseditor.client.texteditor.ConfigurableTextEditor;

/**
 * {@link EditorProvider} for HTML files.
 */
public class HtmlEditorProvider implements EditorProvider {
    private final DefaultEditorProvider           editorProvider;
    private final NotificationManager             notificationManager;
    private final HTMLEditorConfigurationProvider htmlEditorConfigurationProvider;

    @Inject
    public HtmlEditorProvider(final DefaultEditorProvider editorProvider,
                              final NotificationManager notificationManager,
                              final HTMLEditorConfigurationProvider htmlEditorConfigurationProvider) {
        this.editorProvider = editorProvider;
        this.notificationManager = notificationManager;
        this.htmlEditorConfigurationProvider = htmlEditorConfigurationProvider;
    }

    @Override
    public String getId() {
        return "codenvyHTMLEditor";
    }

    @Override
    public String getDescription() {
        return "Codenvy HTML Editor";
    }

    @Override
    public EditorPartPresenter getEditor() {
        ConfigurableTextEditor textEditor = editorProvider.getEditor();
        HtmlEditorConfiguration configuration = this.htmlEditorConfigurationProvider.get();
        textEditor.initialize(configuration, notificationManager);
        return textEditor;
    }
}
