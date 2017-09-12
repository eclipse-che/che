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

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;
import org.eclipse.che.api.machine.server.jpa.listener.RemoveRecipeOnLastUserRemovedEventSubscriber;
import org.eclipse.che.api.machine.server.recipe.RecipeDomain;
import org.eclipse.che.api.machine.server.recipe.RecipePermissionsImpl;
import org.eclipse.che.api.machine.server.spi.RecipeDao;
import org.eclipse.che.api.machine.server.spi.SnapshotDao;
import org.eclipse.che.api.permission.server.AbstractPermissionsDomain;
import org.eclipse.che.api.permission.server.model.impl.AbstractPermissions;
import org.eclipse.che.api.permission.server.spi.PermissionsDao;

/** @author Yevhenii Voevodin */
public class MachineJpaModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(RecipeDao.class).to(JpaRecipeDao.class);
    bind(SnapshotDao.class).to(JpaSnapshotDao.class);
    bind(new TypeLiteral<AbstractPermissionsDomain<RecipePermissionsImpl>>() {})
        .to(RecipeDomain.class);

    final Multibinder<PermissionsDao<? extends AbstractPermissions>> daos =
        Multibinder.newSetBinder(
            binder(), new TypeLiteral<PermissionsDao<? extends AbstractPermissions>>() {});
    daos.addBinding().to(JpaRecipePermissionsDao.class);

    bind(new TypeLiteral<AbstractPermissionsDomain<RecipePermissionsImpl>>() {})
        .to(RecipeDomain.class);
    bind(JpaRecipePermissionsDao.RemovePermissionsBeforeRecipeRemovedEventSubscriber.class)
        .asEagerSingleton();
    bind(RemoveRecipeOnLastUserRemovedEventSubscriber.class).asEagerSingleton();
  }
}
