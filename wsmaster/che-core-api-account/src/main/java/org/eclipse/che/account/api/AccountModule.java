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
package org.eclipse.che.account.api;

import org.eclipse.che.account.spi.AccountDao;
import org.eclipse.che.account.spi.jpa.JpaAccountDao;

import com.google.inject.AbstractModule;

/**
 * @author Sergii Leschenko
 */
public class AccountModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(AccountDao.class).to(JpaAccountDao.class);
    }
}
