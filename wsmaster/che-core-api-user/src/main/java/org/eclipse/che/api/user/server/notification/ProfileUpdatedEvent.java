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
package org.eclipse.che.api.user.server.notification;

import javax.inject.Inject;

import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.user.server.dao.Profile;

/**
 * Event which should be thrown thought {@link EventService} after update user {@link Profile}
 *
 * @author Alexander Andrienko
 */
public class ProfileUpdatedEvent {

    private final Profile profile;

    @Inject
    public ProfileUpdatedEvent(Profile profile) {
        this.profile = new Profile().withId(profile.getId()).withUserId(profile.getUserId()).withAttributes(profile.getAttributes());
    }

    /**
      * Returns copy updated profile
     */
    public Profile getProfile() {
        return profile;
    }

    @Override
    public String toString() {
        return "ProfileUpdatedEvent{" +
               "profile=" + profile +
               '}';
    }
}
