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
package org.eclipse.che.multiuser.resource.api.usage.tracker;

import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.STOPPED;

import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import org.eclipse.che.account.api.AccountManager;
import org.eclipse.che.account.shared.model.Account;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.Pages;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.workspace.server.WorkspaceManager;
import org.eclipse.che.multiuser.resource.api.ResourceUsageTracker;
import org.eclipse.che.multiuser.resource.api.type.RuntimeResourceType;
import org.eclipse.che.multiuser.resource.model.Resource;
import org.eclipse.che.multiuser.resource.spi.impl.ResourceImpl;

/**
 * Tracks usage of {@link RuntimeResourceType} resource.
 *
 * @author Sergii Leschenko
 */
@Singleton
public class RuntimeResourceUsageTracker implements ResourceUsageTracker {
  private final Provider<WorkspaceManager> workspaceManagerProvider;
  private final AccountManager accountManager;

  @Inject
  public RuntimeResourceUsageTracker(
      Provider<WorkspaceManager> workspaceManagerProvider, AccountManager accountManager) {
    this.workspaceManagerProvider = workspaceManagerProvider;
    this.accountManager = accountManager;
  }

  @Override
  public Optional<Resource> getUsedResource(String accountId)
      throws NotFoundException, ServerException {
    final Account account = accountManager.getById(accountId);
    final long currentlyUsedRuntimes =
        Pages.stream(
                (maxItems, skipCount) ->
                    workspaceManagerProvider
                        .get()
                        .getByNamespace(account.getName(), false, maxItems, skipCount))
            .filter(ws -> STOPPED != ws.getStatus())
            .count();
    if (currentlyUsedRuntimes > 0) {
      return Optional.of(
          new ResourceImpl(
              RuntimeResourceType.ID, currentlyUsedRuntimes, RuntimeResourceType.UNIT));
    } else {
      return Optional.empty();
    }
  }
}
