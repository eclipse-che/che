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
import org.eclipse.che.api.workspace.gwt.client.WorkspaceServiceClient;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceDto;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.parts.PerspectiveManager;
import static org.eclipse.che.ide.workspace.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

/**
 * The class contains business logic to stop workspace.
 *
 * @author Dmitry Shnurenko
 */
public class StopMachineAction extends Action {

    private final AppContext             appContext;
    private final WorkspaceServiceClient workspaceService;

    @Inject
    public StopMachineAction(CoreLocalizationConstant locale,
                             AppContext appContext,
                             WorkspaceServiceClient workspaceService) {
        super(locale.stopWsTitle(), locale.stopWsDescription(), null, null);

        this.appContext = appContext;
        this.workspaceService = workspaceService;
    }

    @Override
    public void update(ActionEvent event) {
        PerspectiveManager manager = event.getPerspectiveManager();

        if (PROJECT_PERSPECTIVE_ID.equals(manager.getPerspectiveId())) {
            event.getPresentation().setEnabledAndVisible(false);
            return;
        }

        event.getPresentation().setVisible(true);
        event.getPresentation().setEnabled(appContext.getWorkspace() != null);
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed(ActionEvent event) {
        final WorkspaceDto workspace = appContext.getWorkspace();

        workspaceService.stop(workspace.getId());
    }

}
