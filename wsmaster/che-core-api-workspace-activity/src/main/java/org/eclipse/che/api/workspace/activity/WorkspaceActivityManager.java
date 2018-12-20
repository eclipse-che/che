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
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.RUNNING;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.STOPPED;
import static org.eclipse.che.api.workspace.shared.Constants.WORKSPACE_STOPPED_BY;
import static org.eclipse.che.api.workspace.shared.Constants.WORKSPACE_STOP_REASON;

import com.google.common.annotations.VisibleForTesting;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.workspace.server.WorkspaceManager;
import org.eclipse.che.api.workspace.server.event.BeforeWorkspaceRemovedEvent;
import org.eclipse.che.api.workspace.shared.Constants;
import org.eclipse.che.api.workspace.shared.dto.event.WorkspaceStatusEvent;
import org.eclipse.che.api.workspace.shared.event.WorkspaceCreatedEvent;
import org.eclipse.che.commons.schedule.ScheduleDelay;
import org.eclipse.che.core.db.cascade.CascadeEventSubscriber;
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

  public static final long MINIMAL_TIMEOUT = 300_000L;

  private static final Logger LOG = LoggerFactory.getLogger(WorkspaceActivityManager.class);

  private static final String ACTIVITY_CHECKER = "activity-checker";

  private final long defaultTimeout;
  private final WorkspaceActivityDao activityDao;
  private final EventService eventService;
  private final EventSubscriber<WorkspaceStatusEvent> workspaceEventsSubscriber;
  private final EventSubscriber<WorkspaceCreatedEvent> workspaceCreatedSubscriber;
  private final EventSubscriber<BeforeWorkspaceRemovedEvent> workspaceRemoveSubscriber;

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
    if (timeout > 0 && timeout < MINIMAL_TIMEOUT) {
      LOG.warn(
          "Value of property \"che.limits.workspace.idle.timeout\" is below recommended minimum ("
              + TimeUnit.MILLISECONDS.toMinutes(MINIMAL_TIMEOUT)
              + " minutes). This may cause problems with workspace components startup and/or premature workspace shutdown.");
    }

    //noinspection Convert2Lambda
    this.workspaceCreatedSubscriber =
        new EventSubscriber<WorkspaceCreatedEvent>() {
          @Override
          public void onEvent(WorkspaceCreatedEvent event) {
            try {
              long createdTime =
                  Long.parseLong(
                      event.getWorkspace().getAttributes().get(Constants.CREATED_ATTRIBUTE_NAME));
              activityDao.setCreatedTime(event.getWorkspace().getId(), createdTime);
            } catch (ServerException | NumberFormatException x) {
              LOG.warn("Failed to record workspace created time in workspace activity.", x);
            }
          }
        };

    this.workspaceRemoveSubscriber =
        new CascadeEventSubscriber<BeforeWorkspaceRemovedEvent>() {
          @Override
          public void onCascadeEvent(BeforeWorkspaceRemovedEvent event) throws Exception {
            activityDao.removeActivity(event.getWorkspace().getId());
          }
        };

    //noinspection Convert2Lambda
    this.workspaceEventsSubscriber =
        new EventSubscriber<WorkspaceStatusEvent>() {
          @Override
          public void onEvent(WorkspaceStatusEvent event) {
            long now = System.currentTimeMillis();
            String workspaceId = event.getWorkspaceId();
            WorkspaceStatus status = event.getStatus();

            // first, record the activity
            try {
              activityDao.setStatusChangeTime(workspaceId, status, now);
            } catch (ServerException e) {
              LOG.warn(
                  String.format(
                      "Failed to record workspace activity. Workspace: %s, status: %s",
                      workspaceId, status.toString()),
                  e);
            }

            // now do any special handling
            switch (status) {
              case RUNNING:
                try {
                  Workspace workspace = workspaceManager.getWorkspace(workspaceId);
                  if (workspace.getAttributes().remove(WORKSPACE_STOPPED_BY) != null) {
                    workspaceManager.updateWorkspace(workspaceId, workspace);
                  }
                } catch (Exception ex) {
                  LOG.warn(
                      "Failed to remove stopped information attribute for workspace "
                          + workspaceId);
                }
                WorkspaceActivityManager.this.update(workspaceId, now);
                break;
              case STOPPED:
                try {
                  activityDao.removeExpiration(workspaceId);
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
        activityDao.setExpirationTime(wsId, activityTime + timeout);
      }
    } catch (ServerException e) {
      LOG.error(e.getLocalizedMessage(), e);
    }
  }

  public Page<String> findWorkspacesInStatus(
      WorkspaceStatus status, long threshold, int maxItems, long skipCount) throws ServerException {
    return activityDao.findInStatusSince(threshold, status, maxItems, skipCount);
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
    eventService.subscribe(workspaceEventsSubscriber, WorkspaceStatusEvent.class);
    eventService.subscribe(workspaceCreatedSubscriber, WorkspaceCreatedEvent.class);
    eventService.subscribe(workspaceRemoveSubscriber, BeforeWorkspaceRemovedEvent.class);
  }
}
