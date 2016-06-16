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
package org.eclipse.che.ide.api.event;

import org.eclipse.che.ide.api.project.tree.TreeNode;
import com.google.gwt.event.shared.GwtEvent;

/**
 * Event that describes the fact that node has been changed.
 *
 * @author Artem Zatsarynnyi
 * @deprecated tree performs renaming data objects in nodes by self,
 * so this event is useless after new tree implementation
 */
@Deprecated
public class NodeChangedEvent extends GwtEvent<NodeChangedHandler> {

    /** Type class used to register this event. */
    public static Type<NodeChangedHandler> TYPE = new Type<>();
    private final TreeNode<?> node;
    private final NodeAction  nodeAction;

    /**
     * Create new {@link NodeChangedEvent}.
     *
     * @param node
     *         the node that was changed
     * @param nodeAction
     *         the type of change
     */
    protected NodeChangedEvent(TreeNode<?> node, NodeAction nodeAction) {
        this.node = node;
        this.nodeAction = nodeAction;
    }

    /**
     * Create new {@link NodeChangedEvent}.
     *
     * @param node
     *         the node that was renamed
     * @return new {@link NodeChangedEvent}
     */
    public static NodeChangedEvent createNodeRenamedEvent(TreeNode<?> node) {
        return new NodeChangedEvent(node, NodeAction.RENAMED);
    }

    @Override
    public Type<NodeChangedHandler> getAssociatedType() {
        return TYPE;
    }

    public TreeNode<?> getNode() {
        return node;
    }

    public NodeAction getNodeAction() {
        return nodeAction;
    }

    /** {@inheritDoc} */
    @Override
    protected void dispatch(NodeChangedHandler handler) {
        switch (nodeAction) {
            case RENAMED:
                handler.onNodeRenamed(this);
                break;
            default:
                break;
        }
    }

    public static enum NodeAction {
        RENAMED
    }
}
