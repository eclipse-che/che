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
package org.eclipse.che.multiuser.permission.devfile.server.jpa;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;
import org.eclipse.che.api.devfile.server.RemoveUserDevfileBeforeAccountRemovedEventSubscriber;
import org.eclipse.che.api.devfile.server.spi.UserDevfileDao;
import org.eclipse.che.multiuser.api.permission.server.AbstractPermissionsDomain;
import org.eclipse.che.multiuser.api.permission.server.model.impl.AbstractPermissions;
import org.eclipse.che.multiuser.api.permission.server.spi.PermissionsDao;
import org.eclipse.che.multiuser.permission.devfile.server.UserDevfileDomain;
import org.eclipse.che.multiuser.permission.devfile.server.model.impl.UserDevfilePermissionImpl;
import org.eclipse.che.multiuser.permission.devfile.server.spi.UserDevfilePermissionDao;
import org.eclipse.che.multiuser.permission.devfile.server.spi.jpa.JpaUserDevfilePermissionDao;
import org.eclipse.che.multiuser.permission.devfile.server.spi.jpa.JpaUserDevfilePermissionDao.RemoveUserDevfilePermissionsBeforeUserDevfileRemovedEventSubscriber;
import org.eclipse.che.multiuser.permission.devfile.server.spi.jpa.JpaUserDevfilePermissionDao.RemoveUserDevfilePermissionsBeforeUserRemovedEventSubscriber;
import org.eclipse.che.multiuser.permission.devfile.server.spi.jpa.MultiuserJpaUserDevfileDao;

public class MultiuserUserDevfileJpaModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(UserDevfilePermissionDao.class).to(JpaUserDevfilePermissionDao.class);
    bind(UserDevfileDao.class).to(MultiuserJpaUserDevfileDao.class);

    bind(RemoveUserDevfileBeforeAccountRemovedEventSubscriber.class).asEagerSingleton();
    bind(RemoveUserDevfilePermissionsBeforeUserDevfileRemovedEventSubscriber.class)
        .asEagerSingleton();
    bind(RemoveUserDevfilePermissionsBeforeUserRemovedEventSubscriber.class).asEagerSingleton();

    bind(new TypeLiteral<AbstractPermissionsDomain<UserDevfilePermissionImpl>>() {})
        .to(UserDevfileDomain.class);

    Multibinder<PermissionsDao<? extends AbstractPermissions>> daos =
        Multibinder.newSetBinder(
            binder(), new TypeLiteral<PermissionsDao<? extends AbstractPermissions>>() {});
    daos.addBinding().to(JpaUserDevfilePermissionDao.class);
  }
}
