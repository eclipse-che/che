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
package org.eclipse.che.ide.workspace;

import com.google.gwt.user.client.ui.AcceptsOneWidget;

import org.eclipse.che.ide.api.mvp.View;

/**
 * The view of {@link WorkspacePresenter}.
 *
 * @author Andrey Plotnikov
 * @author Dmitry Shnurenko
 */
public interface WorkspaceView extends View<WorkspaceView.ActionDelegate> {
    /** Required for delegating functions in the view. */
    interface ActionDelegate {
    }

    /** @return central panel */
    AcceptsOneWidget getPerspectivePanel();

    /** @return menu panel */
    AcceptsOneWidget getMenuPanel();

    /** @return toolbar panel */
    AcceptsOneWidget getToolbarPanel();

    /**
     * Returns status panel ( an information panel located under actions panel )
     *
     * @return status panel
     */
    AcceptsOneWidget getStatusPanel();
}
