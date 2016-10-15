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
package org.eclipse.che.api.factory.server.jpa;

import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.persist.jpa.JpaPersistModule;

import org.eclipse.che.api.core.jdbc.jpa.eclipselink.EntityListenerInjectionManagerInitializer;
import org.eclipse.che.api.core.jdbc.jpa.guice.JpaInitializer;
import org.eclipse.che.api.factory.server.model.impl.FactoryImpl;
import org.eclipse.che.api.factory.server.spi.FactoryDao;
import org.eclipse.che.api.user.server.jpa.JpaProfileDao;
import org.eclipse.che.api.user.server.jpa.JpaUserDao;
import org.eclipse.che.api.user.server.model.impl.ProfileImpl;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.api.user.server.spi.ProfileDao;
import org.eclipse.che.api.user.server.spi.UserDao;
import org.eclipse.che.commons.test.tck.TckModule;
import org.eclipse.che.commons.test.tck.repository.JpaTckRepository;
import org.eclipse.che.commons.test.tck.repository.TckRepository;
import org.eclipse.che.security.PasswordEncryptor;
import org.eclipse.che.security.SHA512PasswordEncryptor;

/**
 * @author Anton Korneta
 */
public class JpaTckModule extends TckModule {

    @Override
    protected void configure() {
        install(new JpaPersistModule("main"));
        bind(JpaInitializer.class).asEagerSingleton();
        bind(EntityListenerInjectionManagerInitializer.class).asEagerSingleton();

        bind(new TypeLiteral<TckRepository<UserImpl>>() {}).toInstance(new JpaTckRepository<>(UserImpl.class));
        bind(new TypeLiteral<TckRepository<FactoryImpl>>() {}).toInstance(new JpaTckRepository<>(FactoryImpl.class));
        bind(new TypeLiteral<TckRepository<ProfileImpl>>() {}).toInstance(new JpaTckRepository<>(ProfileImpl.class));


        bind(UserDao.class).to(JpaUserDao.class);
        bind(ProfileDao.class).to(JpaProfileDao.class);
        bind(FactoryDao.class).to(JpaFactoryDao.class);

        bind(PasswordEncryptor.class).to(SHA512PasswordEncryptor.class).in(Singleton.class);
    }
}
