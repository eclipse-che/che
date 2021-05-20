/*
 * Copyright (c) 2012-2020 Red Hat, Inc.
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

import static org.eclipse.che.api.workspace.shared.Constants.WORKSPACE_STOPPED_BY;

import com.google.common.annotations.VisibleForTesting;
import java.time.Clock;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
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

  private final long defaultTimeout;
  private final long runTimeout;
  private final WorkspaceActivityDao activityDao;
  private final EventService eventService;
  private final EventSubscriber<WorkspaceStatusEvent> updateStatusChangedTimestampSubscriber;
  private final EventSubscriber<WorkspaceCreatedEvent> setCreatedTimestampSubscriber;
  private final EventSubscriber<BeforeWorkspaceRemovedEvent> workspaceActivityRemover;

  protected final WorkspaceManager workspaceManager;

  private final Clock clock;

  @Inject
  public WorkspaceActivityManager(
      WorkspaceManager workspaceManager,
      WorkspaceActivityDao activityDao,
      EventService eventService,
      @Named("che.limits.workspace.idle.timeout") long timeout,
      @Named("che.limits.workspace.run.timeout") long runTimeout) {

    this(
        workspaceManager,
        activityDao,
        eventService,
        timeout,
        runTimeout,
        Clock.systemDefaultZone());
  }

  @VisibleForTesting
  WorkspaceActivityManager(
      WorkspaceManager workspaceManager,
      WorkspaceActivityDao activityDao,
      EventService eventService,
      long timeout,
      long runTimeout,
      Clock clock) {
    this.workspaceManager = workspaceManager;
    this.eventService = eventService;
    this.activityDao = activityDao;
    this.defaultTimeout = timeout;
    this.runTimeout = runTimeout;
    this.clock = clock;
    if (timeout > 0 && timeout < MINIMAL_TIMEOUT) {
      LOG.warn(
          "Value of property \"che.limits.workspace.idle.timeout\" is below recommended minimum ("
              + TimeUnit.MILLISECONDS.toMinutes(MINIMAL_TIMEOUT)
              + " minutes). This may cause problems with workspace components startup and/or premature workspace shutdown.");
    }

    //noinspection Convert2Lambda
    this.setCreatedTimestampSubscriber =
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

    this.workspaceActivityRemover =
        new CascadeEventSubscriber<BeforeWorkspaceRemovedEvent>() {
          @Override
          public void onCascadeEvent(BeforeWorkspaceRemovedEvent event) throws Exception {
            activityDao.removeActivity(event.getWorkspace().getId());
          }
        };
    this.updateStatusChangedTimestampSubscriber = new UpdateStatusChangedTimestampSubscriber();
  }

  @VisibleForTesting
  @PostConstruct
  void subscribe() {
    eventService.subscribe(updateStatusChangedTimestampSubscriber, WorkspaceStatusEvent.class);
    eventService.subscribe(setCreatedTimestampSubscriber, WorkspaceCreatedEvent.class);
    eventService.subscribe(workspaceActivityRemover, BeforeWorkspaceRemovedEvent.class);
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

  /**
   * Finds workspaces that have been in the provided status since before the provided time.
   *
   * @param status the status of the workspaces
   * @param threshold the stop-gap time
   * @param maxItems max items on the results page
   * @param skipCount how many items of the result to skip
   * @return the list of workspaces ids that have been in the provided status before the provided
   *     time.
   * @throws ServerException on error
   */
  public Page<String> findWorkspacesInStatus(
      WorkspaceStatus status, long threshold, int maxItems, long skipCount) throws ServerException {
    return activityDao.findInStatusSince(threshold, status, maxItems, skipCount);
  }

  public long countWorkspacesInStatus(WorkspaceStatus status, long threshold)
      throws ServerException {
    return activityDao.countWorkspacesInStatus(status, threshold);
  }

  protected long getIdleTimeout(String wsId) {
    return defaultTimeout;
  }

  protected long getRunTimeout() {
    return runTimeout;
  }

  private class UpdateStatusChangedTimestampSubscriber
      implements EventSubscriber<WorkspaceStatusEvent> {
    @Override
    public void onEvent(WorkspaceStatusEvent event) {
      long now = clock.millis();
      String workspaceId = event.getWorkspaceId();
      WorkspaceStatus status = event.getStatus();

      // first, record the activity
      try {
        activityDao.setStatusChangeTime(workspaceId, status, now);
      } catch (ServerException e) {
        LOG.warn(
            "Failed to record workspace activity. Workspace: {}, status: {}",
            workspaceId,
            status.toString(),
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
                "Failed to remove stopped information attribute for workspace {}", workspaceId);
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
  }
}
