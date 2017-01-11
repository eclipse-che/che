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
package org.eclipse.che.ide.extension.machine.client.perspective.widgets.machine.panel;

import org.eclipse.che.ide.ui.tree.NodeDataAdapter;
import org.eclipse.che.ide.ui.tree.TreeNodeElement;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Dmitry Shnurenko
 */
public class MachineDataAdapter implements NodeDataAdapter<MachineTreeNode> {

    /** {@inheritDoc} */
    @Override
    public int compare(MachineTreeNode current, MachineTreeNode other) {
        return current.getId().compareTo(other.getId());
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasChildren(MachineTreeNode data) {
        Collection<MachineTreeNode> children = data.getChildren();

        return children != null && !children.isEmpty();
    }

    /** {@inheritDoc} */
    @Override
    @NotNull
    public List<MachineTreeNode> getChildren(MachineTreeNode data) {
        List<MachineTreeNode> children = new ArrayList<>();

        Collection<MachineTreeNode> nodes = data.getChildren();

        if (nodes == null) {
            return children;
        }

        for (MachineTreeNode node : nodes) {
            children.add(node);
        }

        return children;
    }

    /** {@inheritDoc} */
    @Override
    @NotNull
    public String getNodeId(MachineTreeNode data) {
        return data.getId();
    }

    /** {@inheritDoc} */
    @Override
    @NotNull
    public String getNodeName(MachineTreeNode data) {
        return data.getName();
    }

    /** {@inheritDoc} */
    @Override
    @NotNull
    public MachineTreeNode getParent(MachineTreeNode data) {
        return data.getParent();
    }

    /** {@inheritDoc} */
    @Override
    @NotNull
    public TreeNodeElement<MachineTreeNode> getRenderedTreeNode(MachineTreeNode data) {
        return data.getTreeNodeElement();
    }

    /** {@inheritDoc} */
    @Override
    public void setNodeName(MachineTreeNode data, String name) {
        throw new UnsupportedOperationException("The method isn't supported in this mode...");
    }

    /** {@inheritDoc} */
    @Override
    public void setRenderedTreeNode(MachineTreeNode data, TreeNodeElement<MachineTreeNode> renderedNode) {
        data.setTreeNodeElement(renderedNode);
    }

    /** {@inheritDoc} */
    @Override
    public MachineTreeNode getDragDropTarget(MachineTreeNode data) {
        throw new UnsupportedOperationException("The method isn't supported in this mode...");
    }

    /** {@inheritDoc} */
    @Override
    public List<String> getNodePath(MachineTreeNode data) {
        throw new UnsupportedOperationException("The method isn't supported in this mode...");
    }

    /** {@inheritDoc} */
    @Override
    public MachineTreeNode getNodeByPath(MachineTreeNode root, List<String> relativeNodePath) {
        throw new UnsupportedOperationException("The method isn't supported in this mode...");
    }
}
