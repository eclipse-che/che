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
package org.eclipse.che.multiuser.api.account.personal;

import static java.util.Arrays.asList;

import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.user.server.UserManager;
import org.eclipse.che.commons.lang.Size;
import org.eclipse.che.multiuser.resource.api.free.DefaultResourcesProvider;
import org.eclipse.che.multiuser.resource.api.type.RamResourceType;
import org.eclipse.che.multiuser.resource.api.type.RuntimeResourceType;
import org.eclipse.che.multiuser.resource.api.type.TimeoutResourceType;
import org.eclipse.che.multiuser.resource.api.type.WorkspaceResourceType;
import org.eclipse.che.multiuser.resource.spi.impl.ResourceImpl;

/**
 * Provided free resources that are available for usage by personal accounts by default.
 *
 * @author Sergii Leschenko
 */
@Singleton
public class DefaultUserResourcesProvider implements DefaultResourcesProvider {
  private final long timeout;
  private final long ramPerUser;
  private final int workspacesPerUser;
  private final int runtimesPerUser;

  @Inject
  public DefaultUserResourcesProvider(
      @Named("che.limits.workspace.idle.timeout") long timeout,
      @Named("che.limits.user.workspaces.ram") String ramPerUser,
      @Named("che.limits.user.workspaces.count") int workspacesPerUser,
      @Named("che.limits.user.workspaces.run.count") int runtimesPerUser) {
    this.timeout = TimeUnit.MILLISECONDS.toMinutes(timeout);
    this.ramPerUser = "-1".equals(ramPerUser) ? -1 : Size.parseSizeToMegabytes(ramPerUser);
    this.workspacesPerUser = workspacesPerUser;
    this.runtimesPerUser = runtimesPerUser;
  }

  @Override
  public String getAccountType() {
    return UserManager.PERSONAL_ACCOUNT;
  }

  @Override
  public List<ResourceImpl> getResources(String accountId)
      throws ServerException, NotFoundException {
    return asList(
        new ResourceImpl(TimeoutResourceType.ID, timeout, TimeoutResourceType.UNIT),
        new ResourceImpl(RamResourceType.ID, ramPerUser, RamResourceType.UNIT),
        new ResourceImpl(WorkspaceResourceType.ID, workspacesPerUser, WorkspaceResourceType.UNIT),
        new ResourceImpl(RuntimeResourceType.ID, runtimesPerUser, RuntimeResourceType.UNIT));
  }
}
