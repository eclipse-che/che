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
package org.eclipse.che.ide.part.explorer.project;

import com.google.common.base.Predicate;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.data.tree.Node;
import org.eclipse.che.ide.api.data.tree.TreeExpander;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.ui.smartTree.Tree;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.any;

/**
 * Project explorer tree expander. Takes care about resources loading state, in case if tree has been never expanded before, it requests
 * infinite resource tree from the server and then expands project tree on the UI. On the second time it just expands project tree without
 * additional resource requests.
 *
 * @author Vlad Zhukovskyi
 * @since 5.0.0
 * @see TreeExpander
 */
final class ProjectExplorerTreeExpander implements TreeExpander {

    private Tree tree;
    private AppContext appContext;

    public ProjectExplorerTreeExpander(Tree tree, AppContext appContext) {
        this.tree = tree;
        this.appContext = appContext;
    }

    private final boolean[] everExpanded = new boolean[]{false};

    @Override
    public void expandTree() {
        if (everExpanded[0]) {
            tree.expandAll();

            return;
        }

        appContext.getWorkspaceRoot().getTree(-1).then(new Operation<Resource[]>() {
            @Override
            public void apply(Resource[] ignored) throws OperationException {
                everExpanded[0] = true;

                tree.expandAll();
            }
        });
    }

    @Override
    public boolean isExpandEnabled() {
        return tree.getNodeStorage().getAllItemsCount() != 0;
    }

    @Override
    public void collapseTree() {
        tree.collapseAll();
    }

    @Override
    public boolean isCollapseEnabled() {
        return any(tree.getRootNodes(), isExpanded());
    }

    private Predicate<Node> isExpanded() {
        return new Predicate<Node>() {
            @Override
            public boolean apply(@javax.annotation.Nullable Node node) {
                checkNotNull(node);

                return tree.isExpanded(node);
            }
        };
    }
}
