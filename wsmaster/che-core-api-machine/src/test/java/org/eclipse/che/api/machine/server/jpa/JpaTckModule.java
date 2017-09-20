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
package org.eclipse.che.api.machine.server.jpa;

import com.google.inject.TypeLiteral;
import java.util.Collection;
import java.util.stream.Collectors;
import org.eclipse.che.account.spi.AccountImpl;
import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.machine.server.model.impl.SnapshotImpl;
import org.eclipse.che.api.machine.server.recipe.RecipeImpl;
import org.eclipse.che.api.machine.server.spi.RecipeDao;
import org.eclipse.che.api.machine.server.spi.SnapshotDao;
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
import org.h2.Driver;

/** @author Anton Korneta */
public class JpaTckModule extends TckModule {

  @Override
  protected void configure() {
    H2DBTestServer server = H2DBTestServer.startDefault();
    install(
        new PersistTestModuleBuilder()
            .setDriver(Driver.class)
            .runningOn(server)
            .addEntityClasses(
                RecipeImpl.class, SnapshotImpl.class, AccountImpl.class, TestWorkspaceEntity.class)
            .setExceptionHandler(H2ExceptionHandler.class)
            .build());
    bind(DBInitializer.class).asEagerSingleton();
    bind(SchemaInitializer.class)
        .toInstance(new FlywaySchemaInitializer(server.getDataSource(), "che-schema"));
    bind(TckResourcesCleaner.class).toInstance(new H2JpaCleaner(server));

    bind(new TypeLiteral<TckRepository<RecipeImpl>>() {})
        .toInstance(new JpaTckRepository<>(RecipeImpl.class));
    bind(new TypeLiteral<TckRepository<SnapshotImpl>>() {})
        .toInstance(new JpaTckRepository<>(SnapshotImpl.class));
    bind(new TypeLiteral<TckRepository<Workspace>>() {})
        .toInstance(new TestWorkspacesTckRepository());
    bind(new TypeLiteral<TckRepository<AccountImpl>>() {})
        .toInstance(new JpaTckRepository<>(AccountImpl.class));

    bind(RecipeDao.class).to(JpaRecipeDao.class);
    bind(SnapshotDao.class).to(JpaSnapshotDao.class);
  }

  private static class TestWorkspacesTckRepository extends JpaTckRepository<Workspace> {

    public TestWorkspacesTckRepository() {
      super(TestWorkspaceEntity.class);
    }

    @Override
    public void createAll(Collection<? extends Workspace> entities) throws TckRepositoryException {
      super.createAll(entities.stream().map(TestWorkspaceEntity::new).collect(Collectors.toList()));
    }
  }
}
