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
package org.eclipse.che.plugin.cpp.ide.editor;

import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.editor.EditorProvider;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.jseditor.client.defaulteditor.DefaultEditorProvider;
import org.eclipse.che.ide.jseditor.client.editorconfig.AutoSaveTextEditorConfiguration;
import org.eclipse.che.ide.jseditor.client.texteditor.EmbeddedTextEditorPresenter;

import javax.inject.Inject;


/**
 * EditorProvider that provides a text editor configured for C source files.
 *
 * @author Vitalii Parfonov
 */
public class CEditorProvider implements EditorProvider {

    private final DefaultEditorProvider editorProvider;
    private final NotificationManager   notificationManager;


    @Inject
    public CEditorProvider(final DefaultEditorProvider editorProvider,
                           final NotificationManager notificationManager) {
        this.editorProvider = editorProvider;
        this.notificationManager = notificationManager;
    }

    @Override
    public String getId() {
        return "CEditor";
    }

    @Override
    public String getDescription() {
        return "C Editor";
    }

    @Override
    public EditorPartPresenter getEditor() {
        final EditorPartPresenter textEditor = editorProvider.getEditor();
        if (textEditor instanceof EmbeddedTextEditorPresenter) {
            final EmbeddedTextEditorPresenter<?> editor = (EmbeddedTextEditorPresenter<?>)textEditor;
            editor.initialize(new AutoSaveTextEditorConfiguration(), notificationManager);
        }
        return textEditor;
    }

}
