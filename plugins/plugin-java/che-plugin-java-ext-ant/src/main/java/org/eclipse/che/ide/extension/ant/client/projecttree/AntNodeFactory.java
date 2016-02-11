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

import org.eclipse.che.api.project.shared.dto.ItemReference;
import org.eclipse.che.api.project.shared.dto.ProjectDescriptor;
import org.eclipse.che.ide.api.project.tree.TreeNode;
import org.eclipse.che.ide.ext.java.client.projecttree.JavaNodeFactory;

import javax.validation.constraints.NotNull;
import org.eclipse.che.commons.annotation.Nullable;

/**
 * Factory that helps to create nodes for {@link AntProjectTreeStructure}.
 *
 * @author Artem Zatsarynnyi
 * @see JavaNodeFactory
 */
public interface AntNodeFactory extends JavaNodeFactory {
    /**
     * Creates a new {@link AntFolderNode} owned by the specified {@code treeStructure}
     * with the specified {@code parent} and associated {@code data}.
     *
     * @param parent
     *         the parent node
     * @param data
     *         the associated {@link ItemReference}
     * @param treeStructure
     *         the {@link AntProjectTreeStructure} to create the node for
     * @return a new {@link AntFolderNode}
     */
    AntFolderNode newAntFolderNode(@NotNull TreeNode<?> parent,
                                   @NotNull ItemReference data,
                                   @NotNull AntProjectTreeStructure treeStructure);

    /**
     * Creates a new {@link AntProjectNode} owned by the specified {@code treeStructure}
     * with the specified {@code parent} and associated {@code data}.
     *
     * @param parent
     *         the parent node
     * @param data
     *         the associated {@link ProjectDescriptor}
     * @param treeStructure
     *         the {@link AntProjectTreeStructure} to create the node for
     * @return a new {@link AntProjectNode}
     */
    AntProjectNode newAntProjectNode(@Nullable TreeNode<?> parent,
                                     @NotNull ProjectDescriptor data,
                                     @NotNull AntProjectTreeStructure treeStructure);
}
