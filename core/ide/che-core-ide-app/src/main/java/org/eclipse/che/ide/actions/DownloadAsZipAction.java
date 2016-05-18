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
import org.eclipse.che.ide.Resources;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.app.CurrentProject;
import org.eclipse.che.ide.api.project.node.HasStorablePath;
import org.eclipse.che.ide.api.selection.Selection;
import org.eclipse.che.ide.download.DownloadContainer;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerPresenter;
import org.eclipse.che.ide.project.node.ProjectNode;

import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.List;

import static org.eclipse.che.ide.workspace.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

/**
 * Download project as zip action
 *
 * @author Roman Nikitenko
 * @author Dmitry Shnurenko
 */
@Singleton
public class DownloadAsZipAction extends AbstractPerspectiveAction {
    private final AppContext               appContext;
    private final DownloadContainer        downloadContainer;
    private final ProjectExplorerPresenter projectExplorer;

    @Inject
    public DownloadAsZipAction(AppContext appContext,
                               CoreLocalizationConstant locale,
                               Resources resources,
                               DownloadContainer downloadContainer,
                               ProjectExplorerPresenter projectExplorer) {
        super(Arrays.asList(PROJECT_PERSPECTIVE_ID),
              locale.downloadProjectAsZipName(),
              locale.downloadProjectAsZipDescription(),
              null,
              resources.downloadZip());
        this.appContext = appContext;
        this.downloadContainer = downloadContainer;
        this.projectExplorer = projectExplorer;
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed(ActionEvent e) {
        String url = appContext.getDevMachine().getWsAgentBaseUrl() + "/project/export/" + getPath();
        downloadContainer.setUrl(url);
    }

    /** {@inheritDoc} */
    @Override
    public void updateInPerspective(@NotNull ActionEvent event) {

        Selection<?> selection = projectExplorer.getSelection();
        boolean enabled = appContext.getCurrentProject() != null || (selection != null &&
                                                                     (!selection.isEmpty() &&
                                                                      selection.getHeadElement() instanceof ProjectNode));

        event.getPresentation().setVisible(true);
        event.getPresentation().setEnabled(enabled);
    }

    private String getPath() {
        String path = "";
        HasStorablePath selectedNode = null;

        List<?> selection = projectExplorer.getSelection().getAllElements();
        CurrentProject currentProject = appContext.getCurrentProject();

        if (!selection.isEmpty() && selection.get(0) instanceof HasStorablePath) {
            selectedNode = (HasStorablePath)selection.get(0);
        }

        if (selectedNode != null && selectedNode instanceof ProjectNode) {
            path = selectedNode.getStorablePath();
        } else if (currentProject != null) {
            path = currentProject.getProjectConfig().getPath();
        }

        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        return path;
    }
}
