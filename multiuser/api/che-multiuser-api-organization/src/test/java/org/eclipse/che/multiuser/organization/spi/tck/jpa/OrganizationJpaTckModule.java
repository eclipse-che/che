/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.multiuser.organization.spi.tck.jpa;

import com.google.inject.TypeLiteral;
import com.google.inject.persist.jpa.JpaPersistModule;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.commons.test.db.H2DBTestServer;
import org.eclipse.che.commons.test.db.H2JpaCleaner;
import org.eclipse.che.commons.test.tck.TckModule;
import org.eclipse.che.commons.test.tck.TckResourcesCleaner;
import org.eclipse.che.commons.test.tck.repository.JpaTckRepository;
import org.eclipse.che.commons.test.tck.repository.TckRepository;
import org.eclipse.che.core.db.DBInitializer;
import org.eclipse.che.core.db.schema.SchemaInitializer;
import org.eclipse.che.core.db.schema.impl.flyway.FlywaySchemaInitializer;
import org.eclipse.che.multiuser.api.permission.server.AbstractPermissionsDomain;
import org.eclipse.che.multiuser.organization.api.permissions.OrganizationDomain;
import org.eclipse.che.multiuser.organization.spi.MemberDao;
import org.eclipse.che.multiuser.organization.spi.OrganizationDao;
import org.eclipse.che.multiuser.organization.spi.OrganizationDistributedResourcesDao;
import org.eclipse.che.multiuser.organization.spi.impl.MemberImpl;
import org.eclipse.che.multiuser.organization.spi.impl.OrganizationDistributedResourcesImpl;
import org.eclipse.che.multiuser.organization.spi.impl.OrganizationImpl;
import org.eclipse.che.multiuser.organization.spi.jpa.JpaMemberDao;
import org.eclipse.che.multiuser.organization.spi.jpa.JpaOrganizationDao;
import org.eclipse.che.multiuser.organization.spi.jpa.JpaOrganizationDistributedResourcesDao;
import org.eclipse.che.multiuser.organization.spi.jpa.JpaOrganizationImplTckRepository;

/** @author Sergii Leschenko */
public class OrganizationJpaTckModule extends TckModule {

  @Override
  protected void configure() {
    install(new JpaPersistModule("main"));
    H2DBTestServer server = H2DBTestServer.startDefault();
    bind(SchemaInitializer.class)
        .toInstance(new FlywaySchemaInitializer(server.getDataSource(), "che-schema"));
    bind(DBInitializer.class).asEagerSingleton();
    bind(TckResourcesCleaner.class).toInstance(new H2JpaCleaner(server));

    bind(new TypeLiteral<AbstractPermissionsDomain<MemberImpl>>() {}).to(OrganizationDomain.class);

    bind(new TypeLiteral<TckRepository<OrganizationImpl>>() {})
        .to(JpaOrganizationImplTckRepository.class);
    bind(new TypeLiteral<TckRepository<UserImpl>>() {})
        .toInstance(new JpaTckRepository<>(UserImpl.class));
    bind(new TypeLiteral<TckRepository<MemberImpl>>() {})
        .toInstance(new JpaTckRepository<>(MemberImpl.class));
    bind(new TypeLiteral<TckRepository<OrganizationDistributedResourcesImpl>>() {})
        .toInstance(new JpaTckRepository<>(OrganizationDistributedResourcesImpl.class));

    bind(OrganizationDao.class).to(JpaOrganizationDao.class);
    bind(MemberDao.class).to(JpaMemberDao.class);

    bind(OrganizationDistributedResourcesDao.class)
        .to(JpaOrganizationDistributedResourcesDao.class);
  }
}
