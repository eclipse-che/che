/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
