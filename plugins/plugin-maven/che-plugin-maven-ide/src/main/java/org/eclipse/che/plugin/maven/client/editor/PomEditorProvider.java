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
package org.eclipse.che.plugin.maven.client.editor;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.editor.EditorProvider;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.jseditor.client.defaulteditor.DefaultEditorProvider;
import org.eclipse.che.ide.jseditor.client.texteditor.ConfigurableTextEditor;
import org.eclipse.che.ide.jseditor.client.texteditor.EmbeddedTextEditorPresenter;

/**
 * Creates editor for pom.xml file
 *
 * @author Evgen Vidolob
 */
@Singleton
public class PomEditorProvider implements EditorProvider {


    private final DefaultEditorProvider         editorProvider;
    private final NotificationManager           notificationManager;
    private final PomEditorConfigurationFactory configurationFactory;

    @Inject
    public PomEditorProvider(DefaultEditorProvider editorProvider,
                             NotificationManager notificationManager,
                             PomEditorConfigurationFactory configurationFactory) {
        this.editorProvider = editorProvider;
        this.notificationManager = notificationManager;
        this.configurationFactory = configurationFactory;
    }

    @Override
    public String getId() {
        return "PomEditor";
    }

    @Override
    public String getDescription() {
        return "Pom Editor";
    }

    @Override
    public EditorPartPresenter getEditor() {
        ConfigurableTextEditor editor = editorProvider.getEditor();
        PomEditorConfiguration configuration = configurationFactory.create((EmbeddedTextEditorPresenter<?>)editor);
        editor.initialize(configuration, notificationManager);
        return editor;
    }
}
