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
import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.parts.Perspective;
import org.eclipse.che.ide.part.editor.EditorPartStackPresenter;
import org.eclipse.che.ide.workspace.perspectives.project.ProjectPerspective;

/**
 * Action for switching to next opened editor by hotKey
 * @author Alexander Andrienko
 */
public class SwitchRightTabAction extends Action {

    private final EditorAgent              editorAgent;
    private final EditorPartStackPresenter editorPartStackPresenter;

    @Inject
    public SwitchRightTabAction(EditorAgent editorAgent,
                                EditorPartStackPresenter editorPartStackPresenter,
                                CoreLocalizationConstant constant) {
        super(constant.switchToRightEditorAction(), constant.switchToRightEditorActionDescription());
        this.editorAgent = editorAgent;
        this.editorPartStackPresenter = editorPartStackPresenter;
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed(ActionEvent event) {
        EditorPartPresenter editor = editorAgent.getNextEditor();
        if (editor == null) {
            editor = editorAgent.getFirstEditor();
        }
        editorPartStackPresenter.setActivePart(editor);
    }

    /** {@inheritDoc} */
    @Override
    public void update(ActionEvent event) {
        Perspective perspective = event.getPerspectiveManager().getActivePerspective();
        if (editorAgent.getOpenedEditors().size() > 1 && perspective instanceof ProjectPerspective) {
            event.getPresentation().setEnabled(true);
        } else {
            event.getPresentation().setEnabled(false);
        }
    }
}
