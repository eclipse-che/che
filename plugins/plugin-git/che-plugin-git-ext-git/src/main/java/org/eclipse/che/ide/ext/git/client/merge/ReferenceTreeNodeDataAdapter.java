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
package org.eclipse.che.ide.ext.git.client.merge;

import org.eclipse.che.ide.ui.tree.NodeDataAdapter;
import org.eclipse.che.ide.ui.tree.TreeNodeElement;

import java.util.HashMap;
import java.util.List;

/**
 * The adapter for reference node.
 *
 * @author <a href="mailto:aplotnikov@codenvy.com">Andrey Plotnikov</a>
 */
public class ReferenceTreeNodeDataAdapter implements NodeDataAdapter<Reference> {
    private HashMap<Reference, TreeNodeElement<Reference>> treeNodeElements = new HashMap<Reference, TreeNodeElement<Reference>>();

    /** {@inheritDoc} */
    @Override
    public int compare(Reference a, Reference b) {
        return a.getDisplayName().compareTo(b.getDisplayName());
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasChildren(Reference data) {
        return data.getBranches() != null && !data.getBranches().isEmpty();
    }

    /** {@inheritDoc} */
    @Override
    public List<Reference> getChildren(Reference data) {
        return data.getBranches();
    }

    /** {@inheritDoc} */
    @Override
    public String getNodeId(Reference data) {
        return data.getFullName();
    }

    /** {@inheritDoc} */
    @Override
    public String getNodeName(Reference data) {
        return data.getDisplayName();
    }

    /** {@inheritDoc} */
    @Override
    public Reference getParent(Reference data) {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public TreeNodeElement<Reference> getRenderedTreeNode(Reference data) {
        return treeNodeElements.get(data);
    }

    /** {@inheritDoc} */
    @Override
    public void setNodeName(Reference data, String name) {
        // do nothing
    }

    /** {@inheritDoc} */
    @Override
    public void setRenderedTreeNode(Reference data, TreeNodeElement<Reference> renderedNode) {
        treeNodeElements.put(data, renderedNode);
    }

    /** {@inheritDoc} */
    @Override
    public Reference getDragDropTarget(Reference data) {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public List<String> getNodePath(Reference data) {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Reference getNodeByPath(Reference root, List<String> relativeNodePath) {
        return null;
    }

}
