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
package org.eclipse.che.api.workspace.server;

import static org.slf4j.LoggerFactory.getLogger;

import com.google.common.annotations.VisibleForTesting;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.Pages;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.api.workspace.server.spi.WorkspaceDao;
import org.eclipse.che.commons.schedule.ScheduleDelay;
import org.slf4j.Logger;

/**
 * Removes stopped temporary workspaces on server startup and shutdown.
 *
 * @author Max Shaposhnik (mshaposhnik@codenvy.com)
 */
@Singleton
public class TemporaryWorkspaceRemover {

  private static final Logger LOG = getLogger(TemporaryWorkspaceRemover.class);

  private final WorkspaceDao workspaceDao;
  private final WorkspaceRuntimes runtimes;

  @Inject
  public TemporaryWorkspaceRemover(WorkspaceDao workspaceDao, WorkspaceRuntimes runtimes) {
    this.workspaceDao = workspaceDao;
    this.runtimes = runtimes;
  }

  @ScheduleDelay(
      initialDelayParameterName = "che.workspace.cleanup_temporary_initial_delay_min",
      delayParameterName = "che.workspace.cleanup_temporary_period_min",
      unit = TimeUnit.MINUTES)
  void initialize() {
    try {
      removeTemporaryWs();
    } catch (ServerException e) {
      LOG.warn("Unable to cleanup temporary workspaces on startup: " + e.getMessage(), e);
    }
  }

  void shutdown() {
    try {
      removeTemporaryWs();
    } catch (ServerException e) {
      LOG.warn("Unable to cleanup temporary workspaces on shutdown: " + e.getMessage(), e);
    }
  }

  @VisibleForTesting
  void removeTemporaryWs() throws ServerException {
    for (WorkspaceImpl workspace :
        Pages.iterate(
            (maxItems, skipCount) -> workspaceDao.getWorkspaces(true, maxItems, skipCount))) {
      WorkspaceStatus status = runtimes.getStatus(workspace.getId());
      if (status == WorkspaceStatus.STOPPED) {
        try {
          workspaceDao.remove(workspace.getId());
        } catch (ServerException e) {
          LOG.error(
              "Unable to cleanup temporary workspace {}. Reason is {}",
              workspace.getId(),
              e.getMessage());
        }
      }
    }
  }
}
