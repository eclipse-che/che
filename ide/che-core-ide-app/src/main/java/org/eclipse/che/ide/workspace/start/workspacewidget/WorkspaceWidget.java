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
package org.eclipse.che.ide.workspace.start.workspacewidget;

import org.eclipse.che.api.workspace.shared.dto.WorkspaceDto;
import org.eclipse.che.ide.api.mvp.View;

/**
 * Provides methods which allows change view representation of workspace widget.
 *
 * @author Dmitry Shnurenko
 */
public interface WorkspaceWidget extends View<WorkspaceWidget.ActionDelegate> {

    interface ActionDelegate {
        /**
         * Performs some actions when user clicks on workspace widget.
         *
         * @param workspace
         *         workspace which was selected
         */
        void onWorkspaceSelected(WorkspaceDto workspace);
    }
}
