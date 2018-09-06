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
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOAD_PAGE_TIMEOUT_SEC;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BooleanSupplier;

/**
 * @author Mykola Morhun
 * @author Ihor Okhrimenko
 * @author Dmytro Nochevnov
 */
public class WaitUtils {

  public static final int DEFAULT_TIMEOUT_IN_SEC = LOAD_PAGE_TIMEOUT_SEC;
  public static final int DEFAULT_DELAY_BETWEEN_ATTEMPTS_IN_MILLISECONDS = 500;

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
  @SuppressWarnings("FutureReturnValueIgnored")
  public static void waitSuccessCondition(
      BooleanSupplier condition,
      long timeout,
      long delayBetweenAttemptsInMilliseconds,
      TimeUnit timeoutTimeUnit)
      throws InterruptedException, TimeoutException {
    ExecutorService executor = Executors.newSingleThreadExecutor();
    executor.submit(
        () -> {
          while (!condition.getAsBoolean()) {
            sleepQuietly(delayBetweenAttemptsInMilliseconds, TimeUnit.MILLISECONDS);
          }
        });

    executor.shutdown();
    if (!executor.awaitTermination(timeout, timeoutTimeUnit)) {
      throw new TimeoutException(
          String.format(
              "Expected condition failed: waiting for %s %s with %s MILLISECONDS interval",
              timeout, timeoutTimeUnit, delayBetweenAttemptsInMilliseconds));
    }
  }

  /**
   * Waits during {@code timeout} until {@code condition} has a "true" state.
   *
   * @param condition expression which should be performed
   * @param timeout waiting time
   */
  public static void waitSuccessCondition(
      BooleanSupplier condition, long timeout, TimeUnit timeUnit)
      throws InterruptedException, TimeoutException {
    waitSuccessCondition(
        condition, timeout, DEFAULT_DELAY_BETWEEN_ATTEMPTS_IN_MILLISECONDS, timeUnit);
  }

  /**
   * Waits during {@code timeoutInSec} until {@code condition} has a "true" state.
   *
   * @param condition expression which should be performed
   * @param timeoutInSec waiting time in seconds
   */
  public static void waitSuccessCondition(BooleanSupplier condition, long timeoutInSec)
      throws InterruptedException, TimeoutException {
    waitSuccessCondition(condition, timeoutInSec, TimeUnit.SECONDS);
  }

  /**
   * Waits until {@code condition} has a "true" state.
   *
   * @param condition expression which should be performed
   */
  public static void waitSuccessCondition(BooleanSupplier condition)
      throws InterruptedException, TimeoutException {
    waitSuccessCondition(condition, DEFAULT_TIMEOUT_IN_SEC);
  }
}
