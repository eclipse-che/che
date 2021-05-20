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

import com.google.common.annotations.Beta;
import com.google.common.annotations.VisibleForTesting;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.wsplugins.model.ChePlugin;

/** @author Alexander Garagatyi */
@Beta
public class BrokersResult {

  private final CompletableFuture<List<ChePlugin>> future;
  private final AtomicBoolean started;

  public BrokersResult() {
    future = new CompletableFuture<>();
    started = new AtomicBoolean();
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
   * Submits the result of a broker execution.
   *
   * @param toolingFromBroker tooling evaluated by a broker that needs to be added into a workspace
   * @throws InfrastructureException if called second time which indicates incorrect usage of the
   *     {@link BrokersResult}
   * @throws IllegalStateException if called before the call of {@link #get(long, TimeUnit)}
   */
  public void setResult(List<ChePlugin> toolingFromBroker) throws InfrastructureException {
    if (!started.get()) {
      throw new IllegalStateException(
          "Submitting a broker result is not allowed before calling BrokerResult#get");
    }
    if (future.isDone()) {
      throw new InfrastructureException(
          "Plugins brokering result is unexpectedly submitted more than one time. This indicates unexpected behavior of the system");
    }
    future.complete(new ArrayList<>(toolingFromBroker));
  }

  /**
   * Waits for the tooling that needs to be injected into a workspace being submitted by a call of
   * {@link #setResult(List)}.
   *
   * <p>If provided timeout elapses before needed call of {@link #setResult(List)} method ends with
   * an exception. This method is based on {@link CompletableFuture#get(long, TimeUnit)} so it also
   * inherits parameters and thrown exception.
   *
   * @return tooling submitted by broker that needs to be injected into a workspace
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
