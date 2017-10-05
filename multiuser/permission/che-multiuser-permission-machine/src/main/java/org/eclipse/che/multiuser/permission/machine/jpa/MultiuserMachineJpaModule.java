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
package org.eclipse.che.multiuser.permission.machine.jpa;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;
import org.eclipse.che.api.recipe.JpaRecipeDao;
import org.eclipse.che.multiuser.api.permission.server.AbstractPermissionsDomain;
import org.eclipse.che.multiuser.api.permission.server.model.impl.AbstractPermissions;
import org.eclipse.che.multiuser.api.permission.server.spi.PermissionsDao;
import org.eclipse.che.multiuser.permission.machine.jpa.listener.RemoveRecipeOnLastUserRemovedEventSubscriber;
import org.eclipse.che.multiuser.permission.machine.recipe.RecipeDomain;
import org.eclipse.che.multiuser.permission.machine.recipe.RecipePermissionsImpl;

/** @author Yevhenii Voevodin */
public class MultiuserMachineJpaModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(JpaRecipeDao.class).to(MultiuserJpaRecipeDao.class);
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
