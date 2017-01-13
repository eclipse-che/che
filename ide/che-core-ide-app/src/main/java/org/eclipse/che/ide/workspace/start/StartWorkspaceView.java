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
package org.eclipse.che.ide.workspace.start;

import com.google.inject.ImplementedBy;

import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.ide.workspace.start.workspacewidget.WorkspaceWidget;

/**
 * Provides methods which allows selecting and starting available workspaces.
 *
 * @author Dmitry Shnurenko
 */
@ImplementedBy(StartWorkspaceViewImpl.class)
interface StartWorkspaceView extends View<StartWorkspaceView.ActionDelegate> {

    /**
     * Changes enabling button.
     *
     * @param enable
     *         <code>true</code> button is enable, <code>false</code> button is disable
     */
    void setEnableStartButton(boolean enable);

    /**
     * Adds workspace widget to panel to display it.
     *
     * @param workspace
     *         widget which will be added
     */
    void addWorkspace(WorkspaceWidget workspace);

    /**
     * Sets workspace name to text box.
     *
     * @param wsName
     *         name which will be set
     */
    void setWsName(String wsName);

    /** Shows dialog window. */
    void show();

    /** Hides dialog window. */
    void hide();

    /** Clears panel which contains workspace widgets. */
    void clearWorkspacesPanel();

    interface ActionDelegate {
        void onStartWorkspaceClicked();

        void onCreateWorkspaceClicked();
    }
}
