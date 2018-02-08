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

import static java.lang.String.format;
import static org.eclipse.che.api.core.util.ProcessUtil.executeAndWait;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * @author andrew00x
 * @author Dmytro Nochevnov
 */
public class ProcessUtilTest {

  private static final String TEST_MESSAGE = "123";
  private static final String[] SIMPLE_COMMAND = new String[] {"echo", TEST_MESSAGE};

  private static final String UNKNOWN_COMMAND = "command-65asdfax3a532v1zc32v1";
  private static final String[] UNKNOWN_COMMAND_ARRAY = new String[] {UNKNOWN_COMMAND};

  /** {@link ProcessBuilder#start} method doesn't catch error of this command */
  private static final String[] BASH_UNKNOWN_COMMAND = new String[] {"bash", "-c", UNKNOWN_COMMAND};

  @Test
  public void testKill() throws Exception {
    final Process p = Runtime.getRuntime().exec(new String[] {"ping", "google.com"});
    final List<String> stdout = new ArrayList<>();
    final List<String> stderr = new ArrayList<>();
    final IOException[] processError = new IOException[1];
    final CountDownLatch latch = new CountDownLatch(1);
    final long start = System.currentTimeMillis();
    new Thread(
            () -> {
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
            })
        .start();

    Thread.sleep(1000); // give time to start process
    assertTrue(ProcessUtil.isAlive(p), "Process is not started.");

    ProcessUtil.kill(p); // kill process

    latch.await(15, TimeUnit.SECONDS); // should not stop here if process killed
    final long end = System.currentTimeMillis();

    Thread.sleep(200);

    // System process sleeps 10 seconds. It is safety to check we done in less then 3 sec.
    Assert.assertFalse(ProcessUtil.isAlive(p));
    assertTrue((end - start) < 3000, "Fail kill process");

    System.out.println(processError[0]);
    // processError[0].printStackTrace();
    System.out.println(stdout);
    System.out.println(stderr);
  }

  @Test(dataProvider = "dataForTestingExecuteAndWaitCommandHandleStderr")
  public void testExecuteAndWaitCommandHandleStderr(
      String[] commandLine,
      String expectedErrorMessageFragment,
      String expectedStdout,
      String expectedStderr)
      throws TimeoutException, InterruptedException {
    // when
    ListLineConsumer stdout = new ListLineConsumer();
    ListLineConsumer stderr = new ListLineConsumer();

    try {
      executeAndWait(commandLine, 5, TimeUnit.SECONDS, stdout, stderr);
    } catch (IOException e) {
      // then
      assertNotNull(expectedErrorMessageFragment, format("Unexpected error '%s'.", e));

      String errorMessagePattern = format(".*%s.*", expectedErrorMessageFragment);
      assertTrue(
          e.getMessage().matches(errorMessagePattern),
          format("Error message doesn't contain '%s'.", expectedErrorMessageFragment));

      return;
    }

    // then
    if (expectedErrorMessageFragment != null) {
      fail(
          format(
              "Command '%s' should cause IOException with error message which contains '%s'.",
              Arrays.toString(commandLine), expectedErrorMessageFragment));
    }

    assertEquals(stdout.getText(), expectedStdout);
    assertEquals(stderr.getText(), expectedStderr);
  }

  @DataProvider
  public Object[][] dataForTestingExecuteAndWaitCommandHandleStderr() {
    return new Object[][] {
      {SIMPLE_COMMAND, null, TEST_MESSAGE, ""},
      {UNKNOWN_COMMAND_ARRAY, UNKNOWN_COMMAND, "", ""},
      {BASH_UNKNOWN_COMMAND, UNKNOWN_COMMAND, "", UNKNOWN_COMMAND}
    };
  }
}
