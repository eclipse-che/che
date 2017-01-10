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
package org.eclipse.che.ide.project.node;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import org.eclipse.che.ide.api.data.HasDataObject;
import org.eclipse.che.ide.api.data.tree.AbstractTreeNode;
import org.eclipse.che.ide.api.data.tree.settings.HasSettings;
import org.eclipse.che.ide.api.data.tree.settings.NodeSettings;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.ui.smartTree.presentation.HasPresentation;
import org.eclipse.che.ide.ui.smartTree.presentation.NodePresentation;

/**
 * Base class for the synthetic, non-resourced item
 *
 * @author Vlad Zhukovskiy
 */
public abstract class SyntheticNode<D> extends AbstractTreeNode implements HasDataObject<D>, HasPresentation, HasSettings {

    private D                data;
    private NodeSettings     nodeSettings;
    private NodePresentation nodePresentation;

    public static final String CUSTOM_BACKGROUND_FILL = "fill";

    public SyntheticNode(D data, NodeSettings nodeSettings) {
        this.data = data;
        this.nodeSettings = nodeSettings;
    }

    /** {@inheritDoc} */
    @Override
    public NodeSettings getSettings() {
        return nodeSettings;
    }

    public Path getProject() {
        return Path.EMPTY;
    }

    /** {@inheritDoc} */
    @Override
    public D getData() {
        return data;
    }

    /** {@inheritDoc} */
    @Override
    public void setData(D data) {
        this.data = data;
    }

    /** {@inheritDoc} */
    @Override
    public final NodePresentation getPresentation(boolean update) {
        if (nodePresentation == null) {
            nodePresentation = new NodePresentation();
            updatePresentation(nodePresentation);
        }

        if (update) {
            updatePresentation(nodePresentation);
        }
        return nodePresentation;
    }

    /** {@inheritDoc} */
    @Override
    public final boolean supportGoInto() {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof SyntheticNode)) {
            return false;
        }

        SyntheticNode node = (SyntheticNode)o;

        return node.getData().equals(getData()) &&  node.getProject().equals(getProject());
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return Objects.hashCode(data, getProject());
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                          .add("data", data)
                          .add("project", getProject())
                          .toString();
    }
}
