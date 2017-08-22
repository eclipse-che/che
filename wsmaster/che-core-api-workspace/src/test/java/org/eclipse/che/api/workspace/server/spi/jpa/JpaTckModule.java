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
package org.eclipse.che.api.workspace.server.spi.jpa;

import static org.eclipse.che.commons.test.db.H2TestHelper.inMemoryDefault;

import com.google.inject.TypeLiteral;
import com.google.inject.persist.jpa.JpaPersistModule;
import org.eclipse.che.account.spi.AccountImpl;
import org.eclipse.che.api.permission.server.AbstractPermissionsDomain;
import org.eclipse.che.api.permission.server.spi.PermissionsDao;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkerImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.api.workspace.server.model.impl.stack.StackImpl;
import org.eclipse.che.api.workspace.server.spi.WorkerDao;
import org.eclipse.che.api.workspace.server.spi.tck.StackPermissionsDaoTest;
import org.eclipse.che.api.workspace.server.spi.tck.WorkerDaoTest;
import org.eclipse.che.api.workspace.server.stack.StackPermissionsImpl;
import org.eclipse.che.commons.test.db.H2JpaCleaner;
import org.eclipse.che.commons.test.tck.TckModule;
import org.eclipse.che.commons.test.tck.TckResourcesCleaner;
import org.eclipse.che.commons.test.tck.repository.JpaTckRepository;
import org.eclipse.che.commons.test.tck.repository.TckRepository;
import org.eclipse.che.core.db.DBInitializer;
import org.eclipse.che.core.db.schema.SchemaInitializer;
import org.eclipse.che.core.db.schema.impl.flyway.FlywaySchemaInitializer;

/** @author Yevhenii Voevodin */
public class JpaTckModule extends TckModule {

  @Override
  protected void configure() {
    bind(new TypeLiteral<AbstractPermissionsDomain<StackPermissionsImpl>>() {})
        .to(StackPermissionsDaoTest.TestDomain.class);
    bind(new TypeLiteral<PermissionsDao<StackPermissionsImpl>>() {})
        .to(JpaStackPermissionsDao.class);
    bind(new TypeLiteral<TckRepository<StackPermissionsImpl>>() {})
        .toInstance(new JpaTckRepository<>(StackPermissionsImpl.class));
    bind(new TypeLiteral<TckRepository<StackImpl>>() {})
        .toInstance(new JpaTckRepository<>(StackImpl.class));

    bind(new TypeLiteral<AbstractPermissionsDomain<WorkerImpl>>() {})
        .to(WorkerDaoTest.TestDomain.class);

    bind(WorkerDao.class).to(JpaWorkerDao.class);
    bind(new TypeLiteral<TckRepository<WorkerImpl>>() {})
        .toInstance(new JpaTckRepository<>(WorkerImpl.class));
    bind(new TypeLiteral<TckRepository<UserImpl>>() {})
        .toInstance(new JpaTckRepository<>(UserImpl.class));
    bind(new TypeLiteral<TckRepository<AccountImpl>>() {})
        .toInstance(new JpaTckRepository<>(AccountImpl.class));

    bind(new TypeLiteral<TckRepository<WorkspaceImpl>>() {})
        .toInstance(new JpaTckRepository<>(WorkspaceImpl.class));

    install(new JpaPersistModule("main"));
    bind(SchemaInitializer.class)
        .toInstance(new FlywaySchemaInitializer(inMemoryDefault(), "che-schema"));
    bind(DBInitializer.class).asEagerSingleton();
    bind(TckResourcesCleaner.class).to(H2JpaCleaner.class);
  }
}
