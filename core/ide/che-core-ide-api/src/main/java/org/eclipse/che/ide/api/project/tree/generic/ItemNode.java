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

import org.eclipse.che.api.project.gwt.client.ProjectServiceClient;
import org.eclipse.che.api.project.shared.dto.ItemReference;
import org.eclipse.che.ide.api.event.RenameNodeEvent;
import org.eclipse.che.ide.api.project.tree.AbstractTreeNode;
import org.eclipse.che.ide.api.project.tree.TreeNode;
import org.eclipse.che.ide.api.project.tree.TreeStructure;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.rest.Unmarshallable;

import org.eclipse.che.ide.api.app.AppContext;


import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.web.bindery.event.shared.EventBus;

import javax.validation.constraints.NotNull;

/**
 * Abstract base class for all tree nodes that represent an {@link ItemReference}.
 * There are exactly two kinds of {@link ItemNode}: {@link FileNode}, {@link FolderNode}.
 *
 * @author Artem Zatsarynnyi
 * @see FileNode
 * @see FolderNode
 */
@Deprecated
public abstract class ItemNode extends AbstractTreeNode<ItemReference> implements StorableNode<ItemReference>, UpdateTreeNodeDataIterable {
    protected ProjectServiceClient    projectServiceClient;
    protected DtoUnmarshallerFactory  dtoUnmarshallerFactory;
    
    private final String workspaceId;

    /**
     * Creates new node.
     *
     * @param parent
     *         parent node
     * @param data
     *         an object this node encapsulates
     * @param treeStructure
     *         {@link org.eclipse.che.ide.api.project.tree.TreeStructure} which this node belongs
     * @param eventBus
     *         {@link EventBus}
     * @param projectServiceClient
     *         {@link ProjectServiceClient}
     * @param dtoUnmarshallerFactory
     *         {@link DtoUnmarshallerFactory}
     */
    public ItemNode(TreeNode<?> parent,
                    ItemReference data,
                    TreeStructure treeStructure,
                    EventBus eventBus,
                    AppContext appContext,
                    ProjectServiceClient projectServiceClient,
                    DtoUnmarshallerFactory dtoUnmarshallerFactory) {
        super(parent, data, treeStructure, eventBus);
        this.projectServiceClient = projectServiceClient;
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
        
        this.workspaceId = appContext.getWorkspace().getId();
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
    public String getDisplayName() {
        return getData().getName();
    }

    /** {@inheritDoc} */
    @Override
    public void refreshChildren(AsyncCallback<TreeNode<?>> callback) {
    }

    /** {@inheritDoc} */
    @NotNull
    @Override
    public String getName() {
        return getData().getName();
    }

    /** {@inheritDoc} */
    @NotNull
    @Override
    public String getPath() {
        return getData().getPath();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isRenamable() {
        return true;
    }

    /** Rename appropriate {@link ItemReference} using Codenvy Project API. */
    @Override
    public void rename(final String newName, final RenameCallback renameCallback) {
        projectServiceClient.rename(workspaceId, getPath(), newName, null, new AsyncRequestCallback<Void>() {
            @Override
            protected void onSuccess(final Void result) {
                String parentPath = ((StorableNode)getParent()).getPath();
                String newPath = parentPath + "/" + newName;
                eventBus.fireEvent(new RenameNodeEvent(ItemNode.this, newPath));
            }

            @Override
            protected void onFailure(Throwable exception) {
                renameCallback.onFailure(exception);
            }
        });
    }

    /** {@inheritDoc} */
    public void updateData(final AsyncCallback<Void> asyncCallback, String newPath) {
        Unmarshallable<ItemReference> unmarshaller = dtoUnmarshallerFactory.newUnmarshaller(ItemReference.class);
        projectServiceClient.getItem(workspaceId, newPath, new AsyncRequestCallback<ItemReference>(unmarshaller) {
            @Override
            protected void onSuccess(ItemReference result) {
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
    public boolean isDeletable() {
        return true;
    }

    /** Delete appropriate {@link ItemReference} using Codenvy Project API. */
    @Override
    public void delete(final DeleteCallback callback) {
        projectServiceClient.delete(workspaceId, getPath(), new AsyncRequestCallback<Void>() {
            @Override
            protected void onSuccess(Void result) {
                ItemNode.super.delete(new DeleteCallback() {
                    @Override
                    public void onDeleted() {
                        callback.onDeleted();
                    }

                    @Override
                    public void onFailure(Throwable exception) {
                        callback.onFailure(exception);
                    }
                });
//                fire event that node has been deleted
            }

            @Override
            protected void onFailure(Throwable exception) {
                callback.onFailure(exception);
            }
        });
    }
}
