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
package org.eclipse.che.account.event;

import org.eclipse.che.account.spi.AccountImpl;
import org.eclipse.che.core.db.event.CascadeRemovalEvent;

/**
 * Published before {@link AccountImpl account} removed.
 *
 * @author Antona Korneta
 */
public class BeforeAccountRemovedEvent extends CascadeRemovalEvent {

    private final AccountImpl account;

    public BeforeAccountRemovedEvent(AccountImpl account) {
        this.account = account;
    }

    /** Returns account which is going to be removed. */
    public AccountImpl getAccount() {
        return account;
    }
}
