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
package org.eclipse.che.api.core.util;

import static org.eclipse.che.api.core.util.ProcessUtil.executeAndWait;
import static org.testng.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author andrew00x
 * @author Dmytro Nochevnov
 */
public class ProcessUtilTest {

  private static final String TEST_MESSAGE = "123";
  private static final String UNKNOWN_COMMAND = "command-65asdfax3a532v1zc32v1";
  private static final String PRINT_TO_STDERR_COMMAND = ">&2 echo " + TEST_MESSAGE;

  @Test
  public void testKill() throws Exception {
    final Process p = Runtime.getRuntime().exec(new String[] {"ping", "google.com"});
    final List<String> stdout = new ArrayList<>();
    final List<String> stderr = new ArrayList<>();
    final IOException[] processError = new IOException[1];
    final CountDownLatch latch = new CountDownLatch(1);
    final long start = System.currentTimeMillis();
    new Thread(() -> {
      try {
        ProcessUtil.readOutput(
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
    }).start();

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

  @Test
  public void testExecuteAndWaitSuccessfully()
      throws TimeoutException, InterruptedException, IOException {
    // given
    final ListLineConsumer stdout = new ListLineConsumer();
    final ListLineConsumer stderr = new ListLineConsumer();

    // when
    executeAndWait(new String[] {"echo", TEST_MESSAGE}, 5, TimeUnit.SECONDS, stdout, stderr);

    // then
    assertEquals(stdout.getText(), TEST_MESSAGE);
    assertEquals(stderr.getText(), "");
  }

  @Test(
    expectedExceptions = IOException.class,
    expectedExceptionsMessageRegExp = ".*" + UNKNOWN_COMMAND + ".*"
  )
  public void testExecuteAndWaitCommandError()
      throws InterruptedException, IOException, TimeoutException {
    // when
    executeAndWait(
        new String[] {UNKNOWN_COMMAND},
        5,
        TimeUnit.SECONDS,
        new ListLineConsumer(),
        new ListLineConsumer());
  }

  @Test(
    expectedExceptions = IOException.class,
    expectedExceptionsMessageRegExp = ".*" + TEST_MESSAGE + ".*"
  )
  public void testExecuteAndWaitCommandErrorInSTDERR()
      throws InterruptedException, IOException, TimeoutException {
    // when
    executeAndWait(
        new String[] {PRINT_TO_STDERR_COMMAND},
        5,
        TimeUnit.SECONDS,
        new ListLineConsumer(),
        new ListLineConsumer());
  }
}
