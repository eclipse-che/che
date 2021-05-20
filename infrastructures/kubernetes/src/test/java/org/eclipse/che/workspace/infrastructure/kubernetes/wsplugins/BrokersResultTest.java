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
package org.eclipse.che.workspace.infrastructure.kubernetes.wsplugins;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.wsplugins.model.ChePlugin;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** @author Alexander Garagatyi */
public class BrokersResultTest {

  private ExecutorService executor;
  private BrokersResult brokersResult;

  @BeforeMethod
  public void setUp() {
    brokersResult = new BrokersResult();
    executor = Executors.newSingleThreadExecutor();
  }

  @AfterMethod
  public void tearDown() {
    executor.shutdown();
    executor.shutdownNow();
  }

  @Test(
      expectedExceptions = IllegalStateException.class,
      expectedExceptionsMessageRegExp =
          "Submitting a broker error is not allowed before calling BrokerResult#get")
  public void shouldThrowExceptionOnCallingErrorBeforeCallGet() {
    brokersResult.error(new Exception());
  }

  @Test(
      expectedExceptions = IllegalStateException.class,
      expectedExceptionsMessageRegExp =
          "Submitting a broker result is not allowed before calling BrokerResult#get")
  public void shouldThrowExceptionOnCallingAddResultBeforeCallGet() throws Exception {
    brokersResult.setResult(emptyList());
  }

  @Test(
      expectedExceptions = InfrastructureException.class,
      expectedExceptionsMessageRegExp =
          "Plugins brokering result is unexpectedly submitted more than one time. This indicates unexpected behavior of the system")
  public void shouldThrowExceptionIfResultIsSubmittedSecondTime() throws Exception {
    // given
    executeBrokerGet();
    waitBrokerGetCalled();

    // when
    brokersResult.setResult(singletonList(new ChePlugin()));

    // then
    brokersResult.setResult(singletonList(new ChePlugin()));
  }

  @Test
  public void shouldReturnResultOfBroker() throws Exception {
    // given
    ChePlugin chePlugin = new ChePlugin();
    executeWhenResultIsStarted(
        () -> {
          brokersResult.setResult(singletonList(chePlugin));
          return null;
        });

    // when
    List<ChePlugin> chePlugins = brokersResult.get(2, TimeUnit.SECONDS);

    // then
    assertEquals(chePlugins, singletonList(chePlugin));
  }

  @Test(
      expectedExceptions = IllegalStateException.class,
      expectedExceptionsMessageRegExp = "BrokerResult#get doesn't support multiple calls")
  public void shouldThrowExceptionIfGetCalledTwice() throws Exception {
    executeBrokerGet();

    waitBrokerGetCalled();
    brokersResult.get(100, TimeUnit.MILLISECONDS);
  }

  @Test(expectedExceptions = TimeoutException.class)
  public void shouldThrowTimeoutExceptionIfResultIsNotSubmitted() throws Exception {
    brokersResult.get(1, TimeUnit.MILLISECONDS);
  }

  @Test
  public void shouldThrowExceptionIfErrorIsSubmitted() throws Exception {
    executeWhenResultIsStarted(
        () -> {
          // when
          brokersResult.error(new InfrastructureException("test"));
          return null;
        });
    // given
    try {
      brokersResult.get(3, TimeUnit.SECONDS);
    } catch (ExecutionException e) {
      // then
      assertEquals(e.getCause().getClass(), InfrastructureException.class);
      assertEquals(e.getCause().getMessage(), "test");
      return;
    }
    fail();
  }

  private void executeBrokerGet() {
    executor.submit(() -> brokersResult.get(2, TimeUnit.SECONDS));
  }

  private void waitBrokerGetCalled() throws InterruptedException {
    while (!brokersResult.isStarted()) {
      Thread.sleep(100);
    }
  }

  private void executeWhenResultIsStarted(Callable<Void> c) {
    executor.submit(
        () -> {
          while (!brokersResult.isStarted()) {
            Thread.sleep(100);
          }
          c.call();
          return null;
        });
  }
}
