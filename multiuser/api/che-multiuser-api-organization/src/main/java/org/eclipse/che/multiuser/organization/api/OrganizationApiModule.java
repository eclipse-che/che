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
package org.eclipse.che.multiuser.organization.api;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import org.eclipse.che.multiuser.api.permission.server.SuperPrivilegesChecker;
import org.eclipse.che.multiuser.api.permission.server.account.AccountPermissionsChecker;
import org.eclipse.che.multiuser.api.permission.shared.model.PermissionsDomain;
import org.eclipse.che.multiuser.organization.api.listener.MemberEventsPublisher;
import org.eclipse.che.multiuser.organization.api.listener.OrganizationEventsWebsocketBroadcaster;
import org.eclipse.che.multiuser.organization.api.listener.RemoveOrganizationOnLastUserRemovedEventSubscriber;
import org.eclipse.che.multiuser.organization.api.permissions.OrganizationDomain;
import org.eclipse.che.multiuser.organization.api.permissions.OrganizationPermissionsFilter;
import org.eclipse.che.multiuser.organization.api.permissions.OrganizationRemoteSubscriptionPermissionsChecks;
import org.eclipse.che.multiuser.organization.api.permissions.OrganizationResourceDistributionServicePermissionsFilter;
import org.eclipse.che.multiuser.organization.api.permissions.OrganizationalAccountPermissionsChecker;
import org.eclipse.che.multiuser.organization.api.resource.DefaultOrganizationResourcesProvider;
import org.eclipse.che.multiuser.organization.api.resource.OrganizationResourceLockKeyProvider;
import org.eclipse.che.multiuser.organization.api.resource.OrganizationResourcesDistributionService;
import org.eclipse.che.multiuser.organization.api.resource.OrganizationalAccountAvailableResourcesProvider;
import org.eclipse.che.multiuser.organization.api.resource.SuborganizationResourcesProvider;
import org.eclipse.che.multiuser.organization.spi.impl.OrganizationImpl;
import org.eclipse.che.multiuser.resource.api.AvailableResourcesProvider;
import org.eclipse.che.multiuser.resource.api.ResourceLockKeyProvider;
import org.eclipse.che.multiuser.resource.api.ResourcesProvider;
import org.eclipse.che.multiuser.resource.api.free.DefaultResourcesProvider;

/** @author Sergii Leschenko */
public class OrganizationApiModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(OrganizationService.class);
    bind(OrganizationPermissionsFilter.class);
    bind(OrganizationRemoteSubscriptionPermissionsChecks.class);
    bind(RemoveOrganizationOnLastUserRemovedEventSubscriber.class).asEagerSingleton();

    Multibinder.newSetBinder(binder(), DefaultResourcesProvider.class)
        .addBinding()
        .to(DefaultOrganizationResourcesProvider.class);

    Multibinder.newSetBinder(binder(), ResourcesProvider.class)
        .addBinding()
        .to(SuborganizationResourcesProvider.class);

    MapBinder.newMapBinder(binder(), String.class, AvailableResourcesProvider.class)
        .addBinding(OrganizationImpl.ORGANIZATIONAL_ACCOUNT)
        .to(OrganizationalAccountAvailableResourcesProvider.class);

    Multibinder.newSetBinder(binder(), ResourceLockKeyProvider.class)
        .addBinding()
        .to(OrganizationResourceLockKeyProvider.class);

    Multibinder.newSetBinder(binder(), AccountPermissionsChecker.class)
        .addBinding()
        .to(OrganizationalAccountPermissionsChecker.class);

    bind(OrganizationResourcesDistributionService.class);
    bind(OrganizationResourceDistributionServicePermissionsFilter.class);

    bind(OrganizationEventsWebsocketBroadcaster.class).asEagerSingleton();
    bind(MemberEventsPublisher.class).asEagerSingleton();

    Multibinder.newSetBinder(
            binder(),
            PermissionsDomain.class,
            Names.named(SuperPrivilegesChecker.SUPER_PRIVILEGED_DOMAINS))
        .addBinding()
        .to(OrganizationDomain.class);
  }
}
