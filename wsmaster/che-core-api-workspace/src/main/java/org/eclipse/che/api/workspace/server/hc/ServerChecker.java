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
package org.eclipse.che.api.workspace.server.hc;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;

/**
 * Checks availability of a server.
 *
 * @author Alexander Garagatyi
 */
public abstract class ServerChecker {
  private final String machineName;
  private final String serverRef;
  private final long period;
  private final long deadLine;
  private final CompletableFuture<String> reportFuture;
  private final Timer timer;

  /**
   * Creates server checker instance.
   *
   * @param machineName name of machine to whom the server belongs
   * @param serverRef reference of the server
   * @param period period between unsuccessful availability checks, measured in {@code timeUnit}
   * @param timeout max time allowed for the server availability checks to last before server is
   *     treated unavailable, measured in {@code timeUnit}
   * @param timeUnit measurement unit for {@code period} and {@code timeout} parameters
   */
  protected ServerChecker(
      String machineName,
      String serverRef,
      long period,
      long timeout,
      TimeUnit timeUnit,
      Timer timer) {
    this.machineName = machineName;
    this.serverRef = serverRef;
    this.timer = timer;
    this.period = TimeUnit.MILLISECONDS.convert(period, timeUnit);
    this.reportFuture = new CompletableFuture<>();
    this.deadLine = System.currentTimeMillis() + TimeUnit.MILLISECONDS.convert(timeout, timeUnit);
  }

  /**
   * Starts server availability checking, which will be stopped when server become available or
   * checking times out.
   */
  public void start() {
    timer.schedule(new ServerCheckingTask(), 0);
  }

  /**
   * Checks server availability, throws {@link InfrastructureException} if the server is not
   * available.
   */
  public void checkOnce(Consumer<String> readinessHandler) throws InfrastructureException {
    if (!isAvailable()) {
      throw new InfrastructureException(
          String.format("Server '%s' in machine '%s' not available.", serverRef, machineName));
    }
    readinessHandler.accept(serverRef);
  }

  /**
   * Shows whether the server is treated as available.
   *
   * @return true if server is available, false otherwise
   */
  public abstract boolean isAvailable();

  /**
   * Returns {@code CompletableFuture} that will be completed when server become available or
   * unavailable. When server become available completable future returns server reference.
   * </br>This completable future can be used to chain {@code CompletableFuture} stage to successful
   * or unsuccessful completion of the server availability check.
   *
   * @see CompletableFuture
   * @see CompletableFuture#thenAccept(Consumer)
   * @see CompletableFuture#exceptionally(Function)
   */
  public CompletableFuture<String> getReportCompFuture() {
    return reportFuture;
  }

  private boolean isTimedOut() {
    return System.currentTimeMillis() > deadLine;
  }

  private class ServerCheckingTask extends TimerTask {
    @Override
    public void run() {
      if (isTimedOut()) {
        reportFuture.completeExceptionally(
            new InfrastructureException(
                String.format(
                    "Server '%s' in machine '%s' not available.", serverRef, machineName)));
      } else if (isAvailable()) {
        reportFuture.complete(serverRef);
      } else {
        timer.schedule(new ServerCheckingTask(), period);
      }
    }
  }
}
