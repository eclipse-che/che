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
package org.eclipse.che.ide.api.project.tree;

import com.google.gwt.user.client.rpc.AsyncCallback;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Defines the requirements for an object that can be used as a project's tree structure.
 *
 * @author Artem Zatsarynnyi
 */
public interface TreeStructure {
    /**
     * Get the root nodes of the tree structure.
     *
     * @param callback
     *         callback to return the root nodes
     */
    // TODO: should return one root node that may be visible/hidden in tree
    void getRootNodes(@NotNull AsyncCallback<List<TreeNode<?>>> callback);

    /** Returns the settings for this tree structure. */
    @NotNull
    TreeSettings getSettings();

    /**
     * Looks for the node with the specified path in the tree structure
     * and returns it or {@code null} if it was not found.
     *
     * @param path
     *         node path
     * @param callback
     *         callback to return node, may return {@code null} if node not found
     */
    void getNodeByPath(@NotNull String path, @NotNull AsyncCallback<TreeNode<?>> callback);
}
