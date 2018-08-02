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
package org.eclipse.che.multiuser.resource.api;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.multibindings.Multibinder;
import org.eclipse.che.api.workspace.server.WorkspaceManager;
import org.eclipse.che.multiuser.api.permission.server.account.AccountPermissionsChecker;
import org.eclipse.che.multiuser.resource.api.free.DefaultResourcesProvider;
import org.eclipse.che.multiuser.resource.api.free.FreeResourcesLimitService;
import org.eclipse.che.multiuser.resource.api.free.FreeResourcesProvider;
import org.eclipse.che.multiuser.resource.api.type.RamResourceType;
import org.eclipse.che.multiuser.resource.api.type.ResourceType;
import org.eclipse.che.multiuser.resource.api.type.RuntimeResourceType;
import org.eclipse.che.multiuser.resource.api.type.TimeoutResourceType;
import org.eclipse.che.multiuser.resource.api.type.WorkspaceResourceType;
import org.eclipse.che.multiuser.resource.api.usage.ResourceService;
import org.eclipse.che.multiuser.resource.api.usage.tracker.RamResourceUsageTracker;
import org.eclipse.che.multiuser.resource.api.usage.tracker.RuntimeResourceUsageTracker;
import org.eclipse.che.multiuser.resource.api.usage.tracker.WorkspaceResourceUsageTracker;
import org.eclipse.che.multiuser.resource.api.workspace.LimitsCheckingWorkspaceManager;
import org.eclipse.che.multiuser.resource.spi.FreeResourcesLimitDao;
import org.eclipse.che.multiuser.resource.spi.jpa.JpaFreeResourcesLimitDao;

/** @author Sergii Leschenko */
public class ResourceModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(ResourceService.class);

    bind(FreeResourcesLimitService.class);
    bind(FreeResourcesLimitDao.class).to(JpaFreeResourcesLimitDao.class);
    bind(JpaFreeResourcesLimitDao.RemoveFreeResourcesLimitSubscriber.class).asEagerSingleton();

    bind(WorkspaceManager.class).to(LimitsCheckingWorkspaceManager.class);

    MapBinder.newMapBinder(binder(), String.class, AvailableResourcesProvider.class);
    Multibinder.newSetBinder(binder(), DefaultResourcesProvider.class);
    Multibinder.newSetBinder(binder(), ResourceLockKeyProvider.class);
    Multibinder.newSetBinder(binder(), AccountPermissionsChecker.class);

    Multibinder.newSetBinder(binder(), ResourcesProvider.class)
        .addBinding()
        .to(FreeResourcesProvider.class);

    MapBinder.newMapBinder(binder(), String.class, AvailableResourcesProvider.class);

    Multibinder<ResourceType> resourcesTypesBinder =
        Multibinder.newSetBinder(binder(), ResourceType.class);
    resourcesTypesBinder.addBinding().to(RamResourceType.class);
    resourcesTypesBinder.addBinding().to(WorkspaceResourceType.class);
    resourcesTypesBinder.addBinding().to(RuntimeResourceType.class);
    resourcesTypesBinder.addBinding().to(TimeoutResourceType.class);

    Multibinder<ResourceUsageTracker> usageTrackersBinder =
        Multibinder.newSetBinder(binder(), ResourceUsageTracker.class);
    usageTrackersBinder.addBinding().to(RamResourceUsageTracker.class);
    usageTrackersBinder.addBinding().to(WorkspaceResourceUsageTracker.class);
    usageTrackersBinder.addBinding().to(RuntimeResourceUsageTracker.class);
  }
}
