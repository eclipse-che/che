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
package org.eclipse.che.multiuser.api.workspace.activity;

import java.util.List;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.eclipse.che.account.api.AccountManager;
import org.eclipse.che.account.shared.model.Account;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.workspace.activity.WorkspaceActivityDao;
import org.eclipse.che.api.workspace.activity.WorkspaceActivityManager;
import org.eclipse.che.api.workspace.server.WorkspaceManager;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.multiuser.resource.api.type.TimeoutResourceType;
import org.eclipse.che.multiuser.resource.api.usage.ResourceManager;
import org.eclipse.che.multiuser.resource.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of WorkspaceActivityManager with overriden retrieval of timeout, with using
 * Resource API to get user's limits
 *
 * @author Mykhailo Kuznietsov
 */
@Singleton
public class MultiUserWorkspaceActivityManager extends WorkspaceActivityManager {

  private static final Logger LOG =
      LoggerFactory.getLogger(MultiUserWorkspaceActivityManager.class);

  private final AccountManager accountManager;
  private final ResourceManager resourceManager;
  private final long defaultTimeout;
  private final long runTimeout;

  @Inject
  public MultiUserWorkspaceActivityManager(
      WorkspaceManager workspaceManager,
      WorkspaceActivityDao activityDao,
      EventService eventService,
      AccountManager accountManager,
      ResourceManager resourceManager,
      @Named("che.limits.workspace.idle.timeout") long defaultTimeout,
      @Named("che.limits.workspace.run.timeout") long runTimeout) {
    super(workspaceManager, activityDao, eventService, defaultTimeout, runTimeout);
    this.accountManager = accountManager;
    this.resourceManager = resourceManager;
    this.defaultTimeout = defaultTimeout;
    this.runTimeout = runTimeout;
  }

  @Override
  protected long getIdleTimeout(String wsId) {
    List<? extends Resource> availableResources;
    try {
      WorkspaceImpl workspace = workspaceManager.getWorkspace(wsId);
      Account account = accountManager.getByName(workspace.getNamespace());
      availableResources = resourceManager.getAvailableResources(account.getId());

    } catch (NotFoundException | ServerException e) {
      LOG.error(e.getLocalizedMessage(), e);
      return defaultTimeout;
    }
    Optional<? extends Resource> timeoutOpt =
        availableResources
            .stream()
            .filter(resource -> TimeoutResourceType.ID.equals(resource.getType()))
            .findAny();

    if (timeoutOpt.isPresent()) {
      return timeoutOpt.get().getAmount() * 60 * 1000;
    } else {
      return defaultTimeout;
    }
  }
}
