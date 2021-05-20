/*
 * Copyright (c) 2012-2019 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.multiuser.resource.api.usage.tracker;

import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.STOPPED;

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
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.workspace.server.WorkspaceManager;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.multiuser.resource.api.ResourceUsageTracker;
import org.eclipse.che.multiuser.resource.api.type.RamResourceType;
import org.eclipse.che.multiuser.resource.model.Resource;
import org.eclipse.che.multiuser.resource.spi.impl.ResourceImpl;

/**
 * Tracks usage of {@link RamResourceType} resource.
 *
 * @author Sergii Leschenko
 * @author Anton Korneta
 */
@Singleton
public class RamResourceUsageTracker implements ResourceUsageTracker {
  private final Provider<WorkspaceManager> workspaceManagerProvider;
  private final AccountManager accountManager;
  private final EnvironmentRamCalculator environmentRamCalculator;

  @Inject
  public RamResourceUsageTracker(
      Provider<WorkspaceManager> workspaceManagerProvider,
      AccountManager accountManager,
      EnvironmentRamCalculator environmentRamCalculator) {
    this.workspaceManagerProvider = workspaceManagerProvider;
    this.accountManager = accountManager;
    this.environmentRamCalculator = environmentRamCalculator;
  }

  @Override
  public Optional<Resource> getUsedResource(String accountId)
      throws NotFoundException, ServerException {
    final Account account = accountManager.getById(accountId);
    List<WorkspaceImpl> activeWorkspaces =
        Pages.stream(
                (maxItems, skipCount) ->
                    workspaceManagerProvider
                        .get()
                        .getByNamespace(account.getName(), true, maxItems, skipCount))
            .filter(ws -> STOPPED != ws.getStatus())
            .collect(Collectors.toList());
    long currentlyUsedRamMB = 0;
    for (WorkspaceImpl activeWorkspace : activeWorkspaces) {
      if (WorkspaceStatus.STARTING.equals(activeWorkspace.getStatus())) {
        // starting workspace may not have all machine in runtime
        // it is need to calculate ram from environment config
        WorkspaceConfigImpl config = activeWorkspace.getConfig();

        if (config != null) {
          final EnvironmentImpl startingEnvironment =
              config.getEnvironments().get(activeWorkspace.getRuntime().getActiveEnv());
          if (startingEnvironment != null) {
            currentlyUsedRamMB += environmentRamCalculator.calculate(startingEnvironment);
          }
        }
        // Estimation of memory for starting workspace with Devfile is not implemented yet
        // just ignore such
      } else {
        currentlyUsedRamMB += environmentRamCalculator.calculate(activeWorkspace.getRuntime());
      }
    }

    if (currentlyUsedRamMB > 0) {
      return Optional.of(
          new ResourceImpl(RamResourceType.ID, currentlyUsedRamMB, RamResourceType.UNIT));
    } else {
      return Optional.empty();
    }
  }
}
