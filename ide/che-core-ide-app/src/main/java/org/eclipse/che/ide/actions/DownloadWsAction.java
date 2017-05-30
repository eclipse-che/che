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
import org.eclipse.che.ide.Resources;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.machine.WsAgentURLModifier;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.download.DownloadContainer;

import javax.validation.constraints.NotNull;

import static java.util.Collections.singletonList;
import static org.eclipse.che.ide.workspace.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

/**
 * Download all projects from the workspace.
 *
 * @author Valeriy Svydenko
 */
@Singleton
public class DownloadWsAction extends AbstractPerspectiveAction {

    private final AppContext         appContext;
    private final WsAgentURLModifier wsAgentURLModifier;
    private final DownloadContainer  downloadContainer;

    @Inject
    public DownloadWsAction(AppContext appContext,
                            WsAgentURLModifier wsAgentURLModifier,
                            CoreLocalizationConstant locale,
                            Resources resources,
                            DownloadContainer downloadContainer) {
        super(singletonList(PROJECT_PERSPECTIVE_ID),
              locale.downloadProjectAsZipName(),
              locale.downloadProjectAsZipDescription(),
              null,
              resources.downloadZip());
        this.appContext = appContext;
        this.wsAgentURLModifier = wsAgentURLModifier;
        this.downloadContainer = downloadContainer;
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed(ActionEvent e) {
        downloadContainer.setUrl(wsAgentURLModifier.modify(appContext.getDevMachine().getWsAgentBaseUrl() + "/project/export/"));
    }

    /** {@inheritDoc} */
    @Override
    public void updateInPerspective(@NotNull ActionEvent e) {
        final Project[] projects = appContext.getProjects();
        e.getPresentation().setVisible(true);
        e.getPresentation().setEnabled(projects != null && projects.length > 0);
    }
}
