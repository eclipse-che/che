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

import static org.eclipse.che.selenium.core.utils.WaitUtils.DEFAULT_DELAY_BETWEEN_ATTEMPTS_IN_MILLISECONDS;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BooleanSupplier;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** @author Dmytro Nochevnov */
public class WaitSuccessConditionTest {
  private static final int NUMBER_OF_SUCCESS_CONDITION_CALL = 3;
  private static final long CONDITION_DELAY_MILLISECS = 300;
  private int countToZeroToSuccess;

  /**
   * Returns true at {@code NUMBER_OF_SUCCESS_CONDITION_CALL}. Operation lasts {@code
   * CONDITION_DELAY_MILLISECS}.
   */
  private BooleanSupplier testCondition =
      () -> {
        try {
          Thread.sleep(CONDITION_DELAY_MILLISECS);
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }

        return --countToZeroToSuccess == 0;
      };

  @BeforeMethod
  public void setupTestCondition() {
    countToZeroToSuccess = NUMBER_OF_SUCCESS_CONDITION_CALL;
  }

  @Test
  public void shouldSuccessWithTimeoutInMillisecs() throws Exception {
    WaitUtils.waitSuccessCondition(
        testCondition,
        NUMBER_OF_SUCCESS_CONDITION_CALL * CONDITION_DELAY_MILLISECS * 4,
        CONDITION_DELAY_MILLISECS / 2,
        TimeUnit.MILLISECONDS);
  }

  @Test(
      expectedExceptions = TimeoutException.class,
      expectedExceptionsMessageRegExp =
          "Expected condition failed: waiting for 600 MILLISECONDS with 150 MILLISECONDS interval")
  public void shouldFailBecauseTooSmallTimeout() throws Exception {
    WaitUtils.waitSuccessCondition(
        testCondition,
        (NUMBER_OF_SUCCESS_CONDITION_CALL - 1) * CONDITION_DELAY_MILLISECS,
        CONDITION_DELAY_MILLISECS / 2,
        TimeUnit.MILLISECONDS);
  }

  @Test(
      expectedExceptions = TimeoutException.class,
      expectedExceptionsMessageRegExp =
          "Expected condition failed: waiting for 1800 MILLISECONDS with 2700 MILLISECONDS interval")
  public void shouldFailBecauseTooLongDelayBetweenAttempts()
      throws InterruptedException, TimeoutException, ExecutionException {
    WaitUtils.waitSuccessCondition(
        testCondition,
        NUMBER_OF_SUCCESS_CONDITION_CALL * CONDITION_DELAY_MILLISECS * 2,
        NUMBER_OF_SUCCESS_CONDITION_CALL * CONDITION_DELAY_MILLISECS * 3,
        TimeUnit.MILLISECONDS);
  }

  @Test(
      expectedExceptions = ExecutionException.class,
      expectedExceptionsMessageRegExp = "java.lang.RuntimeException: test")
  public void shouldFailBecauseConditionCheckException() throws Exception {
    WaitUtils.waitSuccessCondition(
        () -> {
          throw new RuntimeException("test");
        },
        CONDITION_DELAY_MILLISECS * 2,
        CONDITION_DELAY_MILLISECS,
        TimeUnit.MILLISECONDS);
  }

  @Test(
      expectedExceptions = TimeoutException.class,
      expectedExceptionsMessageRegExp =
          "Expected condition failed: waiting for 150 MILLISECONDS with 30 MILLISECONDS interval")
  public void shouldFailBecauseOperationTakesTooLongTime()
      throws InterruptedException, TimeoutException, ExecutionException {
    countToZeroToSuccess = 0;
    WaitUtils.waitSuccessCondition(
        testCondition,
        CONDITION_DELAY_MILLISECS / 2,
        CONDITION_DELAY_MILLISECS / 10,
        TimeUnit.MILLISECONDS);
  }

  @Test(
      expectedExceptions = TimeoutException.class,
      expectedExceptionsMessageRegExp =
          "Expected condition failed: waiting for 1 SECONDS with 500 MILLISECONDS interval")
  public void shouldFailWithDefaultDelay() throws Exception {
    WaitUtils.waitSuccessCondition(
        testCondition, DEFAULT_DELAY_BETWEEN_ATTEMPTS_IN_MILLISECONDS * 2 / 1000);
  }
}
