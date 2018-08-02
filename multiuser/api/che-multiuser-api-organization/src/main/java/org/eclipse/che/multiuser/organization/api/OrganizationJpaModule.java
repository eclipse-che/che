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
package org.eclipse.che.multiuser.organization.api;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;
import org.eclipse.che.multiuser.api.permission.server.AbstractPermissionsDomain;
import org.eclipse.che.multiuser.api.permission.server.model.impl.AbstractPermissions;
import org.eclipse.che.multiuser.api.permission.server.spi.PermissionsDao;
import org.eclipse.che.multiuser.organization.api.permissions.OrganizationDomain;
import org.eclipse.che.multiuser.organization.spi.MemberDao;
import org.eclipse.che.multiuser.organization.spi.OrganizationDao;
import org.eclipse.che.multiuser.organization.spi.OrganizationDistributedResourcesDao;
import org.eclipse.che.multiuser.organization.spi.impl.MemberImpl;
import org.eclipse.che.multiuser.organization.spi.jpa.JpaMemberDao;
import org.eclipse.che.multiuser.organization.spi.jpa.JpaOrganizationDao;
import org.eclipse.che.multiuser.organization.spi.jpa.JpaOrganizationDistributedResourcesDao;

/** @author Sergii Leschenko */
public class OrganizationJpaModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(OrganizationDao.class).to(JpaOrganizationDao.class);
    bind(MemberDao.class).to(JpaMemberDao.class);

    bind(new TypeLiteral<AbstractPermissionsDomain<MemberImpl>>() {}).to(OrganizationDomain.class);

    Multibinder.newSetBinder(
            binder(), new TypeLiteral<PermissionsDao<? extends AbstractPermissions>>() {})
        .addBinding()
        .to(JpaMemberDao.class);

    bind(OrganizationDistributedResourcesDao.class)
        .to(JpaOrganizationDistributedResourcesDao.class);
  }
}
