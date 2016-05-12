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
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.ide.api.project.ProjectServiceClient;
import org.eclipse.che.api.project.shared.dto.ItemReference;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.event.RenameNodeEvent;
import org.eclipse.che.ide.api.event.project.CloseCurrentProjectEvent;
import org.eclipse.che.ide.api.project.node.HasProjectConfig;
import org.eclipse.che.ide.api.project.tree.AbstractTreeNode;
import org.eclipse.che.ide.api.project.tree.TreeNode;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.rest.Unmarshallable;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * Node that represents a project.
 *
 * @author Artem Zatsarynnyi
 */
@Deprecated
public class ProjectNode extends AbstractTreeNode<ProjectConfigDto> implements StorableNode<ProjectConfigDto>, Openable,
                                                                               UpdateTreeNodeDataIterable {
    protected final ProjectServiceClient   projectServiceClient;
    protected final DtoUnmarshallerFactory dtoUnmarshallerFactory;
    private final   AppContext             appContext;
    protected final EventBus eventBus;

    private final GenericTreeStructure treeStructure;
    private final String               workspaceId;

    private boolean opened;

    @Inject
    public ProjectNode(@Assisted TreeNode<?> parent,
                       @Assisted ProjectConfigDto data,
                       @Assisted GenericTreeStructure treeStructure,
                       AppContext appContext,
                       EventBus eventBus,
                       ProjectServiceClient projectService,
                       DtoUnmarshallerFactory dtoUnmarshallerFactory) {
        super(parent, data, treeStructure, eventBus);

        this.treeStructure = treeStructure;
        this.appContext = appContext;
        this.eventBus = eventBus;
        this.projectServiceClient = projectService;
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
        this.workspaceId = appContext.getWorkspace().getId();
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return getData().getName();
    }

    /** {@inheritDoc} */
    @Override
    public String getPath() {
        return getData().getPath();
    }

    /** {@inheritDoc} */
    @Override
    public boolean canContainsFolder() {
        return true;
    }

    /** {@inheritDoc} */
    @NotNull
    @Override
    public String getId() {
        return getData().getName();
    }

    /** {@inheritDoc} */
    @NotNull
    @Override
    public HasProjectConfig getProject() {
        return new HasProjectConfig() {
            @NotNull
            @Override
            public ProjectConfigDto getProjectConfig() {
                return getData();
            }

            @Override
            public void setProjectConfig(@NotNull ProjectConfigDto projectConfig) {
                //stub
            }
        };
    }

    /** {@inheritDoc} */
    @NotNull
    @Override
    public String getDisplayName() {
        return getData().getName();
    }

    /** Returns {@link org.eclipse.che.ide.api.project.tree.TreeStructure} which this node belongs. */
    @NotNull
    public GenericTreeStructure getTreeStructure() {
        return treeStructure;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isLeaf() {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public void rename(final String newName, final RenameCallback renameCallback) {
        projectServiceClient.rename(appContext.getDevMachine(), getPath(), newName, null, new AsyncRequestCallback<Void>() {
            @Override
            protected void onSuccess(Void result) {
                final String parentPath = ((StorableNode)getParent()).getPath();
                final String newPath = parentPath + "/" + newName;
                eventBus.fireEvent(new RenameNodeEvent(ProjectNode.this, newPath));
            }

            @Override
            protected void onFailure(Throwable exception) {
                renameCallback.onFailure(exception);
            }
        });
    }

    /** {@inheritDoc} */
    public void updateData(final AsyncCallback<Void> asyncCallback, String newPath) {
        Unmarshallable<ProjectConfigDto> unmarshaller = dtoUnmarshallerFactory.newUnmarshaller(ProjectConfigDto.class);
        projectServiceClient.getProject(appContext.getDevMachine(), newPath, new AsyncRequestCallback<ProjectConfigDto>(unmarshaller) {
            @Override
            protected void onSuccess(ProjectConfigDto result) {
                setData(result);
                asyncCallback.onSuccess(null);
            }

            @Override
            protected void onFailure(Throwable exception) {
                asyncCallback.onFailure(exception);
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void refreshChildren(final AsyncCallback<TreeNode<?>> callback) {
        getModules(getData(), new AsyncCallback<List<ProjectConfigDto>>() {
            @Override
            public void onSuccess(final List<ProjectConfigDto> modules) {
                getChildren(getData().getPath(), new AsyncCallback<List<ItemReference>>() {
                    @Override
                    public void onSuccess(List<ItemReference> childItems) {
                        setChildren(getChildNodesForItems(childItems, modules));
                        callback.onSuccess(ProjectNode.this);
                    }

                    @Override
                    public void onFailure(Throwable caught) {
                        callback.onFailure(caught);
                    }
                });
            }

            @Override
            public void onFailure(Throwable caught) {
                //can be if pom.xml not found
                getChildren(getData().getPath(), new AsyncCallback<List<ItemReference>>() {
                    @Override
                    public void onSuccess(List<ItemReference> childItems) {
                        callback.onSuccess(ProjectNode.this);
                    }

                    @Override
                    public void onFailure(Throwable caught) {
                        callback.onFailure(caught);
                    }
                });
                callback.onFailure(caught);
            }
        });
    }

    protected void getModules(ProjectConfigDto project, final AsyncCallback<List<ProjectConfigDto>> callback) {
        final Unmarshallable<List<ProjectConfigDto>> unmarshaller = dtoUnmarshallerFactory.newListUnmarshaller(ProjectConfigDto.class);
        projectServiceClient.getModules(appContext.getDevMachine(), project.getPath(), new AsyncRequestCallback<List<ProjectConfigDto>>(unmarshaller) {
            @Override
            protected void onSuccess(List<ProjectConfigDto> result) {
                callback.onSuccess(result);
            }

            @Override
            protected void onFailure(Throwable exception) {
                callback.onFailure(exception);
            }
        });
    }

    private List<TreeNode<?>> getChildNodesForItems(List<ItemReference> childItems, List<ProjectConfigDto> modules) {
        List<TreeNode<?>> oldChildren = getChildren();
        List<TreeNode<?>> newChildren = new ArrayList<>();
        for (ItemReference item : childItems) {
            AbstractTreeNode node = createChildNode(item, modules);
            if (node != null) {
                if (oldChildren.contains(node)) {
                    final int i = oldChildren.indexOf(node);
                    newChildren.add(oldChildren.get(i));
                } else {
                    newChildren.add(node);
                }
            }
        }
        return newChildren;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isRenamable() {
        // Rename is not available for opened project.
        // Special message will be shown for user in this case (see RenameItemAction).
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isDeletable() {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public void delete(final DeleteCallback callback) {
        projectServiceClient.delete(appContext.getDevMachine(), getPath(), new AsyncRequestCallback<Void>() {
            @Override
            protected void onSuccess(Void result) {
                if (isRootProject()) {
                    eventBus.fireEvent(new CloseCurrentProjectEvent(getData()));
                } else {
                    //fire module delete event
                }
                ProjectNode.super.delete(callback);
            }

            @Override
            protected void onFailure(Throwable exception) {
                callback.onFailure(exception);
            }
        });
    }

    /**
     * Method helps to retrieve child {@link ItemReference}s by the specified path using Codenvy Project API.
     * <p/>
     * It takes into account state of the 'show hidden items' setting.
     *
     * @param path
     *         path to retrieve children
     * @param callback
     *         callback to return retrieved children
     */
    protected void getChildren(String path, final AsyncCallback<List<ItemReference>> callback) {
        final List<ItemReference> children = new ArrayList<>();
        final Unmarshallable<List<ItemReference>> unmarshaller = dtoUnmarshallerFactory.newListUnmarshaller(ItemReference.class);
        projectServiceClient.getChildren(appContext.getDevMachine(), path, new AsyncRequestCallback<List<ItemReference>>(unmarshaller) {
            @Override
            protected void onSuccess(List<ItemReference> result) {
                final boolean isShowHiddenItems = getTreeStructure().getSettings().isShowHiddenItems();
                for (ItemReference item : result) {
                    if (!isShowHiddenItems && item.getName().startsWith(".")) {
                        continue;
                    }
                    children.add(item);
                }

                callback.onSuccess(children);
            }

            @Override
            protected void onFailure(Throwable exception) {
                callback.onFailure(exception);
            }
        });
    }

    /** Get unique ID of type of project. */
    public String getProjectTypeId() {
        return getData().getType();
    }

    /**
     * Creates node for the specified item. Method called for every child item in {@link #refreshChildren(AsyncCallback)} method.
     * <p/>
     * May be overridden in order to provide a way to create a node for the specified by.
     *
     * @param item
     *         {@link ItemReference} for which need to create node
     * @return new node instance or {@code null} if the specified item is not supported
     */
    @Nullable
    protected AbstractTreeNode<?> createChildNode(ItemReference item, List<ProjectConfigDto> modules) {
        if ("project".equals(item.getType())) {
            return getTreeStructure().newFolderNode(this, item);
        } else if ("folder".equals(item.getType())) {
            return getTreeStructure().newFolderNode(this, item);
        } else if ("file".equals(item.getType())) {
            return getTreeStructure().newFileNode(this, item);
        }

        return null;
    }

    /**
     * Returns value of the specified attribute.
     *
     * @param attributeName
     *         name of the attribute to get its value
     * @return value of the specified attribute or {@code null} if attribute does not exists
     */
    @Nullable
    @Deprecated
    public String getAttributeValue(String attributeName) {
        List<String> attributeValues = getAttributeValues(attributeName);
        if (attributeValues != null && !attributeValues.isEmpty()) {
            return attributeValues.get(0);
        }
        return null;
    }

    /**
     * Returns values list of the specified attribute.
     *
     * @param attributeName
     *         name of the attribute to get its values
     * @return {@link List} of attribute values or {@code null} if attribute does not exists
     * @see #getAttributeValue(String)
     */
    @Nullable
    @Deprecated
    public List<String> getAttributeValues(String attributeName) {
        return getData().getAttributes().get(attributeName);
    }

    @Override
    public void close() {
        opened = false;
    }

    @Override
    public boolean isOpened() {
        return opened;
    }

    @Override
    public void open() {
        opened = true;
    }

    private boolean isRootProject() {
        return getParent().getParent() == null;
    }
}
