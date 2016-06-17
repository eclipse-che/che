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

import com.google.gwt.event.shared.EventHandler;
import org.eclipse.che.ide.api.project.tree.TreeNode;

/**
 *
 * Handler for {@link RenameNodeEvent}
 *
 * @author Alexander Andrienko
 * @deprecated event should be removed because renaming node is useless in current state
 */
@Deprecated
public interface RenameNodeEventHandler extends EventHandler {

    /**
     * Updates data for renamed node subTree
     * @param newParentNodePath new path
     * @param parentNode parent Node which was renamed
     */
    void onNodeRenamed(TreeNode<?> parentNode, String newParentNodePath);
}
