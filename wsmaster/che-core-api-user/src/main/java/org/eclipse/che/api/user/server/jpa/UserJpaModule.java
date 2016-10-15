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

import com.google.inject.AbstractModule;

import org.eclipse.che.api.user.server.jpa.JpaPreferenceDao.RemovePreferencesBeforeUserRemovedEventSubscriber;
import org.eclipse.che.api.user.server.jpa.JpaProfileDao.RemoveProfileBeforeUserRemovedEventSubscriber;
import org.eclipse.che.api.user.server.spi.PreferenceDao;
import org.eclipse.che.api.user.server.spi.ProfileDao;
import org.eclipse.che.api.user.server.spi.UserDao;
import org.eclipse.che.security.PBKDF2PasswordEncryptor;
import org.eclipse.che.security.PasswordEncryptor;

/**
 * @author Yevhenii Voevodin
 */
public class UserJpaModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(PasswordEncryptor.class).to(PBKDF2PasswordEncryptor.class);
        bind(UserDao.class).to(JpaUserDao.class);
        bind(ProfileDao.class).to(JpaProfileDao.class);
        bind(PreferenceDao.class).to(JpaPreferenceDao.class);
        bind(RemoveProfileBeforeUserRemovedEventSubscriber.class).asEagerSingleton();
        bind(RemovePreferencesBeforeUserRemovedEventSubscriber.class).asEagerSingleton();
    }
}
