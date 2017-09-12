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
package org.eclipse.che.api.workspace.server;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import org.eclipse.che.api.machine.server.filters.RecipePermissionsFilter;
import org.eclipse.che.api.machine.server.recipe.RecipeCreatorPermissionsProvider;
import org.eclipse.che.api.machine.server.recipe.RecipeDomain;
import org.eclipse.che.api.permission.server.SuperPrivilegesChecker;
import org.eclipse.che.api.permission.server.filter.check.RemovePermissionsChecker;
import org.eclipse.che.api.permission.server.filter.check.SetPermissionsChecker;
import org.eclipse.che.api.permission.shared.model.PermissionsDomain;
import org.eclipse.che.api.workspace.server.filters.PublicPermissionsRemoveChecker;
import org.eclipse.che.api.workspace.server.filters.RecipeDomainSetPermissionsChecker;
import org.eclipse.che.api.workspace.server.filters.RecipeScriptDownloadPermissionFilter;
import org.eclipse.che.api.workspace.server.filters.StackDomainSetPermissionsChecker;
import org.eclipse.che.api.workspace.server.filters.StackPermissionsFilter;
import org.eclipse.che.api.workspace.server.filters.WorkspacePermissionsFilter;
import org.eclipse.che.api.workspace.server.stack.StackCreatorPermissionsProvider;
import org.eclipse.che.api.workspace.server.stack.StackDomain;

/** @author Sergii Leschenko */
public class WorkspaceApiPermissionsModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(WorkspacePermissionsFilter.class);
    bind(RecipePermissionsFilter.class);
    bind(StackPermissionsFilter.class);
    bind(RecipeScriptDownloadPermissionFilter.class);

    bind(WorkspaceCreatorPermissionsProvider.class).asEagerSingleton();
    bind(StackCreatorPermissionsProvider.class).asEagerSingleton();
    bind(RecipeCreatorPermissionsProvider.class).asEagerSingleton();

    Multibinder.newSetBinder(
            binder(),
            PermissionsDomain.class,
            Names.named(SuperPrivilegesChecker.SUPER_PRIVILEGED_DOMAINS))
        .addBinding()
        .to(WorkspaceDomain.class);

    MapBinder.newMapBinder(binder(), String.class, SetPermissionsChecker.class)
        .addBinding(StackDomain.DOMAIN_ID)
        .to(StackDomainSetPermissionsChecker.class);
    MapBinder.newMapBinder(binder(), String.class, SetPermissionsChecker.class)
        .addBinding(RecipeDomain.DOMAIN_ID)
        .to(RecipeDomainSetPermissionsChecker.class);

    MapBinder.newMapBinder(binder(), String.class, RemovePermissionsChecker.class)
        .addBinding(StackDomain.DOMAIN_ID)
        .to(PublicPermissionsRemoveChecker.class);
    MapBinder.newMapBinder(binder(), String.class, RemovePermissionsChecker.class)
        .addBinding(RecipeDomain.DOMAIN_ID)
        .to(PublicPermissionsRemoveChecker.class);
  }
}
