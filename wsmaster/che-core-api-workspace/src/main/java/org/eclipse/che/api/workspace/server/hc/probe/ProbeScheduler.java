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
package org.eclipse.che.api.workspace.server.hc.probe;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.workspace.server.hc.probe.ProbeResult.ProbeStatus;
import org.eclipse.che.commons.observability.ExecutorServiceWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Schedules workspace servers probes checks asynchronously.
 *
 * @author Alexander Garagatyi
 * @author Sergii Leshchenko
 */
@Singleton
public class ProbeScheduler {
  private static final Logger LOG = LoggerFactory.getLogger(ProbeScheduler.class);

  private final ScheduledExecutorService probesExecutor;
  /**
   * Use single thread for a scheduling of tasks interruption by timeout. Single thread can be used
   * since it is supposed that interruption is a very quick call. Separate thread is needed to
   * prevent a situation when executor is full of jobs and current ones are hanging but we need to
   * time them out.
   */
  private final Timer timeouts;
  /** Mapping of workspaceId to a list of futures with probes of a workspace. */
  private final Map<String, List<ScheduledFuture>> probesFutures;

  @Inject
  public ProbeScheduler(
      @Named("che.workspace.probe_pool_size") int probeSchedulerPoolSize,
      ExecutorServiceWrapper executorServiceWrapper) {
    probesExecutor =
        executorServiceWrapper.wrap(
            new ScheduledThreadPoolExecutor(
                probeSchedulerPoolSize,
                new ThreadFactoryBuilder()
                    .setDaemon(true)
                    .setNameFormat("ServerProbes-%s")
                    .build()),
            ProbeScheduler.class.getName());
    timeouts = new Timer("ServerProbesTimeouts", true);
    probesFutures = new ConcurrentHashMap<>();
  }

  /**
   * Schedules provided {@link WorkspaceProbes} and uses provided {@code Consumer<ProbeResult>} to
   * send probes results. Respects {@link ProbeConfig} parameters such as thresholds, timeouts. Note
   * that probe execution is not deleted automatically when probe results are passed to probe
   * results consumer. To stop a probe execution method {@link #cancel(String)} should be used.
   *
   * @param probes probes to check
   * @param probeResultConsumer consumer of {@link ProbeResult} instances produced on retrieving
   *     probe execution results
   * @throws RejectedExecutionException when {@link ProbeScheduler} is terminated
   */
  public void schedule(WorkspaceProbes probes, Consumer<ProbeResult> probeResultConsumer) {
    probesFutures.putIfAbsent(probes.getWorkspaceId(), new ArrayList<>());
    probes
        .getProbes()
        .forEach(
            probeFactory -> schedule(probes.getWorkspaceId(), probeFactory, probeResultConsumer));
  }

  /**
   * Schedules provided {@link WorkspaceProbes} when a workspace becomes {@link
   * WorkspaceStatus#RUNNING}.
   *
   * <p>Note that probes scheduling will be canceled when {@link #cancel(String)} is called or when
   * a workspace becomes {@link WorkspaceStatus#STOPPING} or {@link WorkspaceStatus#STOPPED}.
   *
   * @param probes probes to schedule
   * @param probeResultConsumer consumer of {@link ProbeResult} instances produced on retrieving
   *     probe execution results
   * @param statusSupplier supplier to retrieve workspace status. Scheduling will be delayed if
   *     {@link RuntimeException} is thrown
   * @see #schedule(WorkspaceProbes, Consumer)
   */
  public void schedule(
      WorkspaceProbes probes,
      Consumer<ProbeResult> probeResultConsumer,
      Supplier<WorkspaceStatus> statusSupplier) {
    DelayedSchedulingTask task =
        new DelayedSchedulingTask(statusSupplier, probes, probeResultConsumer);

    // scheduleWithFixedDelay is used in favor of scheduleAtFixedRate because in case of big amount
    // of scheduled probes start time of tasks may shift and this may lead to a situation when
    // another probeConfig is needed immediately after the previous one is finished which doesn't
    // seem a good thing
    ScheduledFuture scheduledFuture =
        probesExecutor.scheduleWithFixedDelay(task, 10L, 10L, TimeUnit.SECONDS);

    probesFutures.compute(
        probes.getWorkspaceId(),
        (key, scheduledFutures) -> {
          List<ScheduledFuture> target = scheduledFutures;
          if (target == null) {
            target = new ArrayList<>();
          }
          target.add(scheduledFuture);
          return target;
        });
  }

  /**
   * Dismisses following and if possible current executions of probes of a workspace with a
   * specified ID.
   */
  public void cancel(String workspaceId) {
    List<ScheduledFuture> tasks = probesFutures.remove(workspaceId);
    if (tasks != null) {
      tasks.forEach(task -> task.cancel(true));
    }
  }

  /** Denies starting of new probes and terminates active one if scheduler not terminated yet. */
  public void shutdown() {
    if (!probesExecutor.isShutdown()) {
      probesExecutor.shutdown();
      try {
        LOG.info("Shutdown probe scheduler, wait 30s to stop normally");
        if (!probesExecutor.awaitTermination(30, TimeUnit.SECONDS)) {
          probesExecutor.shutdownNow();
          LOG.info("Interrupt probe scheduler, wait 60s to stop");
          if (!probesExecutor.awaitTermination(60, TimeUnit.SECONDS)) {
            LOG.error("Couldn't shutdown probe scheduler threads pool");
          } else {
            LOG.info("Probe scheduler threads pool is interrupted");
          }
        } else {
          LOG.info("Probe scheduler threads pool is shut down");
        }
      } catch (InterruptedException x) {
        probesExecutor.shutdownNow();
        Thread.currentThread().interrupt();
      }
    }
  }

  private void schedule(
      String workspaceId, ProbeFactory probeFactory, Consumer<ProbeResult> probeResultConsumer) {
    ProbeConfig probeConfig = probeFactory.getProbeConfig();
    Task task = new Task(probeFactory, probeResultConsumer);
    // scheduleWithFixedDelay is used in favor of scheduleAtFixedRate because in case of big amount
    // of scheduled probes start time of tasks may shift and this may lead to a situation when
    // another probeConfig is needed immediately after the previous one is finished which doesn't
    // seem a good thing
    ScheduledFuture scheduledFuture =
        probesExecutor.scheduleWithFixedDelay(
            task,
            probeConfig.getInitialDelaySeconds(),
            probeConfig.getPeriodSeconds(),
            TimeUnit.SECONDS);

    List<ScheduledFuture> workspaceProbes =
        probesFutures.computeIfPresent(
            workspaceId,
            (key, scheduledFutures) -> {
              scheduledFutures.add(scheduledFuture);
              return scheduledFutures;
            });
    // check whether workspace probes were cancelled concurrently which led to removal of the value
    // in the map
    if (workspaceProbes == null) {
      scheduledFuture.cancel(true);
      task.cancel();
    }
  }

  private class Task implements Runnable {
    private final ProbeFactory probeFactory;
    private final Consumer<ProbeResult> probeResultConsumer;
    private final ProbeConfig probeConfig;

    private int failures = 0;
    private int successes = 0;
    private AtomicBoolean cancelled = new AtomicBoolean(false);

    public Task(ProbeFactory probeFactory, Consumer<ProbeResult> probeResultConsumer) {
      this.probeFactory = probeFactory;
      this.probeConfig = probeFactory.getProbeConfig();
      this.probeResultConsumer = probeResultConsumer;
    }

    @Override
    public void run() {
      if (cancelled.get()) {
        return;
      }
      Probe probe = probeFactory.get();
      TimeoutProbeTask timeoutProbeTask = new TimeoutProbeTask(probe);
      timeouts.schedule(
          timeoutProbeTask, TimeUnit.SECONDS.toMillis(probeConfig.getTimeoutSeconds()));
      boolean success = probe.probe();
      timeoutProbeTask.cancel();
      if (success) {
        // current success increases successes count and clears failures count
        successes++;
        failures = 0;

        if (successes >= probeConfig.getSuccessThreshold()) {
          // TODO make them completable futures? Then we should ensure that
          // consecutive calls won't use executors thread time but rather use common thread
          // pool to perform processing of passed probe result
          if (cancelled.get()) {
            return;
          }
          // Health check satisfies probeConfig health conditions
          probeResultConsumer.accept(
              new ProbeResult(
                  probeFactory.getWorkspaceId(),
                  probeFactory.getMachineName(),
                  probeFactory.getServerName(),
                  ProbeStatus.PASSED));
        }
      } else {
        // current failure increases failures count and clears successes count
        failures++;
        successes = 0;

        if (failures >= probeConfig.getFailureThreshold()) {
          if (cancelled.get()) {
            return;
          }
          // Health check satisfies probeConfig failure conditions
          probeResultConsumer.accept(
              new ProbeResult(
                  probeFactory.getWorkspaceId(),
                  probeFactory.getMachineName(),
                  probeFactory.getServerName(),
                  ProbeStatus.FAILED));
        }
      }
    }

    public void cancel() {
      cancelled.set(true);
    }
  }

  private class DelayedSchedulingTask implements Runnable {
    private final String workspaceId;
    private final Supplier<WorkspaceStatus> statusSupplier;
    private final WorkspaceProbes probes;
    private final Consumer<ProbeResult> probeResultConsumer;

    DelayedSchedulingTask(
        Supplier<WorkspaceStatus> statusSupplier,
        WorkspaceProbes probes,
        Consumer<ProbeResult> probeResultConsumer) {
      this.workspaceId = probes.getWorkspaceId();
      this.statusSupplier = statusSupplier;
      this.probes = probes;
      this.probeResultConsumer = probeResultConsumer;
    }

    @Override
    public void run() {
      WorkspaceStatus status;

      try {
        status = statusSupplier.get();
      } catch (RuntimeException e) {
        // delay
        return;
      }

      switch (status) {
        case STARTING:
          // delay
          return;
        case RUNNING:
          ProbeScheduler.this.cancel(workspaceId);
          schedule(probes, probeResultConsumer);
          return;
        case STOPPED:
        case STOPPING:
        default:
          ProbeScheduler.this.cancel(workspaceId);
      }
    }
  }

  private class TimeoutProbeTask extends TimerTask {
    private final Probe probe;

    public TimeoutProbeTask(Probe probe) {
      this.probe = probe;
    }

    @Override
    public void run() {
      probe.cancel();
    }
  }
}
