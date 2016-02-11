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

import org.eclipse.che.ide.api.project.tree.TreeNode;

/**
 * Defines the requirements for a node that represents an item which was
 * retrieved from Codenvy Project API (e.g. file, folder, project, module).
 *
 * @param <T>
 *         the type of the associated data
 * @author Artem Zatsarynnyi
 */
@Deprecated
public interface StorableNode<T> extends TreeNode<T> {
    /** Returns name of the item which this node represents. */
    String getName();

    /** Returns path of the item which this node represents. */
    String getPath();

    /**
     * Checks whether node can contains folders or not.
     * If {@code false} - new folders should not be created under this node.
     */
    boolean canContainsFolder();
}
