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

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.event.FileEvent;
import org.eclipse.che.ide.api.event.FileEventHandler;
import org.eclipse.che.ide.api.resources.VirtualFile;

import javax.validation.constraints.NotNull;

import static java.util.Collections.singletonList;
import static org.eclipse.che.ide.api.event.FileEvent.FileOperation.CLOSE;
import static org.eclipse.che.ide.api.event.FileEvent.FileOperation.OPEN;
import static org.eclipse.che.ide.workspace.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;


/**
 * Performs restoring closed editor tab.
 *
 * @author Vlad Zhukovskiy
 */
@Singleton
public class ReopenClosedFileAction extends AbstractPerspectiveAction implements FileEventHandler {

    private final EventBus             eventBus;

    private VirtualFile lastClosed;

    @Inject
    public ReopenClosedFileAction(EventBus eventBus, CoreLocalizationConstant locale) {
        super(singletonList(PROJECT_PERSPECTIVE_ID), locale.editorTabReopenClosedTab(), locale.editorTabReopenClosedTabDescription(), null,
              null);
        this.eventBus = eventBus;
        eventBus.addHandler(FileEvent.TYPE, this);
    }

    /** {@inheritDoc} */
    @Override
    public void onFileOperation(FileEvent event) {
        if (event.getOperationType() == CLOSE) {
            lastClosed = event.getFile();
        } else if (event.getOperationType() == OPEN && lastClosed != null && lastClosed.equals(event.getFile())) {
            lastClosed = null;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void updateInPerspective(@NotNull ActionEvent event) {
        event.getPresentation().setEnabled(lastClosed != null);
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed(ActionEvent e) {
        eventBus.fireEvent(new FileEvent(lastClosed, OPEN));
    }
}
