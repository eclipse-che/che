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
package org.eclipse.che.selenium.core.utils;

import static java.lang.Thread.sleep;
import static java.util.Arrays.asList;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOAD_PAGE_TIMEOUT_SEC;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import org.openqa.selenium.TimeoutException;

/**
 * @author Mykola Morhun
 * @author Ihor Okhrimenko
 */
public class WaitUtils {

  /**
   * Waits given time. When thread catch interrupt signal, than it immediately ends.
   *
   * @param seconds time to wait in seconds
   */
  public static void sleepQuietly(int seconds) {
    sleepQuietly(seconds, TimeUnit.SECONDS);
  }

  /**
   * Waits given time. When thread catch interrupt signal, than it immediately ends.
   *
   * @param timeout time to wait
   * @param timeUnit time unit of the timeout parameter
   */
  public static void sleepQuietly(long timeout, TimeUnit timeUnit) {
    long millisecondToWait = timeUnit.toMillis(timeout);
    try {
      sleep(millisecondToWait);
    } catch (InterruptedException e) {
      // Taking into account, that tests newer interrupts each other,
      // we can say, that this interrupt signal is external
      // and it wants to immediately stop execution of this thread.
      // According to the above, method, which calls this method
      // has no mechanisms for handling interrupts (so, invoke of
      // interrupt() method has no effect).
      // Considering the above, we must stop this thread here immediately.
      throw new RuntimeException(e);
    }
  }

  /**
   * Waits during {@code timeout} until {@code condition} has a "true" state.
   *
   * @param condition expression which should be performed
   * @param timeout waiting time
   * @param delayBetweenAttemptsInMilliseconds delay between tries of {@code condition} execution in
   *     milliseconds
   */
  public static void waitSuccessCondition(
      BooleanSupplier condition,
      long timeout,
      long delayBetweenAttemptsInMilliseconds,
      TimeUnit timeoutTimeUnit)
      throws InterruptedException, ExecutionException {
    final long waitingTime = timeoutTimeUnit.toMillis(timeout);
    final long startingTime = System.currentTimeMillis();
    final long finishTime = startingTime + waitingTime;

    Callable<Boolean> waitTrueState =
        () -> {
          while (System.currentTimeMillis() <= finishTime) {
            if (condition.getAsBoolean()) {
              return true;
            }

            sleepQuietly(delayBetweenAttemptsInMilliseconds, TimeUnit.MILLISECONDS);
          }

          throw new TimeoutException(
              "The condition has not being in \"true\" state during timeout");
        };

    List<Future<Boolean>> result =
        Executors.newSingleThreadScheduledExecutor()
            .invokeAll(asList(waitTrueState), timeout, timeoutTimeUnit);

    if (result.get(0).isCancelled()) {
      throw new TimeoutException("The condition has not being in \"true\" state during timeout");
    }
  }

  /**
   * Waits during {@code timeout} until {@code condition} has a "true" state.
   *
   * @param condition expression which should be performed
   * @param timeout waiting time
   */
  public static void waitSuccessCondition(BooleanSupplier condition, int timeout, TimeUnit timeUnit)
      throws InterruptedException, ExecutionException {
    final int delayBetweenAttemptsInMilliseconds = 500;
    waitSuccessCondition(condition, timeout, delayBetweenAttemptsInMilliseconds, timeUnit);
  }

  /**
   * Waits during {@code timeout} until {@code condition} has a "true" state.
   *
   * @param condition expression which should be performed
   * @param timeout waiting time in seconds
   */
  public static void waitSuccessCondition(BooleanSupplier condition, int timeout)
      throws InterruptedException, ExecutionException {
    waitSuccessCondition(condition, timeout, TimeUnit.SECONDS);
  }

  /**
   * Waits until {@code condition} has a "true" state.
   *
   * @param condition expression which should be performed
   */
  public static void waitSuccessCondition(BooleanSupplier condition)
      throws InterruptedException, ExecutionException {
    final int defaultTimeout = LOAD_PAGE_TIMEOUT_SEC;
    waitSuccessCondition(condition, defaultTimeout);
  }
}
