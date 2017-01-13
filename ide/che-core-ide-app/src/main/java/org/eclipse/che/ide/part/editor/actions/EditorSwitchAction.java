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
package org.eclipse.che.ide.part.editor.actions;

import com.google.common.annotations.Beta;

import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;

import javax.validation.constraints.NotNull;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Collections.singletonList;
import static org.eclipse.che.ide.workspace.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

/**
 * @author Vlad Zhukovskiy
 * @author Roman Nikitenko
 */
@Beta
abstract class EditorSwitchAction extends AbstractPerspectiveAction {

    protected final EditorAgent                   editorAgent;

    public EditorSwitchAction(String text, String description, EditorAgent editorAgent) {
        super(singletonList(PROJECT_PERSPECTIVE_ID), text, description, null, null);
        this.editorAgent = editorAgent;
    }

    @Override
    public void updateInPerspective(@NotNull ActionEvent event) {
        event.getPresentation().setVisible(true);
        event.getPresentation().setEnabled(editorAgent.getOpenedEditors().size() > 1);
    }

    protected EditorPartPresenter getPreviousEditorBaseOn(EditorPartPresenter editor) {
        checkArgument(editor != null);

        return editorAgent.getPreviousFor(editor);
    }

    protected EditorPartPresenter getNextEditorBaseOn(EditorPartPresenter editor) {
        checkArgument(editor != null);

        return editorAgent.getNextFor(editor);
    }
}
