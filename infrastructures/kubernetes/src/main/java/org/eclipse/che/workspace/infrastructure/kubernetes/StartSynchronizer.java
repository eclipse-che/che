/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.kubernetes;

import com.google.inject.assistedinject.Assisted;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalInfrastructureException;
import org.eclipse.che.api.workspace.server.spi.RuntimeStartInterruptedException;
import org.eclipse.che.workspace.infrastructure.kubernetes.event.KubernetesRuntimeStoppedEvent;
import org.eclipse.che.workspace.infrastructure.kubernetes.event.KubernetesRuntimeStoppingEvent;

/**
 * Controls the runtime start flow and helps to cancel it.
 *
 * <p>The runtime start with cancellation using the Start Synchronizer might look like:
 *
 * <pre>
 * ...
 *   public void startRuntime() {
 *     startSynchronizer.setStartThread();
 *     try {
 *       startMachines();
 *       startSynchronizer.checkFailure();
 *
 *       // ...
 *
 *       waitMachines()
 *         .then(startSynchronizer.checkFailure())
 *         .then(bootstrapAsync())
 *         .then(startSynchronizer.checkFailure())
 *         .then(checkServers)
 *         // ...
 *
 *       startSynchronizer.complete();
 *     } catch (Exception ex) {
 *       startSynchronizer.completeExceptionally(ex);
 *       throw ex;
 *     }
 *   }
 * ...
 * </pre>
 *
 * <p>At the same time stopping might look like:
 *
 * <pre>
 * ...
 *   public void stopRuntime() {
 *     if (startSynchronizer.interrupt()) {
 *       try {
 *         if (startSynchronizer.awaitInterruption(30)) {
 *           // runtime is interrupted correctly
 *         } else {
 *           // runtime is not interrupted correctly in time
 *           // need to forcibly stop it and clean up used resources
 *         }
 *       } catch (RuntimeStartInterruptedException ex) {
 *         // normal stop
 *       } catch (InterruptedException ex) {
 *         Thread.currentThread().interrupt();
 *         ...
 *       }
 *     }
 *   }
 * ...
 * </pre>
 *
 * <p>Note that class is designed to work in clustered mode. For this purpose it listens to {@link
 * KubernetesRuntimeStoppedEvent} and {@link KubernetesRuntimeStoppingEvent} events. So, if Che
 * Server is run in clustered mode then the described events must be published via {@link
 * EventService} otherwise start interruption won't work correctly.
 *
 * @author Sergii Leshchenko
 */
public class StartSynchronizer {

  private final RuntimeIdentity runtimeId;
  private final EventService eventService;

  // future that holds error that occurs during runtime start
  // failure must be completed with null value when start is finished without any exception
  private final CompletableFuture<Void> startFailure;
  private final long workspaceStartTimeoutMillis;

  // latch that indicates whether start is completed or not
  private CountDownLatch completionLatch;

  // thread which is performing start
  // it may be nullable when start is performing on another Che Server instance
  private Thread startThread;

  // flag that indicates whether start is in progress or not
  private boolean isStarting;
  private long startTimeMillis;

  private final RuntimeStartInterrupter runtimeStartInterrupter;
  private final RuntimeStopWatcher runtimeStopWatcher;

  @Inject
  public StartSynchronizer(
      EventService eventService,
      @Named("che.infra.kubernetes.workspace_start_timeout_min") int workspaceStartTimeoutMin,
      @Assisted RuntimeIdentity runtimeId) {
    this.eventService = eventService;
    this.startFailure = new CompletableFuture<>();
    this.completionLatch = new CountDownLatch(0);
    this.runtimeId = runtimeId;
    this.runtimeStartInterrupter = new RuntimeStartInterrupter();
    this.runtimeStopWatcher = new RuntimeStopWatcher();
    this.isStarting = false;
    this.workspaceStartTimeoutMillis = TimeUnit.MINUTES.toMillis(workspaceStartTimeoutMin);
  }

  /** Registers a runtime start. */
  public synchronized void start() {
    if (!isStarting) {
      isStarting = true;
      startTimeMillis = System.currentTimeMillis();
      completionLatch = new CountDownLatch(1);
      eventService.subscribe(runtimeStartInterrupter, KubernetesRuntimeStoppingEvent.class);
      eventService.subscribe(runtimeStopWatcher, KubernetesRuntimeStoppedEvent.class);
    }
  }

  /**
   * Sets {@link Thread#currentThread()} as a {@link #startThread}.
   *
   * @throws InternalInfrastructureException when {@link #startThread} is already set.
   */
  public synchronized void setStartThread() throws InternalInfrastructureException {
    if (startThread != null) {
      throw new InternalInfrastructureException("Runtime is already started");
    }
    startThread = Thread.currentThread();
  }

  /**
   * Registers start completion.
   *
   * <p>It also releases {@link #awaitInterruption(long, TimeUnit)}.
   *
   * @throws RuntimeStartInterruptedException when start was interrupted
   * @throws InfrastructureException when any other exception occurs during start
   */
  public synchronized void complete() throws InfrastructureException {
    completionLatch.countDown();
    startThread = null;
    isStarting = false;
    eventService.unsubscribe(runtimeStartInterrupter);
    eventService.unsubscribe(runtimeStopWatcher);

    // try to complete start failure holder and
    // rethrow original exception if it is already completed exceptionally
    if (!startFailure.complete(null) && startFailure.isCompletedExceptionally()) {
      try {
        // future is already completed.
        startFailure.getNow(null);
      } catch (CompletionException e) {
        rethrowCause(e);
      }
    }
  }

  /**
   * Registers exception that occurs during runtime start.
   *
   * <p>Note that only first exception will be saved.
   *
   * <p>It also releases {@link #awaitInterruption(long, TimeUnit)}.
   */
  public synchronized void completeExceptionally(Exception ex) {
    startFailure.completeExceptionally(ex);

    completionLatch.countDown();
    startThread = null;
    isStarting = false;
    eventService.unsubscribe(runtimeStartInterrupter);
    eventService.unsubscribe(runtimeStopWatcher);
  }

  /**
   * Checks if start is failed.
   *
   * <p>If yes then original exception will be rethrown.
   *
   * @throws RuntimeStartInterruptedException when start was interrupted
   * @throws InfrastructureException when any other exception occurs during start
   */
  public void checkFailure() throws InfrastructureException {
    try {
      startFailure.getNow(null);
      // no exception hasn't occurred yet
    } catch (CompletionException e) {
      rethrowCause(e);
    }
  }

  /** Returns future that holds error that occurs during runtime start */
  public CompletableFuture<Void> getStartFailure() {
    return startFailure;
  }

  /**
   * Interrupts workspace start if it is in progress and is not interrupted yet
   *
   * @return true if workspace start is interrupted, false otherwise
   */
  public synchronized boolean interrupt() {
    if (!isStarting) {
      return false;
    }
    startFailure.completeExceptionally(new RuntimeStartInterruptedException(runtimeId));

    if (startThread != null) {
      startThread.interrupt();
      // set to not to interrupt twice
      startThread = null;
    }

    return true;
  }

  /**
   * Awaits until workspace start process will be completed.
   *
   * <p>Returns true is interruption is completed. Or returns false when interruption wasn't
   * received or processed by start thread and workspace is STARTING or STOPPING. So it's needed to
   * stop a runtime and clean up used resources.
   *
   * @throws InterruptedException if the current thread is interrupted while waiting
   */
  public boolean awaitInterruption(long timeout, TimeUnit unit) throws InterruptedException {
    boolean isCompleted = completionLatch.await(timeout, unit);

    if (isCompleted) {
      // check start failure
      try {
        startFailure.get();
      } catch (ExecutionException e) {
        // start is interrupted or failed. Resources are freed
        return true;
      }
    }
    // runtime start is still in progress
    return false;
  }

  /** Returns true if start is completed, false otherwise. */
  public boolean isCompleted() {
    return completionLatch.getCount() == 0;
  }

  /** Returns start failure exception if occurred otherwise null will be returned. */
  public InfrastructureException getStartFailureNow() {
    try {
      startFailure.getNow(null);
      return null;
    } catch (CompletionException e) {
      Throwable cause = e.getCause();
      if (cause instanceof InfrastructureException) {
        return (InfrastructureException) cause;
      } else {
        return new InternalInfrastructureException(cause.getMessage(), cause);
      }
    }
  }

  private void rethrowCause(Throwable e) throws InfrastructureException {
    try {
      throw e.getCause();
    } catch (InfrastructureException ex) {
      throw ex;
    } catch (Throwable ex) {
      throw new InternalInfrastructureException(ex.getMessage(), ex);
    }
  }

  /**
   * Returns time before workspace start should be interrupted or 0 if runtime start should be
   * interrupted now.
   */
  public long getStartTimeoutMillis() {
    long deadLine = startTimeMillis + workspaceStartTimeoutMillis;
    long timeout = deadLine - System.currentTimeMillis();
    return Math.max(0, timeout);
  }

  /**
   * Listens {@link KubernetesRuntimeStoppingEvent} and interrupts workspace start when workspace
   * become {@link WorkspaceStatus#STOPPING}.
   */
  private class RuntimeStartInterrupter implements EventSubscriber<KubernetesRuntimeStoppingEvent> {
    @Override
    public void onEvent(KubernetesRuntimeStoppingEvent event) {
      if (event.getWorkspaceId().equals(runtimeId.getWorkspaceId())) {
        interrupt();
      }
    }
  }

  /**
   * Listens {@link KubernetesRuntimeStoppedEvent} and releases {@link #completionLatch} when
   * workspace become {@link WorkspaceStatus#STOPPED}.
   */
  private class RuntimeStopWatcher implements EventSubscriber<KubernetesRuntimeStoppedEvent> {
    @Override
    public void onEvent(KubernetesRuntimeStoppedEvent event) {
      if (event.getWorkspaceId().equals(runtimeId.getWorkspaceId())) {
        try {
          // try to complete start it if is not completed yet
          complete();
        } catch (InfrastructureException ignored) {
        }
      }
    }
  }
}
