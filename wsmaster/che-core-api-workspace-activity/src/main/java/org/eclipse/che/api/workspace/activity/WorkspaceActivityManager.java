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
import java.time.Clock;
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
import org.eclipse.che.api.workspace.server.WorkspaceRuntimes;
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
  private final EventSubscriber<WorkspaceStatusEvent> updateStatusChangedTimestampSubscriber;
  private final EventSubscriber<WorkspaceCreatedEvent> setCreatedTimestampSubscriber;
  private final EventSubscriber<BeforeWorkspaceRemovedEvent> workspaceActivityRemover;

  protected final WorkspaceManager workspaceManager;
  private final WorkspaceRuntimes workspaceRuntimes;

  private final Clock clock;

  @Inject
  public WorkspaceActivityManager(
      WorkspaceManager workspaceManager,
      WorkspaceRuntimes workspaceRuntimes,
      WorkspaceActivityDao activityDao,
      EventService eventService,
      @Named("che.limits.workspace.idle.timeout") long timeout) {

    this(
        workspaceManager,
        workspaceRuntimes,
        activityDao,
        eventService,
        timeout,
        Clock.systemDefaultZone());
  }

  @VisibleForTesting
  WorkspaceActivityManager(
      WorkspaceManager workspaceManager,
      WorkspaceRuntimes workspaceRuntimes,
      WorkspaceActivityDao activityDao,
      EventService eventService,
      long timeout,
      Clock clock) {
    this.workspaceManager = workspaceManager;
    this.workspaceRuntimes = workspaceRuntimes;
    this.eventService = eventService;
    this.activityDao = activityDao;
    this.defaultTimeout = timeout;
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

  @ScheduleDelay(
      initialDelayParameterName = "che.workspace.activity_check_scheduler_delay_s",
      delayParameterName = "che.workspace.activity_check_scheduler_period_s")
  @VisibleForTesting
  void validate() {
    try {
      stopAllExpired();
      checkActivityRecordValidity();
    } catch (ServerException e) {
      LOG.error(e.getLocalizedMessage(), e);
    }
  }

  private void stopAllExpired() throws ServerException {
    activityDao.findExpired(clock.millis()).forEach(this::stopExpired);
  }

  private void checkActivityRecordValidity() throws ServerException {
    for (String runningWsId : workspaceRuntimes.getRunning()) {
      WorkspaceActivity activity = activityDao.findActivity(runningWsId);
      if (activity == null) {
        LOG.warn(
            "Found a running workspace {} without any activity record. This shouldn't really happen"
                + " but is being rectified by adding a new activity record for it.",
            runningWsId);
        try {
          createMissingActivityRecord(runningWsId);
        } catch (NotFoundException e) {
          LOG.error(
              "Detected a running workspace {} but could not find" + " its record.",
              runningWsId,
              e);
        } catch (ConflictException e) {
          LOG.debug(
              "Activity record created while we were trying to rectify its absence for a running"
                  + " workspace {}.",
              runningWsId);
        }
      } else {
        // what are we trying to handle here?
        // 1) make sure that the activity record has a valid created time
        // 2) make sure activity record as at least 1 activity record - if it lacks last_running,
        //    we look at last_starting and if even that is not there we make sure there is at least
        //    created_time - the last_* are not mandatory, but the created_time should be present
        // 3) absence of last_running - this can either happen very shortly after the workspace
        //    has started and the schedule of this method coincided with it starting. or there has
        //    been some kind of problem.
        // 4) absence of expiry - again this should only happen shortly after the workspace has
        //    started if the schedule of this method managed to read the activity before the expiry
        //    time has been persisted in the event handler. Otherwise it is a problem.

        if (activity.getCreated() == null) {
          try {
            rectifyCreatedTime(activity);
          } catch (NotFoundException e) {
            LOG.error(
                "Detected a running workspace {} but could not find" + " its record.",
                runningWsId,
                e);
          }
        }

        // let's use a single value for the current time in all the code below
        long now = clock.millis();

        boolean noLastRunningTime = activity.getLastRunning() == null;

        // this value is the last recorded activity of any kind on the workspace.
        // Even though we tried to recover the created_time in the code above, it might still happen
        // that we failed to do that and that no other activity exists on the workspace.
        // That's why in the code below we still have to account for the possibility of this value
        // being null.
        Long lastKnownActivity =
            maxOf(
                activity.getCreated(),
                activity.getLastStarting(),
                activity.getLastRunning(),
                activity.getLastStopping(),
                activity.getLastStopped());

        if (noLastRunningTime) {
          rectifyNoLastRunningTime(runningWsId, activity, now, lastKnownActivity);
        } else if (lastKnownActivity != null && lastKnownActivity > activity.getLastRunning()) {
          LOG.warn(
              "Workspace {} has been found running yet there is an activity on it newer than the"
                  + " last running time. This should not happen. Resetting the last running time to"
                  + " the newest activity time. The activity record is this: ",
              runningWsId,
              activity.toString());
          activityDao.setStatusChangeTime(runningWsId, WorkspaceStatus.RUNNING, lastKnownActivity);
          activity.setLastRunning(lastKnownActivity);
        }

        // check whether we even need to do anything about expiry. If we're configured with
        // idle-timeout of 0 then expiration is not used at all.
        long idleTimeout = getIdleTimeout(runningWsId);
        if (idleTimeout == 0) {
          continue;
        }

        if (activity.getExpiration() == null) {
          rectifyExpirationTime(
              runningWsId, activity, now, noLastRunningTime, lastKnownActivity, idleTimeout);
        }
      }
    }
  }

  private void createMissingActivityRecord(String workspaceId)
      throws ServerException, ConflictException, NotFoundException {
    Workspace workspace = workspaceManager.getWorkspace(workspaceId);
    long createdTime =
        Long.parseLong(workspace.getAttributes().get(Constants.CREATED_ATTRIBUTE_NAME));

    WorkspaceActivity activity = new WorkspaceActivity();
    activity.setWorkspaceId(workspaceId);
    activity.setCreated(createdTime);
    activity.setStatus(WorkspaceStatus.RUNNING);
    activity.setLastRunning(clock.millis());

    long idleTimeout = getIdleTimeout(workspaceId);
    if (idleTimeout > 0) {
      // only set the expiration if it is used
      activity.setExpiration(clock.millis() + idleTimeout);
    }

    activityDao.createActivity(activity);
  }

  private void rectifyCreatedTime(WorkspaceActivity activity)
      throws ServerException, NotFoundException {
    try {
      Workspace workspace = workspaceManager.getWorkspace(activity.getWorkspaceId());
      long createdTime =
          Long.parseLong(workspace.getAttributes().get(Constants.CREATED_ATTRIBUTE_NAME));
      LOG.warn(
          "Workspace {} doesn't have any information about when it was created or last seen"
              + " starting. Setting the created time to {}.",
          activity.getWorkspaceId(),
          createdTime);
      activityDao.setCreatedTime(activity.getWorkspaceId(), createdTime);
      activity.setCreated(createdTime);
    } catch (NumberFormatException e) {
      LOG.error(
          "Failed to read the created time of the workspace {} from its attributes.",
          activity.getWorkspaceId(),
          e);
    }
  }

  private void rectifyExpirationTime(
      String runningWsId,
      WorkspaceActivity activity,
      long now,
      boolean noLastRunningTime,
      Long lastKnownActivity,
      long idleTimeout) {
    // define the error message upfront to make it easier to follow the actual logic
    final String noActivityFoundWhileHandlingExpiration =
        "Found no expiration time on workspace {}. No prior activity was found on the  workspace."
            + " To restore the normal function, the expiration time has been set to {}.";
    final String noExpirationWithoutLastRunning =
        "Found no expiration time on workspace {} and no  record of the last time it started. The"
            + " expiration has been set to {}";
    final String noExpirationAfterThresholdTime =
        "Found no expiration time on workspace {}. This  was detected {}ms after the workspace has"
            + " been recorded running which is suspicious.  Please consider filing a bug report. To"
            + " restore the normal function, the expiration  time has been set to {}.";
    final String noExpirationBeforeThresholdTime =
        "Found no expiration time on workspace {}. This  was detected {}ms after the workspace has"
            + " been recorded running which is most probably caused by the schedule coinciding with"
            + " the workspace actually entering the running state. Not rectifying the expiration at"
            + " the moment and leaving that for the next iteration.";

    // first figure out the expiration time. The last running time has been initialized
    // on the activity before this method is called, so we can safely assume it is non-null here.
    long lastTime = activity.getLastRunning();

    if (lastKnownActivity == null) {
      // here, we have no prior record of any activity. Even thought the code above tried to
      // fix that, we don't want to report on the half-way fixed state. Let's just fix the
      // expiration-related part of the problem and report that we fixed it from the original
      // "condition" of the activity record.
      update(runningWsId, lastTime);
      LOG.warn(noActivityFoundWhileHandlingExpiration, runningWsId, lastTime);
    } else if (noLastRunningTime) {
      // the DB contained no record of the last time the workspace was running, but we found
      // it running anyway. The last_running time was already set to "now" in the code above
      // but we want to report about the expiration being set regardless of that fact.
      update(runningWsId, lastTime);
      LOG.warn(noExpirationWithoutLastRunning, runningWsId, lastTime);
    } else {
      // we have a record of the workspace entering the running state in the DB but we don't
      // have any expiration timestamp yet. This looks like a coincidence between the schedule
      // of this method and the workspace actually starting. Let's just give the DB a second
      // leeway before we log a warning and fix the issue. Note that that means that we only fix
      // the issue, if it still exists, on the next scheduled execution of this method.
      long timeAfterRunning = now - lastTime;

      if (timeAfterRunning > 1000) {
        update(runningWsId, lastTime);
        LOG.warn(noExpirationAfterThresholdTime, runningWsId, timeAfterRunning, lastTime);
      } else {
        LOG.debug(noExpirationBeforeThresholdTime, runningWsId, timeAfterRunning, lastTime);
      }
    }
  }

  private void rectifyNoLastRunningTime(
      String runningWsId, WorkspaceActivity activity, long now, Long lastKnownActivity)
      throws ServerException {
    // k, so we don't have the information about when the workspace was last started here.
    // This is most probably because of the coincidence of the schedule of this method and
    // the workspace being started. On the other hand, it also can happen if the wsmaster is
    // stopped at some unfortunate point in time, which would lead to it never be set until
    // the workspace is manually restarted.
    // Therefore we should do something about this. The only sensible thing to do here is to
    // persist the current time as the last running time. In case of coincidence with the
    // event handler, the difference between the 2 different timestamps will be small, so no
    // harm will be done. In the case of there being no value due to the server having been
    // interrupted in the past, we have no idea what the value might have been, so again,
    // the current time stamp seems like the best choice we have.

    activityDao.setStatusChangeTime(runningWsId, WorkspaceStatus.RUNNING, now);
    activity.setLastRunning(now);

    if (lastKnownActivity == null) {
      LOG.warn(
          "Workspace {} had no information about the last activity on it yet was found running. The"
              + " last seen running time of the workspace has been reset to {}. Please consider"
              + " filing a bug report with any suspicious log messages prior to this one.",
          runningWsId,
          now);
    } else if (lastKnownActivity < now - 300_000) {
      // if the workspace's last activity was more than 5 mins ago (improbably long time
      // for a workspace startup, pulled out of thin air), we want to log a
      // message that we're recovering the last running time, because of some weird
      // circumstances that most probably have happened in the meantime.
      LOG.warn(
          "Workspace {} had no information about the last time it has started yet was found"
              + " running. The last activity recorded on it was more than 5 minutes ago. Please"
              + " consider filing a bug report with attached logs for the period between the last"
              + " recorded activity at timestamp {} and {}. The last seen running time of the"
              + " workspace has been reset to {}.",
          runningWsId,
          lastKnownActivity,
          now,
          now);
    } else {
      LOG.debug(
          "Workspace {} had no information about"
              + " the last time it has started yet was found running. The activity record (with the"
              + " rectified last running time) looks like this: {}",
          runningWsId,
          activity);
    }
  }

  private static Long maxOf(Long... values) {
    Long max = null;
    for (Long v : values) {
      if (v == null) {
        continue;
      }

      if (max == null || v > max) {
        max = v;
      }
    }

    return max;
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
