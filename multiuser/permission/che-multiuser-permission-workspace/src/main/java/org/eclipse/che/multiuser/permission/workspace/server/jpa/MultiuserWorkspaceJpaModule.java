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

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;
import org.eclipse.che.api.workspace.server.jpa.JpaWorkspaceDao.RemoveWorkspaceBeforeAccountRemovedEventSubscriber;
import org.eclipse.che.api.workspace.server.spi.StackDao;
import org.eclipse.che.api.workspace.server.spi.WorkspaceDao;
import org.eclipse.che.multiuser.api.permission.server.AbstractPermissionsDomain;
import org.eclipse.che.multiuser.api.permission.server.jpa.listener.RemovePermissionsOnLastUserRemovedEventSubscriber;
import org.eclipse.che.multiuser.api.permission.server.model.impl.AbstractPermissions;
import org.eclipse.che.multiuser.api.permission.server.spi.PermissionsDao;
import org.eclipse.che.multiuser.permission.workspace.server.WorkspaceDomain;
import org.eclipse.che.multiuser.permission.workspace.server.jpa.listener.RemoveStackOnLastUserRemovedEventSubscriber;
import org.eclipse.che.multiuser.permission.workspace.server.model.impl.WorkerImpl;
import org.eclipse.che.multiuser.permission.workspace.server.spi.WorkerDao;
import org.eclipse.che.multiuser.permission.workspace.server.spi.jpa.JpaStackPermissionsDao;
import org.eclipse.che.multiuser.permission.workspace.server.spi.jpa.JpaWorkerDao;
import org.eclipse.che.multiuser.permission.workspace.server.spi.jpa.MultiuserJpaStackDao;
import org.eclipse.che.multiuser.permission.workspace.server.spi.jpa.MultiuserJpaWorkspaceDao;
import org.eclipse.che.multiuser.permission.workspace.server.stack.StackDomain;
import org.eclipse.che.multiuser.permission.workspace.server.stack.StackPermissionsImpl;

/** @author Yevhenii Voevodin */
public class MultiuserWorkspaceJpaModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(StackDao.class).to(MultiuserJpaStackDao.class);
    bind(WorkerDao.class).to(JpaWorkerDao.class);
    bind(WorkspaceDao.class).to(MultiuserJpaWorkspaceDao.class);
    bind(RemoveWorkspaceBeforeAccountRemovedEventSubscriber.class).asEagerSingleton();

    bind(JpaWorkerDao.RemoveWorkersBeforeWorkspaceRemovedEventSubscriber.class).asEagerSingleton();
    bind(JpaWorkerDao.RemoveWorkersBeforeUserRemovedEventSubscriber.class).asEagerSingleton();

    bind(new TypeLiteral<
            RemovePermissionsOnLastUserRemovedEventSubscriber<JpaStackPermissionsDao>>() {})
        .to(RemoveStackOnLastUserRemovedEventSubscriber.class);
    bind(JpaStackPermissionsDao.RemovePermissionsBeforeStackRemovedEventSubscriber.class)
        .asEagerSingleton();

    bind(new TypeLiteral<AbstractPermissionsDomain<StackPermissionsImpl>>() {})
        .to(StackDomain.class);
    bind(new TypeLiteral<AbstractPermissionsDomain<WorkerImpl>>() {}).to(WorkspaceDomain.class);

    Multibinder<PermissionsDao<? extends AbstractPermissions>> daos =
        Multibinder.newSetBinder(
            binder(), new TypeLiteral<PermissionsDao<? extends AbstractPermissions>>() {});
    daos.addBinding().to(JpaWorkerDao.class);
    daos.addBinding().to(JpaStackPermissionsDao.class);
  }
}
