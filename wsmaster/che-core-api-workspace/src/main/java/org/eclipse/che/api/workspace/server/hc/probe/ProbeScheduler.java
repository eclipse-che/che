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
package org.eclipse.che.api.workspace.server.hc.probe;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.eclipse.che.api.workspace.server.hc.probe.ProbeResult.ProbeStatus;

/**
 * Schedules workspace servers probes checks asynchronously.
 *
 * @author Alexander Garagatyi
 */
@Singleton
public class ProbeScheduler {
  private final ScheduledThreadPoolExecutor probesExecutor;
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
  public ProbeScheduler(@Named("che.workspace.probe_pool_size") int probeSchedulerPoolSize) {
    probesExecutor =
        new ScheduledThreadPoolExecutor(
            probeSchedulerPoolSize,
            new ThreadFactoryBuilder().setDaemon(true).setNameFormat("ServerProbes-%s").build());
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
   */
  public void schedule(WorkspaceProbes probes, Consumer<ProbeResult> probeResultConsumer) {
    probesFutures.putIfAbsent(probes.getWorkspaceId(), new ArrayList<>());
    probes
        .getProbes()
        .forEach(
            probeFactory -> schedule(probes.getWorkspaceId(), probeFactory, probeResultConsumer));
  }

  /**
   * Dismisses following and if possible current executions of probes of a workspace with a
   * specified ID.
   */
  public void cancel(String workspaceId) {
    List<ScheduledFuture> tasks = probesFutures.remove(workspaceId);
    if (tasks == null) {
      return;
    }

    tasks.forEach(task -> task.cancel(true));
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
            (OldKey, scheduledFutures) -> {
              if (scheduledFutures == null) {
                scheduledFutures = new ArrayList<>();
              }
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
      timeouts.schedule(
          new TimeoutProbeTask(probe), TimeUnit.SECONDS.toMillis(probeConfig.getTimeoutSeconds()));
      boolean success = probe.probe();
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
