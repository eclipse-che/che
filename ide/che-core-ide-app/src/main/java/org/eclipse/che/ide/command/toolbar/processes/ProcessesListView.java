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
package org.eclipse.che.ide.command.toolbar.processes;

import org.eclipse.che.ide.api.mvp.View;

/** View for {@link ProcessesListPresenter}. */
public interface ProcessesListView extends View<ProcessesListView.ActionDelegate> {

    /** Add process to the list. */
    void addProcess(Process process);

    /** Remove process from the list. */
    void removeProcess(Process process);

    /** Clear processes list. */
    void clearList();

    /** Informs view that the specified {@code process} has been stopped. */
    void processStopped(Process process);

    interface ActionDelegate {

        /** Called when process has been chosen. */
        void onProcessChosen(Process process);

        /** Called when rerunning process is requested. */
        void onReRunProcess(Process process);

        /** Called when stopping process is requested. */
        void onStopProcess(Process process);

        /** Called when new command creation is requested. */
        void onCreateCommand();
    }
}
