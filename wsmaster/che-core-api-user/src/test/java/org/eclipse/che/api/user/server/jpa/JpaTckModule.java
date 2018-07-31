/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.user.server.jpa;

import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import java.util.Map;
import org.eclipse.che.account.spi.AccountImpl;
import org.eclipse.che.api.user.server.model.impl.ProfileImpl;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.api.user.server.spi.PreferenceDao;
import org.eclipse.che.api.user.server.spi.ProfileDao;
import org.eclipse.che.api.user.server.spi.UserDao;
import org.eclipse.che.commons.lang.Pair;
import org.eclipse.che.commons.test.db.H2DBTestServer;
import org.eclipse.che.commons.test.db.H2JpaCleaner;
import org.eclipse.che.commons.test.db.PersistTestModuleBuilder;
import org.eclipse.che.commons.test.tck.TckModule;
import org.eclipse.che.commons.test.tck.TckResourcesCleaner;
import org.eclipse.che.commons.test.tck.repository.JpaTckRepository;
import org.eclipse.che.commons.test.tck.repository.TckRepository;
import org.eclipse.che.core.db.DBInitializer;
import org.eclipse.che.core.db.h2.jpa.eclipselink.H2ExceptionHandler;
import org.eclipse.che.core.db.schema.SchemaInitializer;
import org.eclipse.che.core.db.schema.impl.flyway.FlywaySchemaInitializer;
import org.eclipse.che.security.PasswordEncryptor;
import org.eclipse.che.security.SHA512PasswordEncryptor;
import org.h2.Driver;

/** @author Yevhenii Voevodin */
public class JpaTckModule extends TckModule {

  @Override
  protected void configure() {
    H2DBTestServer server = H2DBTestServer.startDefault();
    install(
        new PersistTestModuleBuilder()
            .setDriver(Driver.class)
            .runningOn(server)
            .addEntityClasses(
                UserImpl.class, ProfileImpl.class, PreferenceEntity.class, AccountImpl.class)
            .setExceptionHandler(H2ExceptionHandler.class)
            .build());
    bind(DBInitializer.class).asEagerSingleton();
    bind(SchemaInitializer.class)
        .toInstance(new FlywaySchemaInitializer(server.getDataSource(), "che-schema"));
    bind(TckResourcesCleaner.class).toInstance(new H2JpaCleaner(server.getDataSource()));

    bind(new TypeLiteral<TckRepository<UserImpl>>() {}).to(UserJpaTckRepository.class);
    bind(new TypeLiteral<TckRepository<ProfileImpl>>() {})
        .toInstance(new JpaTckRepository<>(ProfileImpl.class));
    bind(new TypeLiteral<TckRepository<Pair<String, Map<String, String>>>>() {})
        .to(PreferenceJpaTckRepository.class);

    bind(UserDao.class).to(JpaUserDao.class);
    bind(ProfileDao.class).to(JpaProfileDao.class);
    bind(PreferenceDao.class).to(JpaPreferenceDao.class);
    // SHA-512 encryptor is faster than PBKDF2 so it is better for testing
    bind(PasswordEncryptor.class).to(SHA512PasswordEncryptor.class).in(Singleton.class);
  }
}
