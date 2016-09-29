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

import static java.util.Collections.singletonList;
import static org.eclipse.che.ide.workspace.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

/**
 * Action for 'Signature help', in general should show signature of something callable.
 *
 * @author Evgen Vidolob
 */
@Singleton
public class SignatureHelpAction extends AbstractPerspectiveAction {

    private final EditorAgent editorAgent;

    @Inject
    public SignatureHelpAction(EditorAgent editorAgent, CoreLocalizationConstant constant) {
        super(singletonList(PROJECT_PERSPECTIVE_ID), constant.signatureName(), constant.signatureDescription(), null, null);
        this.editorAgent = editorAgent;
    }

    @Override
    public void updateInPerspective(@NotNull ActionEvent event) {
        final EditorPartPresenter editor = editorAgent.getActiveEditor();
        boolean isCanDoOperation = false;

        HandlesTextOperations handlesOperations;
        if (editor instanceof HandlesTextOperations) {
            handlesOperations = (HandlesTextOperations)editor;
            isCanDoOperation = handlesOperations.canDoOperation(TextEditorOperations.SIGNATURE_HELP);
        }

        event.getPresentation().setEnabledAndVisible(isCanDoOperation);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final EditorPartPresenter editor = editorAgent.getActiveEditor();
        HandlesTextOperations handlesOperations;
        if (editor instanceof HandlesTextOperations) {
            handlesOperations = (HandlesTextOperations)editor;
            if (handlesOperations.canDoOperation(TextEditorOperations.SIGNATURE_HELP)) {
                handlesOperations.doOperation(TextEditorOperations.SIGNATURE_HELP);
            }
        }
    }
}
