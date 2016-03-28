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

import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.Resources;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.texteditor.HandlesTextOperations;
import org.eclipse.che.ide.api.texteditor.TextEditorOperations;

import javax.validation.constraints.NotNull;
import java.util.Arrays;

import static org.eclipse.che.ide.workspace.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

/**
 * Formatter Action
 *
 * @author Roman Nikitenko
 * @author Dmitry Shnurenko
 */
public class FormatterAction extends AbstractPerspectiveAction {

    private final AppContext           appContext;
    private final EditorAgent          editorAgent;

    @Inject
    public FormatterAction(AppContext appContext,
                           EditorAgent editorAgent,
                           CoreLocalizationConstant localization,
                           Resources resources) {
        super(Arrays.asList(PROJECT_PERSPECTIVE_ID),
              localization.formatName(),
              localization.formatDescription(),
              null,
              resources.format());
        this.appContext = appContext;
        this.editorAgent = editorAgent;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final EditorPartPresenter editor = editorAgent.getActiveEditor();
        HandlesTextOperations handlesOperations;
        if (editor instanceof HandlesTextOperations) {
            handlesOperations = (HandlesTextOperations)editor;
            if (handlesOperations.canDoOperation(TextEditorOperations.FORMAT)) {
                handlesOperations.doOperation(TextEditorOperations.FORMAT);
            }
        }
    }

    @Override
    public void updateInPerspective(@NotNull ActionEvent event) {
        final EditorPartPresenter editor = editorAgent.getActiveEditor();
        boolean isCanDoOperation = false;

        HandlesTextOperations handlesOperations;
        if (editor instanceof HandlesTextOperations) {
            handlesOperations = (HandlesTextOperations)editor;
            isCanDoOperation = handlesOperations.canDoOperation(TextEditorOperations.FORMAT);
        }

        event.getPresentation().setEnabled(isCanDoOperation);
        event.getPresentation().setVisible(appContext.getCurrentProject() != null);
    }
}
