/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.multiuser.permission.devfile.server;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import org.eclipse.che.multiuser.api.permission.server.SuperPrivilegesChecker;
import org.eclipse.che.multiuser.api.permission.shared.model.PermissionsDomain;
import org.eclipse.che.multiuser.permission.devfile.server.filters.UserDevfilePermissionsFilter;

public class UserDevfileApiPermissionsModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(UserDevfilePermissionsFilter.class);
    bind(UserDevfileCreatorPermissionsProvider.class).asEagerSingleton();

    Multibinder.newSetBinder(
            binder(),
            PermissionsDomain.class,
            Names.named(SuperPrivilegesChecker.SUPER_PRIVILEGED_DOMAINS))
        .addBinding()
        .to(UserDevfileDomain.class);
  }
}
