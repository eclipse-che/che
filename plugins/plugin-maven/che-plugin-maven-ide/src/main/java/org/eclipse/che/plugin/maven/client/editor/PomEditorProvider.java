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
package org.eclipse.che.plugin.maven.client.editor;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.editor.defaulteditor.AbstractTextEditorProvider;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.ide.editor.orion.client.OrionEditorPresenter;

/**
 * Creates editor for pom.xml file
 *
 * @author Evgen Vidolob
 * @author Artem Zatsarynnyi
 */
@Singleton
public class PomEditorProvider extends AbstractTextEditorProvider {

    private final PomEditorConfigurationFactory configurationFactory;

    @Inject
    public PomEditorProvider(PomEditorConfigurationFactory configurationFactory) {
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
    public TextEditor getEditor() {
        TextEditor editor = super.getEditor();
        if (editor instanceof OrionEditorPresenter) {
            PomEditorConfiguration configuration = configurationFactory.create((OrionEditorPresenter)editor);
            editor.initialize(configuration);
        }

        return editor;
    }
}
