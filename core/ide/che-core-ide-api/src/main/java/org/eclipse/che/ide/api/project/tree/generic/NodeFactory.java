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

import org.eclipse.che.api.project.shared.dto.ItemReference;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.project.tree.TreeNode;
import org.eclipse.che.ide.api.project.tree.TreeStructure;

import javax.validation.constraints.NotNull;

/**
 * Factory that helps to create nodes for {@link GenericTreeStructure}.
 *
 * @author Artem Zatsarynnyi
 */
public interface NodeFactory {
    /**
     * Creates a new {@link FileNode} owned by the specified {@code treeStructure}
     * with the specified {@code parent} and associated {@code data}.
     *
     * @param parent
     *         the parent node
     * @param data
     *         the associated {@link ItemReference}
     * @param treeStructure
     *         the {@link org.eclipse.che.ide.api.project.tree.TreeStructure} to create the node for
     * @return a new {@link FileNode}
     */
    FileNode newFileNode(@NotNull TreeNode<?> parent,
                         @NotNull ItemReference data,
                         @NotNull TreeStructure treeStructure);

    /**
     * Creates a new {@link FolderNode} owned by the specified {@code treeStructure}
     * with the specified {@code parent} and associated {@code data}.
     *
     * @param parent
     *         the parent node
     * @param data
     *         the associated {@link ItemReference}
     * @param treeStructure
     *         the {@link GenericTreeStructure} to create the node for
     * @return a new {@link FolderNode}
     */
    FolderNode newFolderNode(@NotNull TreeNode<?> parent,
                             @NotNull ItemReference data,
                             @NotNull GenericTreeStructure treeStructure);

    /**
     * Creates a new {@link ProjectNode} owned by the specified {@code treeStructure}
     * with the specified {@code parent} and associated {@code data}.
     *
     * @param parent
     *         the parent node
     * @param data
     *         the associated {@link ProjectConfigDto}
     * @param treeStructure
     *         the {@link GenericTreeStructure} to create the node for
     * @return a new {@link ProjectNode}
     */
    ProjectNode newProjectNode(@Nullable TreeNode<?> parent,
                               @NotNull ProjectConfigDto data,
                               @NotNull GenericTreeStructure treeStructure);
}
