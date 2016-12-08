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
package org.eclipse.che.api.workspace.server.jpa;

import org.eclipse.che.core.db.jpa.CascadeRemovalException;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.workspace.server.event.BeforeWorkspaceRemovedEvent;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.PreRemove;

/**
 * Callback for {@link WorkspaceImpl workspace} jpa related events.
 *
 * @author Yevhenii Voevodin
 */
@Singleton
public class WorkspaceEntityListener {

    @Inject
    private EventService eventService;

    @PreRemove
    private void preRemove(WorkspaceImpl workspace) {
        final BeforeWorkspaceRemovedEvent event = new BeforeWorkspaceRemovedEvent(workspace);
        eventService.publish(event);
        if (event.getContext().isFailed()) {
            throw new CascadeRemovalException(event.getContext().getCause());
        }
    }
}
