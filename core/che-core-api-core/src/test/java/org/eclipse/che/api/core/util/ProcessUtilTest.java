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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.testng.Assert;
import org.testng.annotations.Test;

/** @author andrew00x */
public class ProcessUtilTest {

  @Test
  public void testKill() throws Exception {
    final Process p = Runtime.getRuntime().exec(new String[] {"ping", "google.com"});
    final List<String> stdout = new ArrayList<>();
    final List<String> stderr = new ArrayList<>();
    final IOException[] processError = new IOException[1];
    final CountDownLatch latch = new CountDownLatch(1);
    final long start = System.currentTimeMillis();
    new Thread() {
      public void run() {
        try {
          ProcessUtil.process(
              p,
              new LineConsumer() {
                @Override
                public void writeLine(String line) throws IOException {
                  stdout.add(line);
                }

                @Override
                public void close() throws IOException {}
              },
              new LineConsumer() {
                @Override
                public void writeLine(String line) throws IOException {
                  stderr.add(line);
                }

                @Override
                public void close() throws IOException {}
              });
        } catch (IOException e) {
          processError[0] = e; // throw when kill process
        } finally {
          latch.countDown();
        }
      }
    }.start();

    Thread.sleep(1000); // give time to start process
    Assert.assertTrue(ProcessUtil.isAlive(p), "Process is not started.");

    ProcessUtil.kill(p); // kill process

    latch.await(15, TimeUnit.SECONDS); // should not stop here if process killed
    final long end = System.currentTimeMillis();

    Thread.sleep(200);

    // System process sleeps 10 seconds. It is safety to check we done in less then 3 sec.
    Assert.assertFalse(ProcessUtil.isAlive(p));
    Assert.assertTrue((end - start) < 3000, "Fail kill process");

    System.out.println(processError[0]);
    // processError[0].printStackTrace();
    System.out.println(stdout);
    System.out.println(stderr);
  }
}
