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
package org.eclipse.che.selenium.core.inject;

import static java.util.stream.IntStream.range;
import static org.testng.Assert.assertEquals;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/** @author Dmytro Nochevnov */
public class SeleniumTestStatisticsTest {
  private static final Logger LOG = LoggerFactory.getLogger(SeleniumTestStatisticsTest.class);

  @Test(dataProvider = "testData")
  @SuppressWarnings("FutureReturnValueIgnored")
  public void testSimultaneousUsage(
      int hitStart,
      int hitPass,
      int hitFail,
      int hitSkip,
      String initialToString,
      String finalToString)
      throws InterruptedException {
    // given
    SeleniumTestStatistics seleniumTestStatistics = new SeleniumTestStatistics();

    // when
    String actualString = seleniumTestStatistics.toString();

    // then
    assertEquals(actualString, initialToString);

    // when
    ExecutorService executor = Executors.newFixedThreadPool(10);

    range(0, hitStart).forEach(i -> executor.submit(seleniumTestStatistics::hitStart));
    range(0, hitPass).forEach(i -> executor.submit(seleniumTestStatistics::hitPass));
    range(0, hitFail).forEach(i -> executor.submit(seleniumTestStatistics::hitFail));
    range(0, hitSkip).forEach(i -> executor.submit(seleniumTestStatistics::hitSkip));

    executor.awaitTermination(20, TimeUnit.SECONDS);

    // then
    assertEquals(seleniumTestStatistics.toString(), finalToString);
    assertEquals(seleniumTestStatistics.hitStart(), hitStart + 1);
    assertEquals(seleniumTestStatistics.hitPass(), hitPass + 1);
    assertEquals(seleniumTestStatistics.hitFail(), hitFail + 1);
    assertEquals(seleniumTestStatistics.hitSkip(), hitSkip + 1);
  }

  @DataProvider
  public Object[][] testData() {
    return new Object[][] {
      {0, 0, 0, 0, "Passed: 0, failed: 0, skipped: 0.", "Passed: 0, failed: 0, skipped: 0."},
      {31, 21, 5, 4, "Passed: 0, failed: 0, skipped: 0.", "Passed: 21, failed: 5, skipped: 4."}
    };
  }
}
