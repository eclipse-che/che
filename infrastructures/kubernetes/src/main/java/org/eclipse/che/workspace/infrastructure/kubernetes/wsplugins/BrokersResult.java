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

/**
 * @author Alexander Garagatyi
 */
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

  public void oneMoreBroker() {
    if (started.get()) {
      throw new IllegalStateException("Call of BrokerResult#oneMoreBroker is not allowed after call BrokerResult#get");
    }
    brokersNumber.incrementAndGet();
  }

  public void error(Exception e) {
    future.completeExceptionally(e);
  }

  public void brokerResult(List<ChePlugin> toolingFromBroker) throws InfrastructureException {
    if (!started.get()) {
      throw new IllegalStateException("Submitting a broker result is not allowed before calling BrokerResult#get");
    }
    int previousBrokersNumber = brokersNumber.getAndDecrement();
    if (previousBrokersNumber == 0) {
      throw new InfrastructureException("Broker result is submitted when no more results are expected");
    }
    plugins.addAll(toolingFromBroker);
    if (previousBrokersNumber == 1) {
      future.complete(new ArrayList<>(plugins));
    }
  }

  public List<ChePlugin> get(long waitTime, TimeUnit tu)
      throws ExecutionException, InterruptedException, TimeoutException {
    if (started.compareAndSet(false, true)) {
      return future.get(waitTime, tu);
    } else {
      throw new IllegalStateException("BrokerResult#get doesn't support multiple calls");
    }
  }
}
