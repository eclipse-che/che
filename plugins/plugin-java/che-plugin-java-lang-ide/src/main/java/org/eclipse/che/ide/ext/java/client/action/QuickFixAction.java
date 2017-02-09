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

import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.editor.texteditor.HandlesTextOperations;
import org.eclipse.che.ide.api.editor.texteditor.TextEditorOperations;
import org.eclipse.che.ide.api.filetypes.FileTypeRegistry;
import org.eclipse.che.ide.ext.java.client.JavaLocalizationConstant;

/**
 * Action to show Quick fix in editor.
 *
 * @author Igor Vinokur
 */
@Singleton
public class QuickFixAction extends JavaEditorAction {

    @Inject
    public QuickFixAction(JavaLocalizationConstant locale,
                          EditorAgent editorAgent,
                          FileTypeRegistry fileTypeRegistry) {
        super(locale.actionQuickFixTitle(),
              locale.actionQuickFixDescription(),
              null,
              editorAgent,
              fileTypeRegistry);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        EditorPartPresenter activeEditor = editorAgent.getActiveEditor();
        if (activeEditor == null) {
            return;
        }
        if (activeEditor instanceof HandlesTextOperations) {

            HandlesTextOperations textEditor = (HandlesTextOperations)activeEditor;
            if (textEditor.canDoOperation(TextEditorOperations.QUICK_ASSIST)) {
                textEditor.doOperation(TextEditorOperations.QUICK_ASSIST);
            }
        }
    }
}
