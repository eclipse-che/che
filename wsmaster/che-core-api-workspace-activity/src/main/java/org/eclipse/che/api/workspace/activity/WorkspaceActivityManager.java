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
package org.eclipse.che.api.workspace.activity;

import static java.util.Collections.singletonMap;
import static org.eclipse.che.api.workspace.shared.Constants.WORKSPACE_STOPPED_BY;
import static org.eclipse.che.api.workspace.shared.Constants.WORKSPACE_STOP_REASON;

import com.google.common.annotations.VisibleForTesting;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.workspace.server.WorkspaceManager;
import org.eclipse.che.api.workspace.shared.dto.event.WorkspaceStatusEvent;
import org.eclipse.che.commons.schedule.ScheduleDelay;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides API for updating activity timestamp of running workspaces. Stops the inactive workspaces
 * by given expiration time. Upon stopping, workspace attributes will be updated with information
 * like cause and timestamp of workspace stop.
 *
 * <p>Note that the workspace is not stopped immediately, scheduler will stop the workspaces with
 * one minute rate. If workspace idle timeout is negative, then workspace would not be stopped
 * automatically.
 *
 * @author Anton Korneta
 */
@Singleton
public class WorkspaceActivityManager {

  private static final Logger LOG = LoggerFactory.getLogger(WorkspaceActivityManager.class);

  private static final String ACTIVITY_CHECKER = "activity-checker";

  private final long defaultTimeout;
  private final WorkspaceActivityDao activityDao;
  private final EventService eventService;
  private final EventSubscriber<?> workspaceEventsSubscriber;

  protected final WorkspaceManager workspaceManager;

  @Inject
  public WorkspaceActivityManager(
      WorkspaceManager workspaceManager,
      WorkspaceActivityDao activityDao,
      EventService eventService,
      @Named("che.limits.workspace.idle.timeout") long timeout) {
    this.workspaceManager = workspaceManager;
    this.eventService = eventService;
    this.activityDao = activityDao;
    this.defaultTimeout = timeout;
    this.workspaceEventsSubscriber =
        new EventSubscriber<WorkspaceStatusEvent>() {
          @Override
          public void onEvent(WorkspaceStatusEvent event) {
            switch (event.getStatus()) {
              case RUNNING:
                try {
                  Workspace workspace = workspaceManager.getWorkspace(event.getWorkspaceId());
                  if (workspace.getAttributes().remove(WORKSPACE_STOPPED_BY) != null) {
                    workspaceManager.updateWorkspace(event.getWorkspaceId(), workspace);
                  }
                } catch (Exception ex) {
                  LOG.warn(
                      "Failed to remove stopped information attribute for workspace "
                          + event.getWorkspaceId());
                }
                update(event.getWorkspaceId(), System.currentTimeMillis());
                break;
              case STOPPED:
                try {
                  activityDao.removeExpiration(event.getWorkspaceId());
                } catch (ServerException e) {
                  LOG.error(e.getLocalizedMessage(), e);
                }
                break;
              default:
                // do nothing
            }
          }
        };
  }

  /**
   * Update the expiry period the workspace if it exists, otherwise add new one
   *
   * @param wsId active workspace identifier
   * @param activityTime moment in which the activity occurred
   */
  public void update(String wsId, long activityTime) {
    try {
      long timeout = getIdleTimeout(wsId);
      if (timeout > 0) {
        activityDao.setExpiration(new WorkspaceExpiration(wsId, activityTime + timeout));
      }
    } catch (ServerException e) {
      LOG.error(e.getLocalizedMessage(), e);
    }
  }

  protected long getIdleTimeout(String wsId) {
    return defaultTimeout;
  }

  @ScheduleDelay(
      initialDelayParameterName = "che.workspace.activity_check_scheduler_delay_s",
      delayParameterName = "che.workspace.activity_check_scheduler_period_s")
  private void invalidate() {
    try {
      activityDao.findExpired(System.currentTimeMillis()).forEach(this::stopExpired);
    } catch (ServerException e) {
      LOG.error(e.getLocalizedMessage(), e);
    }
  }

  private void stopExpired(String workspaceId) {
    try {
      Workspace workspace = workspaceManager.getWorkspace(workspaceId);
      workspace.getAttributes().put(WORKSPACE_STOPPED_BY, ACTIVITY_CHECKER);
      workspaceManager.updateWorkspace(workspaceId, workspace);
      workspaceManager.stopWorkspace(
          workspaceId, singletonMap(WORKSPACE_STOP_REASON, "Workspace idle timeout exceeded"));
    } catch (NotFoundException ignored) {
      // workspace no longer exists, no need to do anything
    } catch (ConflictException e) {
      LOG.warn(e.getLocalizedMessage());
    } catch (Exception ex) {
      LOG.error(ex.getLocalizedMessage());
      LOG.debug(ex.getLocalizedMessage(), ex);
    } finally {
      try {
        activityDao.removeExpiration(workspaceId);
      } catch (ServerException e) {
        LOG.error(e.getLocalizedMessage(), e);
      }
    }
  }

  @VisibleForTesting
  @PostConstruct
  public void subscribe() {
    eventService.subscribe(workspaceEventsSubscriber);
  }
}
