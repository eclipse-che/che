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
package org.eclipse.che.commons.test;

import java.util.function.Supplier;
import org.testng.Assert;

public class AssertRetry {
  /**
   * Assert that is making several attempts with pauses to match expected value with the value
   * provided by Supplier.
   */
  public static <V> void assertWithRetry(
      Supplier<V> predicate, V expected, int times, int pause_millis) throws InterruptedException {
    for (int i = 0; i <= times; i++) {
      V actual = predicate.get();
      if (expected.equals(actual)) {
        return;
      } else if (i + 1 <= times) {
        Thread.sleep(pause_millis);
      }
    }
    Assert.fail("Not able to get expected value " + expected + " with " + times + " retries");
  }
}
