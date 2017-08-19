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

import static org.eclipse.che.commons.test.db.H2TestHelper.inMemoryDefault;

import com.google.inject.TypeLiteral;
import com.google.inject.persist.jpa.JpaPersistModule;
import org.eclipse.che.api.machine.server.recipe.RecipeImpl;
import org.eclipse.che.api.machine.server.recipe.RecipePermissionsImpl;
import org.eclipse.che.api.machine.server.spi.RecipeDao;
import org.eclipse.che.api.machine.server.spi.tck.RecipePermissionsDaoTest;
import org.eclipse.che.api.permission.server.AbstractPermissionsDomain;
import org.eclipse.che.api.permission.server.spi.PermissionsDao;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.commons.test.db.H2JpaCleaner;
import org.eclipse.che.commons.test.tck.TckModule;
import org.eclipse.che.commons.test.tck.TckResourcesCleaner;
import org.eclipse.che.commons.test.tck.repository.JpaTckRepository;
import org.eclipse.che.commons.test.tck.repository.TckRepository;
import org.eclipse.che.core.db.DBInitializer;
import org.eclipse.che.core.db.schema.SchemaInitializer;
import org.eclipse.che.core.db.schema.impl.flyway.FlywaySchemaInitializer;

/** @author Max Shaposhnik */
public class RecipePermissionsTckModule extends TckModule {
  @Override
  protected void configure() {
    install(new JpaPersistModule("main"));
    bind(SchemaInitializer.class)
        .toInstance(new FlywaySchemaInitializer(inMemoryDefault(), "che-schema", "codenvy-schema"));
    bind(DBInitializer.class).asEagerSingleton();
    bind(TckResourcesCleaner.class).to(H2JpaCleaner.class);

    bind(new TypeLiteral<AbstractPermissionsDomain<RecipePermissionsImpl>>() {})
        .to(RecipePermissionsDaoTest.TestDomain.class);
    bind(new TypeLiteral<PermissionsDao<RecipePermissionsImpl>>() {})
        .to(JpaRecipePermissionsDao.class);
    bind(new TypeLiteral<TckRepository<RecipePermissionsImpl>>() {})
        .toInstance(new JpaTckRepository<>(RecipePermissionsImpl.class));
    bind(new TypeLiteral<TckRepository<UserImpl>>() {})
        .toInstance(new JpaTckRepository<>(UserImpl.class));
    bind(new TypeLiteral<TckRepository<RecipeImpl>>() {})
        .toInstance(new JpaTckRepository<>(RecipeImpl.class));

    bind(RecipeDao.class).to(JpaRecipeDao.class);
  }
}
