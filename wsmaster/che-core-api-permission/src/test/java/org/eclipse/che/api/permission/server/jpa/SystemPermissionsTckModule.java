/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.permission.server.jpa;

import static org.eclipse.che.commons.test.db.H2TestHelper.inMemoryDefault;

import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import com.google.inject.persist.jpa.JpaPersistModule;
import org.eclipse.che.api.permission.server.AbstractPermissionsDomain;
import org.eclipse.che.api.permission.server.SystemDomain;
import org.eclipse.che.api.permission.server.model.impl.SystemPermissionsImpl;
import org.eclipse.che.api.permission.server.spi.PermissionsDao;
import org.eclipse.che.api.permission.server.spi.tck.SystemPermissionsDaoTest;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.commons.test.db.H2JpaCleaner;
import org.eclipse.che.commons.test.tck.TckModule;
import org.eclipse.che.commons.test.tck.TckResourcesCleaner;
import org.eclipse.che.commons.test.tck.repository.JpaTckRepository;
import org.eclipse.che.commons.test.tck.repository.TckRepository;
import org.eclipse.che.core.db.DBInitializer;
import org.eclipse.che.core.db.schema.SchemaInitializer;
import org.eclipse.che.core.db.schema.impl.flyway.FlywaySchemaInitializer;

/** @author Max Shaposhnik (mshaposhnik@codenvy.com) */
public class SystemPermissionsTckModule extends TckModule {

  @Override
  protected void configure() {
    //Creates empty multibinder to avoid error during container starting
    Multibinder.newSetBinder(
        binder(), String.class, Names.named(SystemDomain.SYSTEM_DOMAIN_ACTIONS));

    bind(new TypeLiteral<AbstractPermissionsDomain<SystemPermissionsImpl>>() {})
        .to(SystemPermissionsDaoTest.TestDomain.class);
    bind(new TypeLiteral<PermissionsDao<SystemPermissionsImpl>>() {})
        .to(JpaSystemPermissionsDao.class);

    bind(new TypeLiteral<TckRepository<SystemPermissionsImpl>>() {})
        .toInstance(new JpaTckRepository<>(SystemPermissionsImpl.class));
    bind(new TypeLiteral<TckRepository<UserImpl>>() {})
        .toInstance(new JpaTckRepository<>(UserImpl.class));

    install(new JpaPersistModule("main"));
    bind(SchemaInitializer.class)
        .toInstance(new FlywaySchemaInitializer(inMemoryDefault(), "che-schema"));
    bind(DBInitializer.class).asEagerSingleton();
    bind(TckResourcesCleaner.class).to(H2JpaCleaner.class);
  }
}
