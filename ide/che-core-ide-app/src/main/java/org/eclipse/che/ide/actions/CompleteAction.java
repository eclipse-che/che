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
package org.eclipse.che.ide.actions;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.editor.texteditor.HandlesTextOperations;
import org.eclipse.che.ide.api.editor.texteditor.TextEditorOperations;

import javax.validation.constraints.NotNull;

/**
 * Calls editor complete(Ctrl+Space)
 *
 * @author Evgen Vidolob
 */
@Singleton
public class CompleteAction extends AbstractPerspectiveAction {

    private EditorAgent editorAgent;

    @Inject
    public CompleteAction(CoreLocalizationConstant coreLocalizationConstant, EditorAgent editorAgent) {
        super(null, coreLocalizationConstant.actionCompetitionsTitle());
        this.editorAgent = editorAgent;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        EditorPartPresenter activeEditor = editorAgent.getActiveEditor();
        if (activeEditor instanceof HandlesTextOperations) {
            ((HandlesTextOperations)activeEditor).doOperation(TextEditorOperations.CODEASSIST_PROPOSALS);
        }
    }

    @Override
    public void updateInPerspective(@NotNull ActionEvent e) {
        EditorPartPresenter activeEditor = editorAgent.getActiveEditor();
        if (activeEditor != null) {
            if (activeEditor instanceof HandlesTextOperations) {
                e.getPresentation().setVisible(true);
                if (((HandlesTextOperations)activeEditor).canDoOperation(TextEditorOperations.CODEASSIST_PROPOSALS)) {
                    e.getPresentation().setEnabled(true);
                } else {
                    e.getPresentation().setEnabled(false);
                }
            }
        } else {
            e.getPresentation().setEnabledAndVisible(false);
        }
    }
}
