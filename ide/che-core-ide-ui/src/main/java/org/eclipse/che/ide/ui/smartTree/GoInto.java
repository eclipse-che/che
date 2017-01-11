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
package org.eclipse.che.ide.ui.smartTree;

import org.eclipse.che.ide.api.data.tree.Node;
import org.eclipse.che.ide.ui.smartTree.event.GoIntoStateEvent.HasGoIntoStateHandlers;

/**
 * Go Into controller for specified tree.
 * By Go Into operation it means that tree isolates selected node if last one's support this feature.
 *
 * @author Vlad Zhukovskyi
 * @see Node#supportGoInto()
 */
public interface GoInto extends HasGoIntoStateHandlers {
    /**
     * Bind specified tree to Go Into processor.
     *
     * @param tree
     *         instance of {@link Tree}
     */
    void bind(Tree tree);

    /**
     * Try to setup Go Into mode on specified node if last on's support it.
     *
     * @param node
     *         node to perform Go Into
     * @return true if Go Into was activated, otherwise false
     */
    boolean activate(Node node);

    /**
     * Returns true if current tree is in Go Into mode now.
     *
     * @return true if it is, otherwise false
     */
    boolean isActive();

    /**
     * Resets Go Into mode. Method will do nothing if Go Into mode wasn't activated.
     */
    void reset();

    /**
     * Returns last used node for Go Into or null if no node was used.
     *
     * @return {@link Node} or null
     */
    Node getLastUsed();
}
