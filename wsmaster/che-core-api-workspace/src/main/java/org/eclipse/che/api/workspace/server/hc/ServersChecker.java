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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.google.inject.assistedinject.Assisted;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import javax.inject.Inject;
import javax.ws.rs.core.UriBuilder;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.core.model.workspace.runtime.Server;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalInfrastructureException;
import org.eclipse.che.api.workspace.server.token.MachineTokenProvider;

/**
 * Checks readiness of servers of a machine.
 *
 * @author Alexander Garagatyi
 */
public class ServersChecker {
  // Is used to define servers which will be checked by this server checker class.
  // It is also a workaround to set correct paths for servers readiness checks.
  private static final Map<String, String> LIVENESS_CHECKS_PATHS =
      ImmutableMap.of(
          "wsagent/http", "/api/",
          "exec-agent/http", "/process",
          "terminal", "/");
  private final RuntimeIdentity runtimeIdentity;
  private final String machineName;
  private final Map<String, ? extends Server> servers;
  private final MachineTokenProvider machineTokenProvider;

  private Timer timer;
  private long resultTimeoutSeconds;
  private CompletableFuture result;

  /**
   * Creates instance of this class.
   *
   * @param machineName name of machine whose servers will be checked by this method
   * @param servers map of servers in a machine
   */
  @Inject
  public ServersChecker(
      @Assisted RuntimeIdentity runtimeIdentity,
      @Assisted String machineName,
      @Assisted Map<String, ? extends Server> servers,
      MachineTokenProvider machineTokenProvider) {
    this.runtimeIdentity = runtimeIdentity;
    this.machineName = machineName;
    this.servers = servers;
    this.timer = new Timer("ServersChecker", true);
    this.machineTokenProvider = machineTokenProvider;
  }

  /**
   * Asynchronously starts checking readiness of servers of a machine. Method {@link #await()} waits
   * the result of this asynchronous check.
   *
   * @param serverReadinessHandler consumer which will be called with server reference as the
   *     argument when server become available
   * @throws InternalInfrastructureException if check of a server failed due to an unexpected error
   * @throws InfrastructureException if check of a server failed due to an error
   */
  public void startAsync(Consumer<String> serverReadinessHandler) throws InfrastructureException {
    timer = new Timer("ServersChecker", true);
    List<ServerChecker> serverCheckers = getServerCheckers();
    // should be completed with an exception if a server considered unavailable
    CompletableFuture<Void> firstNonAvailable = new CompletableFuture<>();
    CompletableFuture[] checkTasks =
        serverCheckers
            .stream()
            .map(ServerChecker::getReportCompFuture)
            .map(
                compFut ->
                    compFut
                        .thenAccept(serverReadinessHandler)
                        .exceptionally(
                            e -> {
                              // cleanup checkers tasks
                              timer.cancel();
                              firstNonAvailable.completeExceptionally(e);
                              return null;
                            }))
            .toArray(CompletableFuture[]::new);
    resultTimeoutSeconds = checkTasks.length * 180;
    // should complete when all servers checks reported availability
    CompletableFuture<Void> allAvailable = CompletableFuture.allOf(checkTasks);
    // should complete when all servers are available or any server is unavailable
    result = CompletableFuture.anyOf(allAvailable, firstNonAvailable);
    for (ServerChecker serverChecker : serverCheckers) {
      serverChecker.start();
    }
  }

  /**
   * Synchronously checks whether servers are available, throws {@link InfrastructureException} if
   * any is not.
   */
  public void checkOnce(Consumer<String> readyHandler) throws InfrastructureException {
    for (ServerChecker checker : getServerCheckers()) {
      checker.checkOnce(readyHandler);
    }
  }

  /**
   * Waits until servers are considered available or one of them is considered as unavailable.
   *
   * @throws InternalInfrastructureException if check of a server failed due to an unexpected error
   * @throws InfrastructureException if check of a server failed due to interruption
   * @throws InfrastructureException if check of a server failed because it reached timeout
   * @throws InfrastructureException if check of a server failed due to an error
   */
  public void await() throws InfrastructureException, InterruptedException {
    try {
      // TODO how much time should we check?
      result.get(resultTimeoutSeconds, TimeUnit.SECONDS);
    } catch (TimeoutException e) {
      throw new InfrastructureException(
          "Servers readiness check of machine " + machineName + " timed out");
    } catch (ExecutionException e) {
      try {
        throw e.getCause();
      } catch (InfrastructureException rethrow) {
        throw rethrow;
      } catch (Throwable thr) {
        throw new InternalInfrastructureException(
            "Machine "
                + machineName
                + " servers readiness check failed. Error: "
                + thr.getMessage(),
            thr);
      }
    }
  }

  private List<ServerChecker> getServerCheckers() throws InfrastructureException {
    ArrayList<ServerChecker> checkers = new ArrayList<>(servers.size());
    for (Map.Entry<String, ? extends Server> serverEntry : servers.entrySet()) {
      // TODO replace with correct behaviour
      // workaround needed because we don't have server readiness check in the model
      if (LIVENESS_CHECKS_PATHS.containsKey(serverEntry.getKey())) {
        checkers.add(getChecker(serverEntry.getKey(), serverEntry.getValue()));
      }
    }
    return checkers;
  }

  private ServerChecker getChecker(String serverRef, Server server) throws InfrastructureException {
    // TODO replace with correct behaviour
    // workaround needed because we don't have server readiness check in the model
    String livenessCheckPath = LIVENESS_CHECKS_PATHS.get(serverRef);
    // Create server readiness endpoint URL
    URL url;
    try {
      // TODO: ws -> http is workaround used for terminal websocket server,
      // should be removed after server checks added to model
      url =
          UriBuilder.fromUri(server.getUrl().replaceFirst("^ws", "http"))
              .replacePath(livenessCheckPath)
              .queryParam("token", machineTokenProvider.getToken(runtimeIdentity.getWorkspaceId()))
              .build()
              .toURL();
    } catch (MalformedURLException e) {
      throw new InternalInfrastructureException(
          "Server " + serverRef + " URL is invalid. Error: " + e.getMessage(), e);
    }

    return doCreateChecker(url, serverRef);
  }

  @VisibleForTesting
  ServerChecker doCreateChecker(URL url, String serverRef) {
    // TODO add readiness endpoint to terminal and remove this
    // workaround needed because terminal server doesn't have endpoint to check it readiness
    if ("terminal".equals(serverRef)) {
      return new TerminalHttpConnectionServerChecker(
          url, machineName, serverRef, 3, 180, TimeUnit.SECONDS, timer);
    }
    // TODO do not hardcode timeouts, use server conf instead
    return new HttpConnectionServerChecker(
        url, machineName, serverRef, 3, 180, TimeUnit.SECONDS, timer);
  }
}
