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
package org.eclipse.che.ide.ext.java.client.action;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.Resources;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.filetypes.FileTypeRegistry;
import org.eclipse.che.ide.ext.java.client.JavaLocalizationConstant;
import org.eclipse.che.ide.ext.java.client.search.FindUsagesPresenter;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;

/**
 * Action invoked 'find usages' search for java elements
 *
 * @author Evgen Vidolob
 */
@Singleton
public class FindUsagesAction extends JavaEditorAction {

    private FindUsagesPresenter presenter;

    @Inject
    public FindUsagesAction(JavaLocalizationConstant constant,
                            EditorAgent editorAgent,
                            FindUsagesPresenter presenter,
                            Resources resources,
                            FileTypeRegistry fileTypeRegistry) {
        super(constant.actionFindUsagesTitle(),
              constant.actionFindUsagesDescription(),
              resources.find(),
              editorAgent,
              fileTypeRegistry);
        this.presenter = presenter;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        EditorPartPresenter editor = editorAgent.getActiveEditor();
        if (editor instanceof TextEditor) {
            presenter.findUsages((TextEditor)editor);
        } else {
            throw new IllegalStateException(getTemplatePresentation().getText() +
                                            " can be performed only on editors that implement TextEditor interface. Try to open this file in another editor.");
        }

    }
}
