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
package org.eclipse.che.api.user.server.jpa;

import org.eclipse.che.core.db.jpa.CascadeRemovalException;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.user.server.event.BeforeUserPersistedEvent;
import org.eclipse.che.api.user.server.event.BeforeUserRemovedEvent;
import org.eclipse.che.api.user.server.model.impl.UserImpl;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;

/**
 * Callback for {@link UserImpl user} jpa related events.
 *
 * @author Yevhenii Voevodin
 */
@Singleton
public class UserEntityListener {

    @Inject
    private EventService eventService;

    @PreRemove
    public void preRemove(UserImpl user) {
        final BeforeUserRemovedEvent event = new BeforeUserRemovedEvent(user);
        eventService.publish(event);
        if (event.getContext().isFailed()) {
            throw new CascadeRemovalException(event.getContext().getCause());
        }
    }

    @PrePersist
    public void prePersist(UserImpl user) {
        eventService.publish(new BeforeUserPersistedEvent(user));
    }
}
