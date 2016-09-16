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

import org.eclipse.che.api.user.server.model.impl.UserImpl;

/**
 * Published after {@link UserImpl user} is persisted.
 *
 * @author Max Shaposhnik
 */
public class AfterUserPersistedEvent {

    private final UserImpl user;

    public AfterUserPersistedEvent(UserImpl user) {
        this.user = user;
    }

    /** Returns user which is going to be removed. */
    public UserImpl getUser() {
        return user;
    }
}
