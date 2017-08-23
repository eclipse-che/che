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
package org.eclipse.che.api.deploy;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import javax.sql.DataSource;
import org.eclipse.che.account.permission.PersonalAccountPermissionsChecker;
import org.eclipse.che.api.factory.server.permissions.FactoryPermissionsFilter;
import org.eclipse.che.api.permission.server.account.AccountPermissionsChecker;
import org.eclipse.che.api.permission.server.jpa.SystemPermissionsJpaModule;
import org.eclipse.che.api.user.server.permissions.UserProfileServicePermissionsFilter;
import org.eclipse.che.api.user.server.permissions.UserServicePermissionsFilter;
import org.eclipse.che.inject.DynaModule;
import org.eclipse.che.plugin.activity.ActivityPermissionsFilter;

@DynaModule
public class MultiUserCheWsMasterModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(DataSource.class).toProvider(org.eclipse.che.core.db.JndiDataSourceProvider.class);
    bind(org.eclipse.che.api.system.server.SystemServicePermissionsFilter.class);
    install(new SystemPermissionsJpaModule());
    install(new org.eclipse.che.api.permission.server.PermissionsModule());
    install(new org.eclipse.che.api.workspace.server.WorkspaceApiPermissionsModule());
    bind(UserProfileServicePermissionsFilter.class);
    bind(UserServicePermissionsFilter.class);
    bind(FactoryPermissionsFilter.class);
    bind(ActivityPermissionsFilter.class);
    bindConstant().annotatedWith(Names.named("system.super_privileged_mode")).to(false);
    Multibinder.newSetBinder(binder(), AccountPermissionsChecker.class)
        .addBinding()
        .to(PersonalAccountPermissionsChecker.class);
  }
}
