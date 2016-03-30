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
package org.eclipse.che.api.workspace.server.event;

import org.eclipse.che.api.core.notification.EventOrigin;

/**
 * Informs that workspace with certain id was removed.
 *
 * @author Sergii Leschenko
 */
@EventOrigin("workspace")
public class WorkspaceRemovedEvent {
    private final String workspaceId;

    public WorkspaceRemovedEvent(String workspaceId) {
        this.workspaceId = workspaceId;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }
}
