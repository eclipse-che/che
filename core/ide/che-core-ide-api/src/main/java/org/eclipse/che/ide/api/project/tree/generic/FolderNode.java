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

import org.eclipse.che.api.project.gwt.client.ProjectServiceClient;
import org.eclipse.che.api.project.shared.dto.ItemReference;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.project.tree.AbstractTreeNode;
import org.eclipse.che.ide.api.project.tree.TreeNode;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.rest.Unmarshallable;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * A node that represents a folder (an {@link ItemReference} with type - folder or project).
 *
 * @author Artem Zatsarynnyi
 */
@Deprecated
public class FolderNode extends ItemNode {

    private final AppContext appContext;

    @Inject
    public FolderNode(@Assisted TreeNode<?> parent,
                      @Assisted ItemReference data,
                      @Assisted GenericTreeStructure treeStructure,
                      EventBus eventBus,
                      ProjectServiceClient projectService,
                      AppContext appContext,
                      DtoUnmarshallerFactory dtoUnmarshallerFactory) {
        super(parent, data, treeStructure, eventBus, appContext, projectService, dtoUnmarshallerFactory);
        this.appContext = appContext;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isLeaf() {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public void refreshChildren(final AsyncCallback<TreeNode<?>> callback) {
        getChildren(getPath(), new AsyncCallback<List<ItemReference>>() {
            @Override
            public void onSuccess(List<ItemReference> childItems) {
                setChildren(getChildNodesForItems(childItems));
                callback.onSuccess(FolderNode.this);
            }

            @Override
            public void onFailure(Throwable exception) {
                callback.onFailure(exception);
            }
        });
    }

    private List<TreeNode<?>> getChildNodesForItems(List<ItemReference> childItems) {
        List<TreeNode<?>> oldChildren = getChildren();
        List<TreeNode<?>> newChildren = new ArrayList<>();
        for (ItemReference item : childItems) {
            final AbstractTreeNode node = createChildNode(item);
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
    protected AbstractTreeNode<?> createChildNode(ItemReference item) {
        if ("file".equals(item.getType())) {
            return getTreeStructure().newFileNode(this, item);
        } else if ("folder".equals(item.getType()) || "project".equals(item.getType())) {
            return getTreeStructure().newFolderNode(this, item);
        }
        return null;
    }

    /** {@inheritDoc} */
    @NotNull
    @Override
    public GenericTreeStructure getTreeStructure() {
        return (GenericTreeStructure)super.getTreeStructure();
    }

    /** {@inheritDoc} */
    @Override
    public boolean canContainsFolder() {
        return true;
    }
}
