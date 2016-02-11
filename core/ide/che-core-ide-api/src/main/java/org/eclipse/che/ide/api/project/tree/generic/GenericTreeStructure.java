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
package org.eclipse.che.ide.api.project.tree.generic;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.project.gwt.client.ProjectServiceClient;
import org.eclipse.che.api.project.shared.dto.ItemReference;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.app.CurrentProject;
import org.eclipse.che.ide.api.project.tree.TreeNode;
import org.eclipse.che.ide.api.project.tree.TreeSettings;
import org.eclipse.che.ide.api.project.tree.TreeStructure;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;

import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.List;

/**
 * Serves as a 'generic' tree and as the factory for creating new tree nodes owned by that tree.
 * <p/>
 * Builds a currently opened project's tree structure that reflects the project's physical structure.
 *
 * @author Artem Zatsarynnyi
 * @see NodeFactory
 * @see org.eclipse.che.ide.api.project.tree.TreeSettings
 */
public class GenericTreeStructure implements TreeStructure {
    protected final NodeFactory            nodeFactory;
    protected       EventBus               eventBus;
    protected       AppContext             appContext;
    protected       ProjectServiceClient   projectServiceClient;
    protected       DtoUnmarshallerFactory dtoUnmarshallerFactory;
    private         ProjectNode            projectNode;

    protected GenericTreeStructure(NodeFactory nodeFactory, EventBus eventBus, AppContext appContext,
                                   ProjectServiceClient projectServiceClient, DtoUnmarshallerFactory dtoUnmarshallerFactory) {
        this.nodeFactory = nodeFactory;
        this.eventBus = eventBus;
        this.appContext = appContext;
        this.projectServiceClient = projectServiceClient;
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
    }

    /** {@inheritDoc} */
    @Override
    public void getRootNodes(@NotNull AsyncCallback<List<TreeNode<?>>> callback) {
        if (projectNode == null) {
            final CurrentProject currentProject = appContext.getCurrentProject();
            if (currentProject != null) {
                projectNode = newProjectNode(currentProject.getRootProject());
            } else {
                callback.onFailure(new IllegalStateException("No project is opened."));
                return;
            }
        }
        callback.onSuccess(Arrays.<TreeNode<?>>asList(projectNode));
    }

    @NotNull
    @Override
    public TreeSettings getSettings() {
        return TreeSettings.DEFAULT;
    }

    public NodeFactory getNodeFactory() {
        return nodeFactory;
    }

    @Override
    public void getNodeByPath(@NotNull final String path, @NotNull final AsyncCallback<TreeNode<?>> callback) {
        getRootNodes(new AsyncCallback<List<TreeNode<?>>>() {
            @Override
            public void onSuccess(List<TreeNode<?>> result) {
                ProjectNode project = null;
                for (TreeNode<?> node : result) {
                    if (node instanceof ProjectNode) {
                        project = (ProjectNode)node;
                        break;
                    }
                }

                if (project == null) {
                    callback.onFailure(new IllegalStateException("ProjectNode not found"));
                    return;
                }

                String p = path;
                if (path.startsWith("/")) {
                    p = path.substring(1);
                }
                getNodeByPathRecursively(project, p, project.getId().length() + 1, callback);
            }

            @Override
            public void onFailure(Throwable caught) {
                callback.onFailure(caught);
            }
        });
    }

    private void getNodeByPathRecursively(TreeNode<?> node, final String path, final int offset,
                                          final AsyncCallback<TreeNode<?>> callback) {
        node.refreshChildren(new AsyncCallback<TreeNode<?>>() {
            @Override
            public void onSuccess(TreeNode<?> result) {
                for (TreeNode<?> childNode : result.getChildren()) {
                    if (path.startsWith(childNode.getId(), offset)) {

                        final int nextOffset = offset + childNode.getId().length() + 1;

                        if (nextOffset - 1 == path.length()) {
                            callback.onSuccess(childNode);
                        } else {

                            int indexNextNodeSlash = path.indexOf("/", nextOffset - 1);

                            if (indexNextNodeSlash == nextOffset - 1) {
                                getNodeByPathRecursively(childNode, path, nextOffset, callback);
                            } else {//very similar path, f.e. /com/u but we need another /com/ua, we should get next child of com
                                continue;
                            }
                        }
                        return;
                    }
                }
                callback.onFailure(new IllegalStateException("Node not found"));
            }

            @Override
            public void onFailure(Throwable caught) {
                callback.onFailure(caught);
            }
        });
    }

    /**
     * Creates a new {@link ProjectNode} owned by this tree with the specified associated {@code data}.
     *
     * @param data
     *         the associated {@link ProjectConfigDto}
     * @return a new {@link ProjectNode}
     */
    public ProjectNode newProjectNode(@NotNull ProjectConfigDto data) {
        return getNodeFactory().newProjectNode(null, data, this);
    }

    /**
     * Creates a new {@link FileNode} owned by this tree
     * with the specified {@code parent} and associated {@code data}.
     *
     * @param parent
     *         the parent node
     * @param data
     *         the associated {@link ItemReference}
     * @return a new {@link FileNode}
     */
    public FileNode newFileNode(@NotNull TreeNode parent, @NotNull ItemReference data) {
        if (!"file".equals(data.getType())) {
            throw new IllegalArgumentException("The associated ItemReference type must be - file.");
        }
        return getNodeFactory().newFileNode(parent, data, this);
    }

    /**
     * Creates a new {@link FolderNode} owned by this tree
     * with the specified {@code parent} and associated {@code data}.
     *
     * @param parent
     *         the parent node
     * @param data
     *         the associated {@link ItemReference}
     * @return a new {@link FolderNode}
     */
    public FolderNode newFolderNode(@NotNull TreeNode parent, @NotNull ItemReference data) {
        if (!"folder".equals(data.getType()) && !"project".equals(data.getType())) {
            throw new IllegalArgumentException("The associated ItemReference type must be - folder or project.");
        }
        return getNodeFactory().newFolderNode(parent, data, this);
    }
}
