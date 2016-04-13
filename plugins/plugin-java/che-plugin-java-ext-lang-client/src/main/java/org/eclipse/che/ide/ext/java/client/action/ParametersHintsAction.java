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
package org.eclipse.che.ide.ext.java.client.action;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.filetypes.FileTypeRegistry;
import org.eclipse.che.ide.ext.java.client.JavaLocalizationConstant;
import org.eclipse.che.ide.ext.java.client.navigation.paraminfo.ParametersHintsPresenter;
import org.eclipse.che.ide.jseditor.client.texteditor.EmbeddedTextEditorPresenter;

/**
 * @author Dmitry Shnurenko
 */
@Singleton
public class ParametersHintsAction extends JavaEditorAction {

    private final ParametersHintsPresenter parametersHintsPresenter;

    @Inject
    public ParametersHintsAction(EditorAgent editorAgent,
                                 ParametersHintsPresenter parametersHintsPresenter,
                                 FileTypeRegistry fileTypeRegistry,
                                 JavaLocalizationConstant locale) {
        super(locale.parameterInfo(), locale.parameterInfoDescription(), editorAgent, fileTypeRegistry);
        this.editorAgent = editorAgent;
        this.parametersHintsPresenter = parametersHintsPresenter;
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        EditorPartPresenter activeEditor = editorAgent.getActiveEditor();
        if (!(activeEditor instanceof EmbeddedTextEditorPresenter)) {
            return;
        }

        parametersHintsPresenter.show((EmbeddedTextEditorPresenter)activeEditor);
    }
}
