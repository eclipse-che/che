/*******************************************************************************
 * Copyright (c) 2012-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package com.codenvy.ide.tutorial.editor.editor;

import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.editor.EditorProvider;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.jseditor.client.defaulteditor.DefaultEditorProvider;
import org.eclipse.che.ide.jseditor.client.texteditor.ConfigurableTextEditor;
import com.google.inject.Inject;

/**
 * EditorProvider for Groovy file type.
 * 
 * @author <a href="mailto:aplotnikov@codenvy.com">Andrey Plotnikov</a>
 */
public class GroovyEditorProvider implements EditorProvider {
    private final DefaultEditorProvider editorProvider;
    private final NotificationManager notificationManager;

    @Inject
    public GroovyEditorProvider(final DefaultEditorProvider editorProvider,
                                final NotificationManager notificationManager) {
        super();
        this.editorProvider = editorProvider;
        this.notificationManager = notificationManager;
    }

    @Override
    public String getId() {
        return "codenvyGroovyEditor";
    }

    @Override
    public String getDescription() {
        return "Codenvy Groovy Editor";
    }

    /** {@inheritDoc} */
    @Override
    public EditorPartPresenter getEditor() {
        ConfigurableTextEditor textEditor = editorProvider.getEditor();
        textEditor.initialize(new GroovyEditorConfiguration(), notificationManager);
        return textEditor;
    }
}
