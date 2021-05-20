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
package org.eclipse.che.commons.test;

import static org.testng.Assert.*;

import java.util.concurrent.atomic.AtomicBoolean;
import org.testng.annotations.Test;

public class AssertRetryTest {

  @Test
  public void testAssertWithRetry() throws InterruptedException {
    // Given
    AtomicBoolean atomicBoolean = new AtomicBoolean(false);
    // When
    new Thread(
            () -> {
              try {
                Thread.sleep(100);
                atomicBoolean.getAndSet(true);
              } catch (InterruptedException e) {
              }
            })
        .start();
    // then
    AssertRetry.assertWithRetry(() -> atomicBoolean.get(), Boolean.TRUE, 100, 10);
  }

  @Test(expectedExceptions = AssertionError.class)
  public void testAssertFailWithRetry() throws InterruptedException {
    // Given
    AtomicBoolean atomicBoolean = new AtomicBoolean(false);
    // When
    new Thread(
            () -> {
              try {
                Thread.sleep(100);
                atomicBoolean.getAndSet(true);
              } catch (InterruptedException e) {
              }
            })
        .start();
    // then
    AssertRetry.assertWithRetry(() -> atomicBoolean.get(), Boolean.TRUE, 2, 10);
  }
}
