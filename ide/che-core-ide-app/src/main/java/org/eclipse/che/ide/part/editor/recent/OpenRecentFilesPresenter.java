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

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.project.node.FileReferenceNode;

import java.util.List;

/**
 * Presenter for showing recently opened files.
 *
 * @author Vlad Zhukovskiy
 */
@Singleton
public class OpenRecentFilesPresenter implements OpenRecentFilesView.ActionDelegate {

    private final OpenRecentFilesView view;

    @Inject
    public OpenRecentFilesPresenter(OpenRecentFilesView view) {
        this.view = view;

        view.setDelegate(this);
    }

    /**
     * Show dialog.
     */
    public void show() {
        view.show();
    }

    /**
     * Set recent file list.
     *
     * @param recentFiles
     *         recent file list
     */
    public void setRecentFiles(List<FileReferenceNode> recentFiles) {
        view.setRecentFiles(recentFiles);
    }

    /**
     * Clear recent file list.
     */
    public void clearRecentFiles() {
        view.clearRecentFiles();
    }
}
