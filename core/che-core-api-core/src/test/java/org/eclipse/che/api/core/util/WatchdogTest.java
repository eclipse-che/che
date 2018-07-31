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
package org.eclipse.che.api.core.util;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.testng.Assert;
import org.testng.annotations.Test;

/** @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a> */
public class WatchdogTest {
  @Test
  public void testWatchDog() throws Exception {
    final CountDownLatch latch = new CountDownLatch(1);
    final boolean[] cancel = new boolean[] {false};
    final Cancellable myCancellable =
        new Cancellable() {
          @Override
          public void cancel() throws Exception {
            cancel[0] = true;
            latch.countDown();
          }
        };

    final Watchdog watchdog =
        new Watchdog(1, TimeUnit.SECONDS); // wait 1 sec then cancel myCancellable
    watchdog.start(myCancellable);
    latch.await(2, TimeUnit.SECONDS); // wait 2 sec
    Assert.assertTrue(cancel[0], "cancellation failed"); // should be cancelled
  }
}
