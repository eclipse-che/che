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
package org.eclipse.che.plugin.maven.server.core;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Executor for {@link MavenProjectTask}. Uses {@link MavenExecutorService} as executor service.
 *
 * @author Evgen Vidolob
 */
public class MavenTaskExecutor {
  private static final Logger LOG = LoggerFactory.getLogger(MavenTaskExecutor.class);

  private final MavenExecutorService service;
  private final MavenProgressNotifier notifier;
  private final Queue<MavenProjectTask> queue = new LinkedList<>();
  private boolean isWorking;

  public MavenTaskExecutor(MavenExecutorService service, MavenProgressNotifier notifier) {
    this.service = service;
    this.notifier = notifier;
  }

  public void submitTask(MavenProjectTask task) {
    synchronized (queue) {
      // if no running tasks, start immediately
      if (!isWorking) {
        isWorking = true;
        runTask(task);
      } else {
        if (!queue.contains(task)) {
          queue.add(task);
        }
      }
    }
  }

  public void removeTask(MavenProjectTask task) {
    synchronized (queue) {
      queue.remove(task);
    }
  }

  public void stop() {
    synchronized (queue) {
      queue.clear();
    }
  }

  private void runTask(MavenProjectTask task) {
    service.submit(() -> doRunTasks(task));
  }

  private void doRunTasks(MavenProjectTask task) {
    int taskDone = 0;
    notifier.start();
    while (true) {
      taskDone++;
      int restTasks;
      synchronized (queue) {
        restTasks = queue.size();
      }

      notifier.setPercent((double) taskDone / (double) (restTasks + taskDone));
      try {
        task.perform();
      } catch (Throwable throwable) {
        LOG.error(throwable.getMessage(), throwable);
        // TODO need to notify user some how
      }

      synchronized (queue) {
        task = queue.poll();
        if (task == null) {
          isWorking = false;
          notifier.stop();
          return;
        }
      }
    }
  }

  public void waitForEndAllTasks() {
    if (!isWorking) {
      return;
    }

    Semaphore semaphore = new Semaphore(1);
    try {
      semaphore.acquire();
      submitTask(semaphore::release);

      while (true) {
        if (!isWorking || semaphore.tryAcquire(1, TimeUnit.SECONDS)) {
          return;
        }
      }
    } catch (InterruptedException e) {
      LOG.debug(e.getMessage(), e);
    }
  }
}
