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
package org.eclipse;

import java.util.concurrent.CountDownLatch;

public class ThreadDumpTest {

  public static void main(String[] args) throws Exception {
    CountDownLatch latch = new CountDownLatch(1);

    Thread thread = new SomeThread(latch);
    thread.setDaemon(true);
    thread.start();

    latch.await();

    System.out.println("Hello, world!");

    for (; ; ) {}
  }

  public static class SomeThread extends Thread {
    private final CountDownLatch latch;

    public SomeThread(CountDownLatch latch) {
      super("SomeThread");
      this.latch = latch;
    }

    @Override
    public void run() {
      latch.countDown();

      for (; ; ) {}
    }
  }
}
