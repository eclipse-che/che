// Copyright 2012 Google Inc. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.eclipse.che.ide.util.executor;

import com.google.gwt.core.client.Duration;
import org.eclipse.che.ide.util.ListenerRegistrar;
import org.eclipse.che.ide.util.loging.Log;

/** A scheduler that can incrementally run a task. */
public class BasicIncrementalScheduler implements IncrementalScheduler {

  private final AsyncRunner runner =
      new AsyncRunner() {
        @Override
        public void run() {
          if (isPaused) {
            return;
          }

          try {
            double start = Duration.currentTimeMillis();
            boolean keepRunning = worker.run(currentWorkAmount);
            updateWorkAmount(Duration.currentTimeMillis() - start);
            if (keepRunning) {
              schedule();
            } else {
              clearWorker();
            }
          } catch (Throwable t) {
            Log.error(getClass(), "Could not run worker", t);
          }
        }
      };

  private Task worker;

  private boolean isPaused;

  private int currentWorkAmount;

  private final int targetExecutionMs;

  private int completedWorkAmount;

  private double totalTimeTaken;

  public ListenerRegistrar.Remover userActivityRemover;

  public BasicIncrementalScheduler(int targetExecutionMs, int workGuess) {
    this.targetExecutionMs = targetExecutionMs;
    currentWorkAmount = workGuess;
  }

  public BasicIncrementalScheduler(
      UserActivityManager userActivityManager, int targetExecutionMs, int workGuess) {
    this(targetExecutionMs, workGuess);

    userActivityRemover =
        userActivityManager
            .getUserActivityListenerRegistrar()
            .add(
                new UserActivityManager.UserActivityListener() {
                  @Override
                  public void onUserActive() {
                    pause();
                  }

                  @Override
                  public void onUserIdle() {
                    resume();
                  }
                });
  }

  @Override
  public void schedule(Task worker) {
    cancel();
    this.worker = worker;

    if (!isPaused) {
      runner.run();
    }
  }

  @Override
  public void cancel() {
    runner.cancel();
    worker = null;
  }

  @Override
  public void pause() {
    isPaused = true;
  }

  /** Schedules the worker to resume. This will run asychronously. */
  @Override
  public void resume() {
    isPaused = false;

    if (worker != null) {
      launch();
    }
  }

  @Override
  public boolean isPaused() {
    return isPaused;
  }

  @Override
  public void teardown() {
    cancel();

    if (userActivityRemover != null) {
      userActivityRemover.remove();
    }
  }

  /**
   * Update the currentWorkAmount based upon the workTime it took to run the last command so running
   * the worker will take ~targetExecutionMs.
   *
   * @param workTime ms the last run took
   */
  private void updateWorkAmount(double workTime) {
    if (workTime <= 0) {
      currentWorkAmount *= 2;
    } else {
      totalTimeTaken += workTime;
      completedWorkAmount += currentWorkAmount;
      currentWorkAmount = (int) Math.ceil(targetExecutionMs * completedWorkAmount / totalTimeTaken);
    }
  }

  private void clearWorker() {
    worker = null;
  }

  @Override
  public boolean isBusy() {
    return worker != null;
  }

  /** Queues the worker launch. */
  private void launch() {
    runner.schedule();
  }
}
