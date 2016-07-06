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
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.event.FileEvent;

import javax.validation.constraints.NotNull;

import static java.util.Collections.singletonList;
import static org.eclipse.che.ide.api.event.FileEvent.FileOperation.CLOSE;
import static org.eclipse.che.ide.workspace.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

/**
 * General action which listens current active editor and closes it if need.
 *
 * @author Vlad Zhukovskiy
 */
@Singleton
public class CloseActiveEditor extends AbstractPerspectiveAction {

    private final EditorAgent editorAgent;
    private final EventBus eventBus;

    @Inject
    public CloseActiveEditor(CoreLocalizationConstant locale,
                             EditorAgent editorAgent,
                             EventBus eventBus) {
        super(singletonList(PROJECT_PERSPECTIVE_ID), locale.editorTabClose(), locale.editorTabCloseDescription(), null, null);
        this.editorAgent = editorAgent;
        this.eventBus = eventBus;
    }

    /** {@inheritDoc} */
    @Override
    public void updateInPerspective(@NotNull ActionEvent event) {
        event.getPresentation().setEnabledAndVisible(editorAgent.getActiveEditor() != null);
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed(ActionEvent e) {
        eventBus.fireEvent(new FileEvent(editorAgent.getActiveEditor().getEditorInput().getFile(), CLOSE));
    }
}
