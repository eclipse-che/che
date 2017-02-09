/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.svn.ide.sw;

import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.data.tree.AbstractTreeNode;
import org.eclipse.che.ide.api.data.tree.Node;
import org.eclipse.che.ide.project.shared.NodesResources;
import org.eclipse.che.ide.ui.smartTree.presentation.HasPresentation;
import org.eclipse.che.ide.ui.smartTree.presentation.NodePresentation;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * A tree {@link Node} to be selected as a switch location.
 *
 * @author Anatoliy Bazko
 */
public class SvnNode extends AbstractTreeNode implements HasPresentation {

    interface ActionDelegate {
        Promise<List<Node>> list(String location);
    }

    private final String                  location;
    private final String                  name;
    private final NodesResources          resources;
    private final ActionDelegate delegate;

    public SvnNode(String location, NodesResources resources, ActionDelegate delegate) {
        this.location = location;
        this.resources = resources;
        this.delegate = delegate;

        String[] entries = location.split("/");
        this.name = entries.length == 0 ? location : entries[entries.length - 1];
    }

    @Override
    protected Promise<List<Node>> getChildrenImpl() {
        return delegate.list(location);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isLeaf() {
        return false;
    }

    public String getLocation() {
        return location;
    }

    @Override
    public void updatePresentation(@NotNull NodePresentation presentation) { }

    @Override
    public NodePresentation getPresentation(boolean update) {
        return new NodePresentation(name, null, null, resources.simpleFolder());
    }
}
