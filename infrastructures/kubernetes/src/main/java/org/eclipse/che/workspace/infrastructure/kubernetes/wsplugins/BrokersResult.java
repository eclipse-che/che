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

import com.google.common.annotations.Beta;
import com.google.common.annotations.VisibleForTesting;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.wsplugins.model.ChePlugin;

/** @author Alexander Garagatyi */
@Beta
public class BrokersResult {

  private final CompletableFuture<List<ChePlugin>> future;
  private final AtomicInteger brokersNumber;
  private final AtomicBoolean started;
  private final List<ChePlugin> plugins;

  public BrokersResult() {
    future = new CompletableFuture<>();
    brokersNumber = new AtomicInteger();
    started = new AtomicBoolean();
    plugins = Collections.synchronizedList(new ArrayList<>());
  }

  /**
   * Notifies {@code BrokerResult} that one more broker will be launched and we need to wait
   * response from it.
   *
   * <p>It should be called before the call of {@link #get(long, TimeUnit)}, otherwise {@link
   * IllegalStateException} would be thrown
   *
   * @throws IllegalStateException if called after call of {@link #get(long, TimeUnit)}
   */
  public void oneMoreBroker() {
    if (started.get()) {
      throw new IllegalStateException(
          "Call of BrokerResult#oneMoreBroker is not allowed after call BrokerResult#get");
    }
    brokersNumber.incrementAndGet();
  }

  /**
   * Submits exception indicating an error if the brokering process.
   *
   * <p>Completes call of {@link #get(long, TimeUnit)} with {@link ExecutionException} containing
   * provided exception
   *
   * @param e exception indicating brokering error
   * @throws IllegalStateException if called before the call of {@link #get(long, TimeUnit)}
   */
  public void error(Exception e) {
    if (!started.get()) {
      throw new IllegalStateException(
          "Submitting a broker error is not allowed before calling BrokerResult#get");
    }
    future.completeExceptionally(e);
  }

  /**
   * Submits a result of a broker execution.
   *
   * <p>It also count down the number of brokers that are waited for the result submission.
   *
   * @param toolingFromBroker tooling evaluated by a broker that needs to be added into a workspace
   * @throws InfrastructureException if called more times than {@link #oneMoreBroker()} which
   *     indicates incorrect usage of the {@link BrokersResult}
   * @throws IllegalStateException if called before the call of {@link #get(long, TimeUnit)}
   */
  public void brokerResult(List<ChePlugin> toolingFromBroker) throws InfrastructureException {
    if (!started.get()) {
      throw new IllegalStateException(
          "Submitting a broker result is not allowed before calling BrokerResult#get");
    }
    int previousBrokersNumber = brokersNumber.getAndDecrement();
    if (previousBrokersNumber == 0) {
      throw new InfrastructureException(
          "Broker result is submitted when no more results are expected");
    }
    plugins.addAll(toolingFromBroker);
    if (previousBrokersNumber == 1) {
      future.complete(new ArrayList<>(plugins));
    }
  }

  /**
   * Waits for the tooling that needs to be injected into a workspace being submitted by calls of
   * {@link #brokerResult(List)}.
   *
   * <p>Number of calls of {@link #brokerResult(List)} needs to be the same as number of calls of
   * {@link #oneMoreBroker()}. Returned list is a combination of lists submitted to {@link
   * #brokerResult(List)}. If provided timeout elapses before all needed calls of {@link
   * #brokerResult(List)} method ends with an exception. This method is based on {@link
   * CompletableFuture#get(long, TimeUnit)} so it also inherits parameters and thrown exception.
   *
   * @return tooling submitted by one or several brokers that needs to be injected into a workspace
   * @throws IllegalStateException if called more than one time
   * @see CompletableFuture#get(long, TimeUnit)
   */
  public List<ChePlugin> get(long waitTime, TimeUnit tu)
      throws ExecutionException, InterruptedException, TimeoutException {
    if (started.compareAndSet(false, true)) {
      return future.get(waitTime, tu);
    } else {
      throw new IllegalStateException("BrokerResult#get doesn't support multiple calls");
    }
  }

  @VisibleForTesting
  boolean isStarted() {
    return started.get();
  }
}
