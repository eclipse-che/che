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

import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.workspace.WorkspaceServiceClient;

import javax.validation.constraints.NotNull;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Collections.singletonList;
import static org.eclipse.che.ide.workspace.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

/**
 * The class contains business logic to stop workspace.
 *
 * TODO combine with StopMachineAction or remove it. These two actions duplicate own logic
 *
 * @author Dmitry Shnurenko
 */
public class StopWorkspaceAction extends AbstractPerspectiveAction {

    private final WorkspaceServiceClient workspaceService;
    private final AppContext             appContext;

    @Inject
    public StopWorkspaceAction(CoreLocalizationConstant locale,
                               WorkspaceServiceClient workspaceService,
                               AppContext appContext) {
        super(singletonList(PROJECT_PERSPECTIVE_ID), locale.stopWsTitle(), locale.stopWsDescription(), null, null);
        this.appContext = appContext;
        this.workspaceService = workspaceService;
    }

    @Override
    public void updateInPerspective(@NotNull ActionEvent event) {
        event.getPresentation().setVisible(true);
        event.getPresentation().setEnabled(appContext.getDevMachine() != null);
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed(ActionEvent event) {
        checkNotNull(appContext.getWorkspace().getId(), "Workspace id should not be null");

        workspaceService.stop(appContext.getWorkspace().getId());
    }
}
