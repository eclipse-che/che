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
import org.eclipse.che.api.workspace.server.event.BeforeStackRemovedEvent;
import org.eclipse.che.api.workspace.server.model.impl.stack.StackImpl;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.PreRemove;

/**
 * Entity events listener for {@link StackImpl}.
 *
 * @author Max Shaposhnik
 */
@Singleton
public class StackEntityListener {

    @Inject
    private EventService eventService;

    @PreRemove
    private void preRemove(StackImpl stack) {
        final BeforeStackRemovedEvent event = new BeforeStackRemovedEvent(stack);
        eventService.publish(event);
        if (event.getContext().isFailed()) {
            throw new CascadeRemovalException(event.getContext().getCause());
        }
    }
}
