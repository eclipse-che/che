/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.workspace.server;

import static org.eclipse.che.api.system.server.DtoConverter.asDto;

import com.google.common.base.Preconditions;
import java.util.Collections;
import java.util.Set;
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
import org.eclipse.che.api.system.server.CronThreadPullTermination;
import org.eclipse.che.api.system.server.ServiceTermination;
import org.eclipse.che.api.system.shared.event.service.SystemServiceItemStoppedEvent;
import org.eclipse.che.api.system.shared.event.service.SystemServiceItemSuspendedEvent;
import org.eclipse.che.api.system.shared.event.service.SystemServiceStoppedEvent;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.RuntimeInfrastructure;
import org.eclipse.che.api.workspace.shared.dto.event.WorkspaceStatusEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Terminates workspace service. In case of full system shutdown or if current infra doesn't support
 * workspaces recovery, it blocks starting new workspaces and stops all that already running. In
 * case of suspend and recovery support, blocks starting new workspaces and waits until all
 * workspaces that are currently in a starting/stopping state to finish this process and become
 * stable running or stopped state.
 *
 * @author Yevhenii Voevodin
 * @author Max Shaposhnyk
 */
public class WorkspaceServiceTermination implements ServiceTermination {

  private static final Logger LOG = LoggerFactory.getLogger(WorkspaceServiceTermination.class);

  /** Delay in MS between runtimes stopped checks. The value is experimental. */
  private static final long DEFAULT_PULL_RUNTIMES_PERIOD_MS = TimeUnit.SECONDS.toMillis(1);

  public static final String SERVICE_NAME = "workspace";

  private final WorkspaceManager manager;
  private final WorkspaceSharedPool sharedPool;
  private final WorkspaceRuntimes runtimes;
  private final RuntimeInfrastructure runtimeInfrastructure;
  private final EventService eventService;
  private final TemporaryWorkspaceRemover workspaceRemover;

  @Inject
  public WorkspaceServiceTermination(
      WorkspaceManager manager,
      WorkspaceSharedPool sharedPool,
      WorkspaceRuntimes runtimes,
      RuntimeInfrastructure runtimeInfrastructure,
      EventService eventService,
      TemporaryWorkspaceRemover workspaceRemover) {
    this.manager = manager;
    this.sharedPool = sharedPool;
    this.runtimes = runtimes;
    this.runtimeInfrastructure = runtimeInfrastructure;
    this.eventService = eventService;
    this.workspaceRemover = workspaceRemover;
  }

  @Override
  public String getServiceName() {
    return SERVICE_NAME;
  }

  @Override
  public Set<String> getDependencies() {
    return Collections.singleton(CronThreadPullTermination.SERVICE_NAME);
  }

  /**
   * Blocks starting new workspaces and stops all that already running
   *
   * @throws InterruptedException
   */
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
    try {
      workspaceRemover.shutdown();
    } catch (Exception ignored) {
    }
  }

  /**
   * Blocks starting new workspaces and waits until all workspaces that are currently in a
   * starting/stopping state to finish this process
   *
   * @throws InterruptedException
   * @throws UnsupportedOperationException
   */
  @Override
  public void suspend() throws InterruptedException, UnsupportedOperationException {
    try {
      runtimeInfrastructure.getIdentities();
    } catch (UnsupportedOperationException | InfrastructureException e) {
      throw new UnsupportedOperationException("Current infrastructure does not support suspend.");
    }
    Preconditions.checkState(runtimes.refuseStart());
    WorkspaceSuspendedEventsPropagator propagator = new WorkspaceSuspendedEventsPropagator();
    eventService.subscribe(propagator);
    try {
      waitAllWorkspacesRunningOrStopped();
      sharedPool.shutdown();
    } finally {
      eventService.unsubscribe(propagator);
    }
    try {
      workspaceRemover.shutdown();
    } catch (Exception ignored) {
    }
  }

  private void stopRunningAndStartingWorkspacesAsync() {
    for (String workspaceId : runtimes.getRunning()) {
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
      this.totalRunning = runtimes.getRunning().size();
      this.currentlyStopped = new AtomicInteger(0);
    }

    @Override
    public void onEvent(WorkspaceStatusEvent event) {
      if (event.getStatus() == WorkspaceStatus.STOPPED) {
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

  /** Propagates workspace suspended events as {@link SystemServiceItemSuspendedEvent} events. */
  private class WorkspaceSuspendedEventsPropagator
      implements EventSubscriber<WorkspaceStatusEvent> {

    private final int totalRunning;
    private final AtomicInteger currentlyStopped;

    private WorkspaceSuspendedEventsPropagator() {
      this.totalRunning = runtimes.getInProgress().size();
      this.currentlyStopped = new AtomicInteger(0);
    }

    @Override
    public void onEvent(WorkspaceStatusEvent event) {
      if (event.getStatus() == WorkspaceStatus.STOPPED
          || event.getStatus() == WorkspaceStatus.RUNNING) {
        eventService.publish(
            asDto(
                new SystemServiceItemSuspendedEvent(
                    getServiceName(),
                    event.getWorkspaceId(),
                    currentlyStopped.incrementAndGet(),
                    totalRunning)));
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
