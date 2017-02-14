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
package org.eclipse.che.ide.extension.machine.client.processes;

import org.eclipse.che.ide.ui.tree.NodeDataAdapter;
import org.eclipse.che.ide.ui.tree.TreeNodeElement;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Anna Shumilova
 */
public class ProcessDataAdapter implements NodeDataAdapter<ProcessTreeNode> {

    /** {@inheritDoc} */
    @Override
    public int compare(ProcessTreeNode current, ProcessTreeNode other) {
        return current.getId().compareTo(other.getId());
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasChildren(ProcessTreeNode data) {
        Collection<ProcessTreeNode> children = data.getChildren();

        return children != null && !children.isEmpty();
    }

    /** {@inheritDoc} */
    @Override
    @NotNull
    public List<ProcessTreeNode> getChildren(ProcessTreeNode data) {
        List<ProcessTreeNode> children = new ArrayList<>();
        Collection<ProcessTreeNode> nodes = data.getChildren();

        if (nodes == null) {
            return children;
        }

        for (ProcessTreeNode node : nodes) {
            children.add(node);
        }

        return children;
    }

    /** {@inheritDoc} */
    @Override
    @NotNull
    public String getNodeId(ProcessTreeNode data) {
        return data.getId();
    }

    /** {@inheritDoc} */
    @Override
    @NotNull
    public String getNodeName(ProcessTreeNode data) {
        return data.getName();
    }

    /** {@inheritDoc} */
    @Override
    @NotNull
    public ProcessTreeNode getParent(ProcessTreeNode data) {
        return data.getParent();
    }

    /** {@inheritDoc} */
    @Override
    @NotNull
    public TreeNodeElement<ProcessTreeNode> getRenderedTreeNode(ProcessTreeNode data) {
        return data.getTreeNodeElement();
    }

    /** {@inheritDoc} */
    @Override
    public void setNodeName(ProcessTreeNode data, String name) {
        throw new UnsupportedOperationException("The method isn't supported in this mode...");
    }

    /** {@inheritDoc} */
    @Override
    public void setRenderedTreeNode(ProcessTreeNode data, TreeNodeElement<ProcessTreeNode> renderedNode) {
        data.setTreeNodeElement(renderedNode);
    }

    /** {@inheritDoc} */
    @Override
    public ProcessTreeNode getDragDropTarget(ProcessTreeNode data) {
        throw new UnsupportedOperationException("The method isn't supported in this mode...");
    }

    /** {@inheritDoc} */
    @Override
    public List<String> getNodePath(ProcessTreeNode data) {
        throw new UnsupportedOperationException("The method isn't supported in this mode...");
    }

    /** {@inheritDoc} */
    @Override
    public ProcessTreeNode getNodeByPath(ProcessTreeNode root, List<String> relativeNodePath) {
        throw new UnsupportedOperationException("The method isn't supported in this mode...");
    }
}
