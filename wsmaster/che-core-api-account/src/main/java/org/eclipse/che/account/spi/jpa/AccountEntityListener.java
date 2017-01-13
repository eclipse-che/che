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
package org.eclipse.che.account.spi.jpa;


import org.eclipse.che.account.event.BeforeAccountRemovedEvent;
import org.eclipse.che.account.spi.AccountImpl;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.notification.EventService;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.PreRemove;

/**
 * Callback for {@link AccountImpl account} jpa related events.
 *
 * @author Anton Korneta.
 */
@Singleton
public class AccountEntityListener {

    @Inject
    private EventService eventService;

    @PreRemove
    private void preRemove(AccountImpl account) throws ServerException {
        eventService.publish(new BeforeAccountRemovedEvent(account)).propagateException();
    }
}
