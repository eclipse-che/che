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
package org.eclipse.che.ide.part.editor.recent;

import org.eclipse.che.ide.api.recent.RecentList;
import org.eclipse.che.ide.api.resources.File;

/**
 * Extension for the recent list which process recent file lists.
 *
 * @author Vlad Zhukovskiy
 */
public interface RecentFileList extends RecentList<File> {
    /**
     * Return recent file list user dialog.
     *
     * @return user dialog with recent file list
     */
    OpenRecentFilesPresenter getRecentViewDialog();
}
