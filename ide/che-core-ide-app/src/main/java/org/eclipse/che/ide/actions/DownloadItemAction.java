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
import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.project.node.HasStorablePath;
import org.eclipse.che.ide.api.selection.Selection;
import org.eclipse.che.ide.download.DownloadContainer;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerPresenter;
import org.eclipse.che.ide.project.node.FileReferenceNode;

/**
 * Download selected item action.
 * If selected item is a project or a folder then item will be downloaded as a zip archive.
 *
 * @author Roman Nikitenko
 */
@Singleton
public class DownloadItemAction extends Action {

    private final String BASE_URL;

    private final AppContext appContext;
    private final DownloadContainer downloadContainer;
    private final ProjectExplorerPresenter projectExplorer;

    @Inject
    public DownloadItemAction(AppContext appContext,
                              CoreLocalizationConstant locale,
                              DownloadContainer downloadContainer,
                              ProjectExplorerPresenter projectExplorer) {
        super(locale.downloadItemName(), locale.downloadItemDescription());
        this.appContext = appContext;
        this.downloadContainer = downloadContainer;
        this.projectExplorer = projectExplorer;

        BASE_URL = "/project/export/";
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed(ActionEvent e) {
        Selection<?> selection = projectExplorer.getSelection();
        if (selection.isEmpty() || selection.getAllElements().size() > 1) {
            return;
        }

        Object selectedNode = selection.getHeadElement();

        if (!(selectedNode instanceof HasStorablePath)) {
            return;
        }

        String url = getUrl((HasStorablePath)selectedNode);
        downloadContainer.setUrl(url);
    }

    /** {@inheritDoc} */
    @Override
    public void update(ActionEvent event) {
        Selection<?> selection = projectExplorer.getSelection();

        if (selection == null || selection.isEmpty() || selection.getAllElements().size() > 1) {
            event.getPresentation().setEnabledAndVisible(false);
            return;
        }

        Object selectedNode = selection.getHeadElement();

        event.getPresentation().setEnabledAndVisible(selectedNode instanceof HasStorablePath);
    }

    private String getUrl(HasStorablePath node) {
        String path = normalizePath(node.getStorablePath());

        if (node instanceof FileReferenceNode) {
            return appContext.getDevMachine().getWsAgentBaseUrl() + BASE_URL + "file/" + path;
        }
        return appContext.getDevMachine().getWsAgentBaseUrl() + BASE_URL + path;
    }

    private String normalizePath(String path) {
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        return path;
    }
}
