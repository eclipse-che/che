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
package org.eclipse.che.plugin.svn.ide.common.filteredtree;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.project.gwt.client.ProjectServiceClient;
import org.eclipse.che.api.project.shared.dto.ItemReference;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.project.tree.AbstractTreeNode;
import org.eclipse.che.ide.api.project.tree.TreeNode;
import org.eclipse.che.ide.api.project.tree.generic.ProjectNode;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Node that represents a filtered project.
 *
 * @author Vladyslav Zhukovskyi
 */
public class FilteredProjectNode extends ProjectNode {

    @AssistedInject
    public FilteredProjectNode(@Assisted TreeNode<?> parent,
                               @Assisted ProjectConfigDto data,
                               @Assisted FilteredTreeStructure treeStructure,
                               AppContext appContext,
                               EventBus eventBus,
                               ProjectServiceClient projectServiceClient,
                               DtoUnmarshallerFactory dtoUnmarshallerFactory) {
        super(parent, data, treeStructure, appContext, eventBus, projectServiceClient, dtoUnmarshallerFactory);
    }

    /** {@inheritDoc} */
    @NotNull
    @Override
    public FilteredTreeStructure getTreeStructure() {
        return (FilteredTreeStructure)super.getTreeStructure();
    }

    /** {@inheritDoc} */
    @Nullable
    @Override
    protected AbstractTreeNode<?> createChildNode(ItemReference item, List<ProjectConfigDto> modules) {
        if ("file".equals(item.getType())) {
            return getTreeStructure().newFileNode(FilteredProjectNode.this, item);
        } else if ("folder".equals(item.getType()) || "project".equals(item.getType())) {
            return getTreeStructure().newFolderNode(FilteredProjectNode.this, item);
        }
        return null;
    }
}
