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
package org.eclipse.che.api.user.server.event;

import org.eclipse.che.api.core.notification.EventOrigin;
import org.eclipse.che.api.user.server.model.impl.UserImpl;

/**
 * Published after {@link UserImpl user} removed.
 *
 * @author Sergii Kabashniuk
 */
@EventOrigin("user")
public class PostUserRemovedEvent {

    private final String userId;

    public PostUserRemovedEvent(String userId) {
        this.userId = userId;
    }

    /** Returns id of removed user*/
    public String getUserId() {
        return userId;
    }
}
