/*******************************************************************************
 * Copyright (c) 2012-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.extension.ant.client.projecttree;

import org.eclipse.che.api.project.gwt.client.ProjectServiceClient;
import org.eclipse.che.api.project.shared.dto.ItemReference;
import org.eclipse.che.api.project.shared.dto.ProjectDescriptor;
import org.eclipse.che.ide.api.project.tree.AbstractTreeNode;
import org.eclipse.che.ide.api.project.tree.TreeNode;
import org.eclipse.che.ide.ext.java.client.projecttree.JavaSourceFolderUtil;
import org.eclipse.che.ide.ext.java.client.projecttree.nodes.JavaProjectNode;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.web.bindery.event.shared.EventBus;

import javax.validation.constraints.NotNull;
import org.eclipse.che.commons.annotation.Nullable;
import java.util.List;

/**
 * Node that represents Ant project.
 *
 * @author Vladyslav Zhukovskii
 */
public class AntProjectNode extends JavaProjectNode {

    /** Create instance of {@link AntProjectNode}. */
    @Inject
    protected AntProjectNode(@Assisted TreeNode<?> parent,
                             @Assisted ProjectDescriptor data,
                             @Assisted AntProjectTreeStructure treeStructure,
                             EventBus eventBus,
                             ProjectServiceClient projectServiceClient,
                             DtoUnmarshallerFactory dtoUnmarshallerFactory) {
        super(parent, data, treeStructure, eventBus, projectServiceClient, dtoUnmarshallerFactory);
    }

    /** {@inheritDoc} */
    @NotNull
    @Override
    public AntProjectTreeStructure getTreeStructure() {
        return (AntProjectTreeStructure)super.getTreeStructure();
    }

    /** {@inheritDoc} */
    @Nullable
    @Override
    protected AbstractTreeNode<?> createChildNode(ItemReference item, List<ProjectDescriptor> modules) {
        if (JavaSourceFolderUtil.isSourceFolder(item, getProject())) {
            return getTreeStructure().newSourceFolderNode(AntProjectNode.this, item);
        } else if ("folder".equals(item.getType())) {
            return getTreeStructure().newJavaFolderNode(AntProjectNode.this, item);
        } else {
            return super.createChildNode(item, modules);
        }
    }
}
