/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.multiuser.api.permission.server;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import org.eclipse.che.multiuser.api.permission.server.account.AccountPermissionsChecker;
import org.eclipse.che.multiuser.api.permission.server.filter.GetPermissionsFilter;
import org.eclipse.che.multiuser.api.permission.server.filter.RemovePermissionsFilter;
import org.eclipse.che.multiuser.api.permission.server.filter.SetPermissionsFilter;
import org.eclipse.che.multiuser.api.permission.server.filter.check.RemovePermissionsChecker;
import org.eclipse.che.multiuser.api.permission.server.filter.check.SetPermissionsChecker;
import org.eclipse.che.multiuser.api.permission.server.jsonrpc.RemoteSubscriptionPermissionManager;

/** @author Sergii Leschenko */
public class PermissionsModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(PermissionsService.class);
    bind(SetPermissionsFilter.class);
    bind(RemovePermissionsFilter.class);
    bind(GetPermissionsFilter.class);
    bind(RemoteSubscriptionPermissionManager.class).asEagerSingleton();

    // Creates empty multibinder to avoid error during container starting
    Multibinder.newSetBinder(
        binder(), String.class, Names.named(SystemDomain.SYSTEM_DOMAIN_ACTIONS));

    // initialize empty set binder
    Multibinder.newSetBinder(binder(), AccountPermissionsChecker.class);
    MapBinder.newMapBinder(binder(), String.class, SetPermissionsChecker.class);
    MapBinder.newMapBinder(binder(), String.class, RemovePermissionsChecker.class);
  }
}
