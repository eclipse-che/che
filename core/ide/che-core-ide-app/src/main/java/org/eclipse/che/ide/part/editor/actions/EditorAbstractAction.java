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
package org.eclipse.che.ide.part.editor.actions;

import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.project.tree.VirtualFile;
import org.vectomatic.dom.svg.ui.SVGResource;

import javax.validation.constraints.NotNull;

import static java.util.Collections.singletonList;
import static org.eclipse.che.ide.workspace.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

/**
 * Base action for editor tab.
 *
 * @author Vlad Zhukovskiy
 */
public abstract class EditorAbstractAction extends AbstractPerspectiveAction {

    public static final String CURRENT_FILE_PROP = "source";

    protected EditorAgent          editorAgent;
    protected EventBus             eventBus;

    public EditorAbstractAction(String tooltip,
                                String description,
                                SVGResource icon,
                                EditorAgent editorAgent,
                                EventBus eventBus) {
        super(singletonList(PROJECT_PERSPECTIVE_ID), tooltip, description, null, icon);
        this.editorAgent = editorAgent;
        this.eventBus = eventBus;
    }

    /** {@inheritDoc} */
    @Override
    public void updateInPerspective(@NotNull ActionEvent event) {
        event.getPresentation().setEnabledAndVisible(editorAgent.getOpenedEditors() != null && !editorAgent.getOpenedEditors().isEmpty());
    }

    /**
     * Fetch file from the action event. File should be passed by context menu during construction the last one.
     *
     * @param e
     *         action event
     * @return {@link VirtualFile} file.
     * @throws IllegalStateException
     *         in case if file not found in action event
     */
    protected VirtualFile getEditorFile(ActionEvent e) {
        Object o = e.getPresentation().getClientProperty(CURRENT_FILE_PROP);

        if (o instanceof VirtualFile) {
            return (VirtualFile)o;
        }

        throw new IllegalStateException("File doesn't provided");
    }
}
