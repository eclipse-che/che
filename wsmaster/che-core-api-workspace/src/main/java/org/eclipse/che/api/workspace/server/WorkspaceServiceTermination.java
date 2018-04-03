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
package org.eclipse.che.api.workspace.server;

import static org.eclipse.che.api.system.server.DtoConverter.asDto;

import com.google.common.base.Preconditions;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import javax.inject.Inject;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.system.server.ServiceTermination;
import org.eclipse.che.api.system.shared.event.service.SystemServiceItemStoppedEvent;
import org.eclipse.che.api.system.shared.event.service.SystemServiceStoppedEvent;
import org.eclipse.che.api.workspace.shared.dto.event.WorkspaceStatusEvent;

/**
 * Terminates workspace service.
 *
 * @author Yevhenii Voevodin
 */
public class WorkspaceServiceTermination implements ServiceTermination {

  /** Delay in MS between runtimes stopped checks. The value is experimental. */
  private static final long DEFAULT_PULL_RUNTIMES_PERIOD_MS = TimeUnit.SECONDS.toMillis(1);

  private final WorkspaceSharedPool sharedPool;
  private final WorkspaceRuntimes runtimes;
  private final EventService eventService;

  @Inject
  public WorkspaceServiceTermination(
      WorkspaceSharedPool sharedPool, WorkspaceRuntimes runtimes, EventService eventService) {
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
      waitAllWorkspacesRunningOrStopped();
      sharedPool.shutdown();
    } finally {
      eventService.unsubscribe(propagator);
    }
  }

  /** Propagates workspace stopped events as {@link SystemServiceStoppedEvent} events. */
  private class WorkspaceStoppedEventsPropagator implements EventSubscriber<WorkspaceStatusEvent> {

    private final int totalRunning;
    private final AtomicInteger currentlyStopped;

    private WorkspaceStoppedEventsPropagator() {
      this.totalRunning = runtimes.getInProgress().size();
      this.currentlyStopped = new AtomicInteger(0);
    }

    @Override
    public void onEvent(WorkspaceStatusEvent event) {
      if (event.getStatus() == WorkspaceStatus.STOPPED
          || event.getStatus() == WorkspaceStatus.RUNNING) {
        eventService.publish(
            asDto(
                new SystemServiceItemStoppedEvent(
                    getServiceName(),
                    event.getWorkspaceId(),
                    currentlyStopped.incrementAndGet(),
                    totalRunning)));
      }
    }
  }

  private void waitAllWorkspacesRunningOrStopped() throws InterruptedException {
    Timer timer = new Timer("RuntimesStoppedTracker", false);
    CountDownLatch latch = new CountDownLatch(1);
    timer.schedule(
        new TimerTask() {
          @Override
          public void run() {
            if (!runtimes.isAnyInProgress()) {
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
