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
package org.eclipse.che.api.workspace.server.hc;

import static java.lang.String.format;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.util.Timer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** @author Alexander Garagatyi */
public class ServerCheckerTest {
  private static final String MACHINE_NAME = "mach1";
  private static final String SERVER_REF = "ref1";
  private static final long PERIOD_MS = 10;
  private static final long CHECKER_TIMEOUT_MS = 5000;
  private static final long TEST_TIMEOUT_MS = CHECKER_TIMEOUT_MS + 5000;
  private static final int SUCCESS_THRESHOLD = 1;

  private Timer timer;
  private TestServerChecker checker;

  @BeforeMethod
  public void setUp() throws Exception {
    timer = new Timer(true);
  }

  @AfterMethod
  public void tearDown() throws Exception {
    timer.cancel();
  }

  @Test(timeOut = TEST_TIMEOUT_MS)
  public void successfulCheckTest() throws Exception {
    checker =
        new TestServerChecker(
            MACHINE_NAME,
            SERVER_REF,
            PERIOD_MS,
            CHECKER_TIMEOUT_MS,
            SUCCESS_THRESHOLD,
            TimeUnit.MILLISECONDS,
            timer);
    CompletableFuture<String> reportCompFuture = checker.getReportCompFuture();
    // not considered as available before start
    assertFalse(reportCompFuture.isDone());
    // ensure server not available before start
    CountDownLatch isAvailableCountDownLatch = checker.setAvailable(false);

    checker.start();

    isAvailableCountDownLatch.await(PERIOD_MS * 2, TimeUnit.MILLISECONDS);
    // not considered as available after check
    assertFalse(reportCompFuture.isDone());

    // make server available
    isAvailableCountDownLatch = checker.setAvailable(true);

    assertEquals(reportCompFuture.get(), SERVER_REF);
    isAvailableCountDownLatch.await(PERIOD_MS * 2, TimeUnit.MILLISECONDS);
  }

  @Test(timeOut = TEST_TIMEOUT_MS)
  public void checkTimeoutTest() throws Exception {
    checker =
        new TestServerChecker(
            MACHINE_NAME,
            SERVER_REF,
            PERIOD_MS,
            PERIOD_MS * 2,
            SUCCESS_THRESHOLD,
            TimeUnit.MILLISECONDS,
            timer);

    // ensure server not available before start
    checker.setAvailable(false);
    checker.start();

    CompletableFuture<String> reportCompFuture = checker.getReportCompFuture();
    try {
      reportCompFuture.get();
      fail();
    } catch (ExecutionException e) {
      assertTrue(e.getCause() instanceof InfrastructureException);
      assertEquals(
          e.getCause().getMessage(),
          format("Server '%s' in container '%s' not available.", SERVER_REF, MACHINE_NAME));
    }
  }

  @Test(expectedExceptions = InfrastructureException.class)
  public void checkOnceThrowsExceptionIfServerIsNotAvailable() throws InfrastructureException {
    new TestServerChecker("test", "test", 1, 1, 1, TimeUnit.SECONDS, null).checkOnce(ref -> {});
  }

  private static class TestServerChecker extends ServerChecker {

    private boolean isAvailable;

    private CountDownLatch isAvailableCountDownLatch = new CountDownLatch(1);

    protected TestServerChecker(
        String machineName,
        String serverRef,
        long period,
        long timeout,
        int successThreshold,
        TimeUnit timeUnit,
        Timer timer) {
      super(machineName, serverRef, period, timeout, successThreshold, timeUnit, timer);
    }

    @Override
    public boolean isAvailable() {
      isAvailableCountDownLatch.countDown();
      return isAvailable;
    }

    public CountDownLatch setAvailable(boolean isAvailable) {
      this.isAvailable = isAvailable;
      this.isAvailableCountDownLatch = new CountDownLatch(1);
      return isAvailableCountDownLatch;
    }
  }
}
