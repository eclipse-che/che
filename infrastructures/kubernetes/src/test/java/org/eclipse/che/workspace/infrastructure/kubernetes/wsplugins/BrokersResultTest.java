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
package org.eclipse.che.workspace.infrastructure.kubernetes.wsplugins;

import static java.util.Arrays.asList;
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
  public void shouldThrowExceptionOnCallingBrokerResultBeforeCallGet() throws Exception {
    brokersResult.brokerResult(emptyList());
  }

  @Test(
      expectedExceptions = InfrastructureException.class,
      expectedExceptionsMessageRegExp =
          "Broker result is submitted when no more results are expected")
  public void shouldThrowExceptionIfNumberOfBrokerResultCallsIsBiggerThanExpected()
      throws Exception {
    // given
    brokersResult.oneMoreBroker();
    brokersResult.oneMoreBroker();
    brokersResult.oneMoreBroker();
    executeBrokerGet();
    waitBrokerGetCalled();

    // when
    brokersResult.brokerResult(singletonList(new ChePlugin()));
    brokersResult.brokerResult(singletonList(new ChePlugin()));
    brokersResult.brokerResult(singletonList(new ChePlugin()));

    // then
    brokersResult.brokerResult(singletonList(new ChePlugin()));
  }

  @Test
  public void shouldReturnResultOfOneBroker() throws Exception {
    // given
    brokersResult.oneMoreBroker();
    ChePlugin chePlugin = new ChePlugin();
    executeWhenResultIsStarted(
        () -> {
          brokersResult.brokerResult(singletonList(chePlugin));
          return null;
        });

    // when
    List<ChePlugin> chePlugins = brokersResult.get(2, TimeUnit.SECONDS);

    // then
    assertEquals(chePlugins, singletonList(chePlugin));
  }

  @Test
  public void shouldCombineResultsOfSeveralBrokers() throws Exception {
    // given
    brokersResult.oneMoreBroker();
    ChePlugin chePlugin = new ChePlugin();
    brokersResult.oneMoreBroker();
    ChePlugin chePlugin2 = new ChePlugin();
    executeWhenResultIsStarted(
        () -> {
          brokersResult.brokerResult(singletonList(chePlugin));
          brokersResult.brokerResult(singletonList(chePlugin2));
          return null;
        });

    // when
    List<ChePlugin> chePlugins = brokersResult.get(2, TimeUnit.SECONDS);

    // then
    assertEquals(chePlugins, asList(chePlugin, chePlugin2));
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
    brokersResult.oneMoreBroker();

    brokersResult.get(1, TimeUnit.MILLISECONDS);
  }

  @Test(expectedExceptions = TimeoutException.class)
  public void shouldThrowTimeoutExceptionIfNotAllResultsAreSubmitted() throws Exception {
    // given
    brokersResult.oneMoreBroker();
    brokersResult.oneMoreBroker();
    executeWhenResultIsStarted(
        () -> {
          brokersResult.brokerResult(singletonList(new ChePlugin()));
          return null;
        });

    // when
    brokersResult.get(1, TimeUnit.SECONDS);
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
    brokersResult.oneMoreBroker();
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

  @Test
  public void shouldThrowExceptionIfErrorIsSubmittedAfterOneOfTheResults() throws Exception {
    executeWhenResultIsStarted(
        () -> {
          // when
          brokersResult.brokerResult(singletonList(new ChePlugin()));
          brokersResult.error(new InfrastructureException("test"));
          return null;
        });
    // given
    brokersResult.oneMoreBroker();
    brokersResult.oneMoreBroker();
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
