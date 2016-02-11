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
package org.eclipse.che.ide.part.editor.recent;

import org.eclipse.che.ide.project.node.FileReferenceNode;

/**
 * Factory for the recent files action.
 *
 * @author Vlad Zhukovskiy
 */
public interface RecentFileActionFactory {
    /**
     * Creates new recent file action to show in main menu.
     *
     * @param file
     *         file associated with
     * @return action
     */
    RecentFileAction newRecentFileAction(FileReferenceNode file);
}
