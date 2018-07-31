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
package org.eclipse.che.multiuser.resource.api.usage.tracker;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import org.eclipse.che.account.api.AccountManager;
import org.eclipse.che.account.shared.model.Account;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.Pages;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.workspace.server.WorkspaceManager;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.multiuser.resource.api.ResourceUsageTracker;
import org.eclipse.che.multiuser.resource.api.type.WorkspaceResourceType;
import org.eclipse.che.multiuser.resource.model.Resource;
import org.eclipse.che.multiuser.resource.spi.impl.ResourceImpl;

/**
 * Tracks usage of {@link WorkspaceResourceType} resource.
 *
 * @author Sergii Leschenko
 */
@Singleton
public class WorkspaceResourceUsageTracker implements ResourceUsageTracker {
  private final Provider<WorkspaceManager> workspaceManagerProvider;
  private final AccountManager accountManager;

  @Inject
  public WorkspaceResourceUsageTracker(
      Provider<WorkspaceManager> workspaceManagerProvider, AccountManager accountManager) {
    this.workspaceManagerProvider = workspaceManagerProvider;
    this.accountManager = accountManager;
  }

  @Override
  public Optional<Resource> getUsedResource(String accountId)
      throws NotFoundException, ServerException {
    final Account account = accountManager.getById(accountId);
    final List<WorkspaceImpl> accountWorkspaces =
        Pages.stream(
                (maxItems, skipCount) ->
                    workspaceManagerProvider
                        .get()
                        .getByNamespace(account.getName(), false, maxItems, skipCount))
            .collect(Collectors.toList());
    if (!accountWorkspaces.isEmpty()) {
      return Optional.of(
          new ResourceImpl(
              WorkspaceResourceType.ID, accountWorkspaces.size(), WorkspaceResourceType.UNIT));
    } else {
      return Optional.empty();
    }
  }
}
