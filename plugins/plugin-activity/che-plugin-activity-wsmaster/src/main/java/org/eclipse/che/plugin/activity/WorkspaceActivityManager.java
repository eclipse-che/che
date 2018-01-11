/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.activity;

import static java.util.Collections.emptyMap;
import static org.eclipse.che.activity.shared.Constants.ACTIVITY_CHECKER;
import static org.eclipse.che.api.workspace.shared.Constants.WORKSPACE_STOPPED_BY;

import com.google.common.annotations.VisibleForTesting;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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
import org.eclipse.che.commons.schedule.ScheduleRate;
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

  private final long timeout;
  private final Map<String, Long> activeWorkspaces;
  private final EventService eventService;
  private final EventSubscriber<?> workspaceEventsSubscriber;

  protected final WorkspaceManager workspaceManager;

  @Inject
  public WorkspaceActivityManager(
      WorkspaceManager workspaceManager,
      EventService eventService,
      @Named("che.workspace.agent.dev.inactive_stop_timeout_ms") long timeout) {
    this.timeout = timeout;
    this.workspaceManager = workspaceManager;
    this.eventService = eventService;
    this.activeWorkspaces = new ConcurrentHashMap<>();
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
                activeWorkspaces.remove(event.getWorkspaceId());
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
        activeWorkspaces.put(wsId, activityTime + timeout);
      }
    } catch (NotFoundException | ServerException e) {
      LOG.error(e.getLocalizedMessage(), e);
    }
  }

  protected long getIdleTimeout(String workspaceId) throws NotFoundException, ServerException {
    if (timeout > 0) {
      return timeout;
    } else {
      return -1;
    }
  }

  @ScheduleRate(periodParameterName = "che.workspace.activity_check_scheduler_period_s")
  private void invalidate() {
    final long currentTime = System.currentTimeMillis();
    for (Map.Entry<String, Long> workspaceExpireEntry : activeWorkspaces.entrySet()) {
      if (workspaceExpireEntry.getValue() <= currentTime) {
        try {
          String workspaceId = workspaceExpireEntry.getKey();
          Workspace workspace = workspaceManager.getWorkspace(workspaceId);
          workspace.getAttributes().put(WORKSPACE_STOPPED_BY, ACTIVITY_CHECKER);
          workspaceManager.updateWorkspace(workspaceId, workspace);
          workspaceManager.stopWorkspace(workspaceId, emptyMap());
        } catch (NotFoundException ignored) {
          // workspace no longer exists, no need to do anything
        } catch (ConflictException e) {
          LOG.warn(e.getLocalizedMessage());
        } catch (Exception ex) {
          LOG.error(ex.getLocalizedMessage());
          LOG.debug(ex.getLocalizedMessage(), ex);
        } finally {
          activeWorkspaces.remove(workspaceExpireEntry.getKey());
        }
      }
    }
  }

  @VisibleForTesting
  @PostConstruct
  public void subscribe() {
    eventService.subscribe(workspaceEventsSubscriber);
  }
}
