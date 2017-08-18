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
package org.eclipse.che.api.workspace.server;

import com.google.common.base.Preconditions;
import java.util.Collections;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import javax.inject.Inject;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.system.server.ServiceTermination;
import org.eclipse.che.api.system.shared.event.service.SystemServiceItemStoppedEvent;
import org.eclipse.che.api.system.shared.event.service.SystemServiceStoppedEvent;
import org.eclipse.che.api.workspace.shared.dto.event.WorkspaceStatusEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Terminates workspace service.
 *
 * @author Yevhenii Voevodin
 */
public class WorkspaceServiceTermination implements ServiceTermination {

  private static final Logger LOG = LoggerFactory.getLogger(WorkspaceServiceTermination.class);

  /** Delay in MS between runtimes stopped checks. The value is experimental. */
  private static final long DEFAULT_PULL_RUNTIMES_PERIOD_MS = TimeUnit.SECONDS.toMillis(1);

  private final WorkspaceManager manager;
  private final WorkspaceSharedPool sharedPool;
  private final WorkspaceRuntimes runtimes;
  private final EventService eventService;

  @Inject
  public WorkspaceServiceTermination(
      WorkspaceManager manager,
      WorkspaceSharedPool sharedPool,
      WorkspaceRuntimes runtimes,
      EventService eventService) {
    this.manager = manager;
    this.sharedPool = sharedPool;
    this.runtimes = runtimes;
    this.eventService = eventService;
  }

  @Override
  public String getServiceName() {
    return "workspace";
  }

  @Override
  public void terminate() throws InterruptedException {
    Preconditions.checkState(runtimes.refuseStart());

    WorkspaceStoppedEventsPropagator propagator = new WorkspaceStoppedEventsPropagator();
    eventService.subscribe(propagator);
    try {
      stopRunningAndStartingWorkspacesAsync();
      waitAllWorkspacesStopped();
      sharedPool.shutdown();
    } finally {
      eventService.unsubscribe(propagator);
    }
  }

  private void stopRunningAndStartingWorkspacesAsync() {
    for (String workspaceId : runtimes.getRuntimesIds()) {
      WorkspaceStatus status = runtimes.getStatus(workspaceId);
      if (status == WorkspaceStatus.RUNNING || status == WorkspaceStatus.STARTING) {
        try {
          manager.stopWorkspace(workspaceId, Collections.emptyMap());
        } catch (ServerException | ConflictException | NotFoundException x) {
          if (runtimes.hasRuntime(workspaceId)) {
            LOG.error(
                "Couldn't get the workspace '{}' while it's running, the occurred error: '{}'",
                workspaceId,
                x.getMessage());
          }
        }
      }
    }
  }

  /** Propagates workspace stopped events as {@link SystemServiceStoppedEvent} events. */
  private class WorkspaceStoppedEventsPropagator implements EventSubscriber<WorkspaceStatusEvent> {

    private final int totalRunning;
    private final AtomicInteger currentlyStopped;

    private WorkspaceStoppedEventsPropagator() {
      this.totalRunning = runtimes.getRuntimesIds().size();
      this.currentlyStopped = new AtomicInteger(0);
    }

    @Override
    public void onEvent(WorkspaceStatusEvent event) {
      if (event.getStatus() == WorkspaceStatus.STOPPED) {
        eventService.publish(
            new SystemServiceItemStoppedEvent(
                getServiceName(),
                event.getWorkspaceId(),
                currentlyStopped.incrementAndGet(),
                totalRunning));
      }
    }
  }

  private void waitAllWorkspacesStopped() throws InterruptedException {
    Timer timer = new Timer("RuntimesStoppedTracker", false);
    CountDownLatch latch = new CountDownLatch(1);
    timer.schedule(
        new TimerTask() {
          @Override
          public void run() {
            if (!runtimes.isAnyRunning()) {
              timer.cancel();
              latch.countDown();
            }
          }
        },
        0,
        DEFAULT_PULL_RUNTIMES_PERIOD_MS);
    latch.await();
  }
}
