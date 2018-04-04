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

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import org.eclipse.che.ide.runtime.Assert;

/**
 * Executor of a cancellable repeating command.
 *
 * <p>
 *
 * <p>Execution can be cancelled with {@link #cancel()} and rescheduled with consequent {@link
 * #schedule(int)} invocations.
 *
 * <p>
 *
 * <p>Generally method {@link #execute()} is invoked after (<i>{@link #tickDurationMs} * {@link
 * #tickCount}</i>) ms. If this method return {@code true} then it is called again after {@link
 * #tickDurationMs} ms.
 *
 * <p>
 *
 * <p>If method {@link #schedule(int)} is called before {@link #execute()} had finally returned
 * {@code false} then {@link #execute()} will be called only after (<i>{@link #tickDurationMs} *
 * {@link #tickCount}</i>) ms.
 *
 * <p>
 *
 * <p>At any time there is at most one scheduled {@link RepeatingCommand}. Thus duration to the next
 * {@link #execute()} invocation is not exact. Actually duration can be less than expected by, at
 * most, {@link #tickDurationMs}.
 */
public abstract class DeferredCommandExecutor {

  /**
   * A time granule size.
   *
   * <p>
   *
   * <p>Bigger values make scheduling less accurate. Lesser values lead to more frequent idle
   * cycles.
   */
  private final int tickDurationMs;

  /**
   * A number of the idle cycles before action is executed.
   *
   * <p>
   *
   * <p>{@code -1} means that action is not going to be executed.
   */
  private int tickCount;

  /** Indicator that shows if {@link #repeatingCommand} is still scheduled. */
  private boolean repeatingCommandAlive;

  /**
   * Synchronisation / anti-recursion safeguard.
   *
   * <p>
   *
   * <p>{@code true} when action is executed from timer callback.
   */
  private boolean isExecuting;

  /** Command that is regularly executed to check if it is time to run action. */
  private final RepeatingCommand repeatingCommand =
      new RepeatingCommand() {

        @Override
        public boolean execute() {
          isExecuting = true;
          try {
            repeatingCommandAlive = DeferredCommandExecutor.this.onTick();
          } finally {
            isExecuting = false;
          }
          if (!repeatingCommandAlive) {
            tickCount = -1;
          }
          return repeatingCommandAlive;
        }
      };

  private boolean onTick() {
    // If disarmed
    if (tickCount < 0) {
      return false;
    }

    // It is not time yet
    if (tickCount > 0) {
      tickCount--;
    }
    if (tickCount > 0) {
      return true;
    }

    return execute();
  }

  /**
   * Method that is invoked on schedule.
   *
   * <p>
   *
   * <p>After scheduled invocation, method will be re-invoked each {@link #tickDurationMs}ms until
   * it return {@code false}, or {@link #cancel()} / {@link #schedule(int)} is called.
   */
  protected abstract boolean execute();

  /** Schedule / reschedule {@link #execute()} invocation. */
  public void schedule(int tickCount) {
    Assert.isTrue(!isExecuting);
    Assert.isLegal(tickCount > 0);

    this.tickCount = tickCount;

    if (!repeatingCommandAlive) {
      repeatingCommandAlive = true;
      Scheduler.get().scheduleFixedDelay(repeatingCommand, tickDurationMs);
    }
  }

  /** Cancel scheduled {@link #execute()} invocation. */
  public void cancel() {
    Assert.isTrue(!isExecuting);
    tickCount = -1;
  }

  /** Check if executor is going to invoke {@link #execute()} again. */
  public boolean isScheduled() {
    Assert.isTrue(!isExecuting);
    return tickCount >= 0;
  }

  protected DeferredCommandExecutor(int tickDurationMs) {
    this.tickDurationMs = tickDurationMs;
  }
}
