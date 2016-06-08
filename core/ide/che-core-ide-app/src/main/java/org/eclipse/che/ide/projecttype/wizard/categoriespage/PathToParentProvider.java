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
package org.eclipse.che.ide.projecttype.wizard.categoriespage;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.core.model.project.ProjectConfig;
import org.eclipse.che.ide.api.data.tree.Node;
import org.eclipse.che.ide.api.project.type.wizard.ProjectWizardMode;
import org.eclipse.che.ide.api.selection.Selection;
import org.eclipse.che.ide.api.selection.SelectionAgent;
import org.eclipse.che.ide.project.node.FolderReferenceNode;
import org.eclipse.che.ide.project.node.ProjectNode;

/**
 * The class provides path to parent node for different project wizard modes.Fro example we have some tree structure and select
 * /project/some_folder node (some_folder is just a folder not a project):
 * <p/>
 * <pre>
 * /project
 *      /some_folder
 *      /src
 *          /main
 * ...
 * </pre>
 * <p/>
 * In project wizard mode CREATE the path to parent will be following: /project
 * In project wizard mode UPDATE the path to parent will be following: /
 * Note if project wizard mode is UPDATE and /project/some_folder is ordinary folder provider returns path to parent '/' because
 * path will be get from project configuration of /project and parent /project is '/', but if /project/some_folder is project node
 * the provider returns path to parent '/project' because path will be get from its own project configuration.
 * <p/>
 *
 * @author Dmitry Shnurenko
 */
@Singleton
class PathToParentProvider {
    final static String DEFAULT_PARENT_DIRECTORY = "/";

    private final SelectionAgent selectionAgent;

    @Inject
    PathToParentProvider(SelectionAgent selectionAgent) {
        this.selectionAgent = selectionAgent;
    }

    /**
     * Returns path to parent node. In according to project wizard mode the path can be get from selected node or from project
     * configuration.
     *
     * @param wizardMode
     *         project wizard mode
     * @param projectConfig
     *         project configuration from which path can be get
     * @return path to parent node.
     */
    public String getPathToParent(ProjectWizardMode wizardMode, ProjectConfig projectConfig) {
        switch (wizardMode) {
            case CREATE:
                return getPathToParentFromSelectedNode();
            case UPDATE:
                return getPathToParentFromConfig(projectConfig);
            default:
                return "";
        }
    }

    private String getPathToParentFromSelectedNode() {
        Selection<?> selection = selectionAgent.getSelection();
        if (selection == null || selection.isEmpty()) {
            return DEFAULT_PARENT_DIRECTORY;
        }

        if (selection.getAllElements().size() > 1) {
            return DEFAULT_PARENT_DIRECTORY;
        }

        Object selectedElement = selection.getHeadElement();

        if (selectedElement instanceof FolderReferenceNode) {
            Node parent = ((FolderReferenceNode)selectedElement).getParent();
            return getPath(parent);
        } else if (selectedElement instanceof ProjectNode) {
            Node parent = ((ProjectNode)selectedElement).getParent();
            return getPath(parent);
        }

        return DEFAULT_PARENT_DIRECTORY;
    }

    private String getPath(Node parent) {
        if (parent instanceof ProjectNode) {
            return ((ProjectNode)parent).getStorablePath() + '/';
        } else if (parent instanceof FolderReferenceNode) {
            return ((FolderReferenceNode)parent).getData().getPath() + '/';
        }

        return DEFAULT_PARENT_DIRECTORY;
    }

    private String getPathToParentFromConfig(ProjectConfig projectConfig) {
        String path = projectConfig.getPath();

        if (Strings.isNullOrEmpty(path)) {
            return DEFAULT_PARENT_DIRECTORY;
        }

        path = path.substring(0, path.lastIndexOf("/"));

        return path.isEmpty() ? DEFAULT_PARENT_DIRECTORY : path;
    }
}
