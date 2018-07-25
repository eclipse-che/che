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
package org.eclipse.che.multiuser.permission.workspace.server.jpa;

import com.google.inject.TypeLiteral;
import java.util.Collection;
import org.eclipse.che.account.spi.AccountImpl;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.api.workspace.server.jpa.JpaStackDao;
import org.eclipse.che.api.workspace.server.jpa.JpaWorkspaceDao;
import org.eclipse.che.api.workspace.server.model.impl.CommandImpl;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
import org.eclipse.che.api.workspace.server.model.impl.MachineConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.ProjectConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.RecipeImpl;
import org.eclipse.che.api.workspace.server.model.impl.ServerConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.SourceStorageImpl;
import org.eclipse.che.api.workspace.server.model.impl.VolumeImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.api.workspace.server.model.impl.stack.StackImpl;
import org.eclipse.che.api.workspace.server.spi.StackDao;
import org.eclipse.che.api.workspace.server.spi.WorkspaceDao;
import org.eclipse.che.commons.test.db.H2DBTestServer;
import org.eclipse.che.commons.test.db.H2JpaCleaner;
import org.eclipse.che.commons.test.db.PersistTestModuleBuilder;
import org.eclipse.che.commons.test.tck.TckModule;
import org.eclipse.che.commons.test.tck.TckResourcesCleaner;
import org.eclipse.che.commons.test.tck.repository.JpaTckRepository;
import org.eclipse.che.commons.test.tck.repository.TckRepository;
import org.eclipse.che.commons.test.tck.repository.TckRepositoryException;
import org.eclipse.che.core.db.DBInitializer;
import org.eclipse.che.core.db.h2.jpa.eclipselink.H2ExceptionHandler;
import org.eclipse.che.core.db.schema.SchemaInitializer;
import org.eclipse.che.core.db.schema.impl.flyway.FlywaySchemaInitializer;
import org.eclipse.che.multiuser.api.permission.server.AbstractPermissionsDomain;
import org.eclipse.che.multiuser.api.permission.server.spi.PermissionsDao;
import org.eclipse.che.multiuser.permission.workspace.server.model.impl.WorkerImpl;
import org.eclipse.che.multiuser.permission.workspace.server.spi.WorkerDao;
import org.eclipse.che.multiuser.permission.workspace.server.spi.jpa.JpaStackPermissionsDao;
import org.eclipse.che.multiuser.permission.workspace.server.spi.jpa.JpaWorkerDao;
import org.eclipse.che.multiuser.permission.workspace.server.spi.tck.StackPermissionsDaoTest;
import org.eclipse.che.multiuser.permission.workspace.server.spi.tck.WorkerDaoTest;
import org.eclipse.che.multiuser.permission.workspace.server.stack.StackPermissionsImpl;
import org.h2.Driver;

/** @author Yevhenii Voevodin */
public class WorkspaceTckModule extends TckModule {

  @Override
  protected void configure() {
    H2DBTestServer server = H2DBTestServer.startDefault();
    install(
        new PersistTestModuleBuilder()
            .setDriver(Driver.class)
            .runningOn(server)
            .addEntityClasses(
                AccountImpl.class,
                UserImpl.class,
                WorkspaceImpl.class,
                WorkspaceConfigImpl.class,
                ProjectConfigImpl.class,
                EnvironmentImpl.class,
                StackPermissionsImpl.class,
                WorkerImpl.class,
                MachineConfigImpl.class,
                SourceStorageImpl.class,
                ServerConfigImpl.class,
                StackImpl.class,
                CommandImpl.class,
                RecipeImpl.class,
                VolumeImpl.class)
            .addEntityClass(
                "org.eclipse.che.api.workspace.server.model.impl.ProjectConfigImpl$Attribute")
            .setExceptionHandler(H2ExceptionHandler.class)
            .build());
    bind(DBInitializer.class).asEagerSingleton();
    bind(SchemaInitializer.class)
        .toInstance(new FlywaySchemaInitializer(server.getDataSource(), "che-schema"));
    bind(TckResourcesCleaner.class).toInstance(new H2JpaCleaner(server));

    bind(new TypeLiteral<TckRepository<AccountImpl>>() {})
        .toInstance(new JpaTckRepository<>(AccountImpl.class));
    bind(new TypeLiteral<TckRepository<WorkspaceImpl>>() {}).toInstance(new WorkspaceRepository());
    bind(new TypeLiteral<TckRepository<StackImpl>>() {}).toInstance(new StackRepository());
    bind(new TypeLiteral<TckRepository<UserImpl>>() {})
        .toInstance(new JpaTckRepository<>(UserImpl.class));
    bind(new TypeLiteral<TckRepository<WorkerImpl>>() {})
        .toInstance(new JpaTckRepository<>(WorkerImpl.class));
    bind(new TypeLiteral<TckRepository<StackPermissionsImpl>>() {})
        .toInstance(new JpaTckRepository<>(StackPermissionsImpl.class));

    bind(new TypeLiteral<PermissionsDao<StackPermissionsImpl>>() {})
        .to(JpaStackPermissionsDao.class);

    bind(new TypeLiteral<AbstractPermissionsDomain<WorkerImpl>>() {})
        .to(WorkerDaoTest.TestDomain.class);
    bind(new TypeLiteral<AbstractPermissionsDomain<StackPermissionsImpl>>() {})
        .to(StackPermissionsDaoTest.TestDomain.class);

    bind(WorkerDao.class).to(JpaWorkerDao.class);
    bind(WorkspaceDao.class).to(JpaWorkspaceDao.class);
    bind(StackDao.class).to(JpaStackDao.class);
  }

  private static class WorkspaceRepository extends JpaTckRepository<WorkspaceImpl> {
    public WorkspaceRepository() {
      super(WorkspaceImpl.class);
    }

    @Override
    public void createAll(Collection<? extends WorkspaceImpl> entities)
        throws TckRepositoryException {
      for (WorkspaceImpl entity : entities) {
        entity.getConfig().getProjects().forEach(ProjectConfigImpl::prePersistAttributes);
      }
      super.createAll(entities);
    }
  }

  private static class StackRepository extends JpaTckRepository<StackImpl> {
    public StackRepository() {
      super(StackImpl.class);
    }

    @Override
    public void createAll(Collection<? extends StackImpl> entities) throws TckRepositoryException {
      for (StackImpl stack : entities) {
        stack.getWorkspaceConfig().getProjects().forEach(ProjectConfigImpl::prePersistAttributes);
      }
      super.createAll(entities);
    }
  }
}
