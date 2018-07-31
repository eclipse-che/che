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
package org.eclipse.che.multiuser.resource.spi.tck.jpa;

import com.google.inject.TypeLiteral;
import com.google.inject.persist.jpa.JpaPersistModule;
import org.eclipse.che.account.spi.AccountImpl;
import org.eclipse.che.commons.test.db.H2DBTestServer;
import org.eclipse.che.commons.test.db.H2JpaCleaner;
import org.eclipse.che.commons.test.tck.TckModule;
import org.eclipse.che.commons.test.tck.TckResourcesCleaner;
import org.eclipse.che.commons.test.tck.repository.JpaTckRepository;
import org.eclipse.che.commons.test.tck.repository.TckRepository;
import org.eclipse.che.core.db.DBInitializer;
import org.eclipse.che.core.db.schema.SchemaInitializer;
import org.eclipse.che.core.db.schema.impl.flyway.FlywaySchemaInitializer;
import org.eclipse.che.multiuser.resource.spi.FreeResourcesLimitDao;
import org.eclipse.che.multiuser.resource.spi.impl.FreeResourcesLimitImpl;
import org.eclipse.che.multiuser.resource.spi.jpa.JpaFreeResourcesLimitDao;

/** @author Sergii Leschenko */
public class ResourceTckModule extends TckModule {

  @Override
  protected void configure() {
    install(new JpaPersistModule("main"));
    H2DBTestServer server = H2DBTestServer.startDefault();
    bind(SchemaInitializer.class)
        .toInstance(new FlywaySchemaInitializer(server.getDataSource(), "che-schema"));
    bind(DBInitializer.class).asEagerSingleton();
    bind(TckResourcesCleaner.class).toInstance(new H2JpaCleaner(server));

    bind(new TypeLiteral<TckRepository<FreeResourcesLimitImpl>>() {})
        .toInstance(new JpaTckRepository<>(FreeResourcesLimitImpl.class));
    bind(new TypeLiteral<TckRepository<AccountImpl>>() {})
        .toInstance(new JpaTckRepository<>(AccountImpl.class));

    bind(FreeResourcesLimitDao.class).to(JpaFreeResourcesLimitDao.class);
  }
}
