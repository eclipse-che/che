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

import org.eclipse.che.core.db.event.CascadeRemovalEvent;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;

/**
 * Published before {@link WorkspaceImpl workspace} removed.
 *
 * @author Yevhenii Voevodin
 */
public class BeforeWorkspaceRemovedEvent extends CascadeRemovalEvent {

    private final WorkspaceImpl workspace;

    public BeforeWorkspaceRemovedEvent(WorkspaceImpl workspace) {
        this.workspace = workspace;
    }

    public WorkspaceImpl getWorkspace() {
        return workspace;
    }
}
