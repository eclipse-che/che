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
package org.eclipse.che.ide.extension.machine.client.processes.actions;

import org.eclipse.che.ide.extension.machine.client.processes.ProcessTreeNode;
import org.eclipse.che.ide.ui.tree.TreeNodeElement;

/**
 * Consoles tree context menu factory.
 *
 * @author Vitaliy Guliy
 */
public interface ConsoleTreeContextMenuFactory {

    /**
     * Creates new context menu for consoles tree.
     *
     * @param processTreeNode
     * @return new context menu
     */
    ConsoleTreeContextMenu newContextMenu(ProcessTreeNode processTreeNode);

}
