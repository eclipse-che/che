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
package org.eclipse.che.api.workspace.server.bootstrap;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.eclipse.che.api.core.model.workspace.runtime.BootstrapperStatus;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.shared.dto.RuntimeIdentityDto;
import org.eclipse.che.api.workspace.shared.dto.event.BootstrapperStatusEvent;

/**
 * Bootstraps installers.
 *
 * @author Sergii Leshchenko
 */
public abstract class AbstractBootstrapper {

  private final String machineName;
  private final EventService eventService;
  private final EventSubscriber<BootstrapperStatusEvent> bootstrapperStatusListener;
  private final String installerEndpoint;
  private final String outputEndpoint;
  private CompletableFuture<BootstrapperStatusEvent> finishEventFuture;

  public AbstractBootstrapper(
      String machineName,
      RuntimeIdentity runtimeIdentity,
      String outputEndpoint,
      String installerEndpoint,
      EventService eventService) {
    this.machineName = machineName;
    this.eventService = eventService;
    this.installerEndpoint = outputEndpoint;
    this.outputEndpoint = installerEndpoint;
    this.bootstrapperStatusListener =
        event -> {
          BootstrapperStatus status = event.getStatus();
          // skip starting status event
          if (status.equals(BootstrapperStatus.DONE) || status.equals(BootstrapperStatus.FAILED)) {
            // check bootstrapper belongs to current runtime and machine
            RuntimeIdentityDto runtimeId = event.getRuntimeId();
            if (event.getMachineName().equals(machineName)
                && runtimeIdentity.getEnvName().equals(runtimeId.getEnvName())
                && runtimeIdentity.getWorkspaceId().equals(runtimeId.getWorkspaceId())) {

              finishEventFuture.complete(event);
            }
          }
        };
  }

  /**
   * Bootstraps installers and wait while they finished.
   *
   * @throws InfrastructureException when bootstrapping timeout reached
   * @throws InfrastructureException when bootstrapping failed
   * @throws InfrastructureException when any other error occurs while bootstrapping
   * @throws InterruptedException when the bootstrapping process was interrupted
   */
  public void bootstrap(int bootstrappingTimeoutMinutes)
      throws InfrastructureException, InterruptedException {
    if (finishEventFuture != null) {
      throw new IllegalStateException("Bootstrap method must be called only once.");
    }
    finishEventFuture = new CompletableFuture<>();

    eventService.subscribe(bootstrapperStatusListener, BootstrapperStatusEvent.class);
    try {
      doBootstrapAsync(installerEndpoint, outputEndpoint);

      // waiting for DONE or FAILED bootstrapper status event
      BootstrapperStatusEvent resultEvent =
          finishEventFuture.get(bootstrappingTimeoutMinutes, TimeUnit.MINUTES);
      if (resultEvent.getStatus().equals(BootstrapperStatus.FAILED)) {
        throw new InfrastructureException(resultEvent.getError());
      }
    } catch (ExecutionException e) {
      throw new InfrastructureException(e.getCause().getMessage(), e);
    } catch (TimeoutException e) {
      throw new InfrastructureException(
          "Bootstrapping of machine " + machineName + " reached timeout");
    } finally {
      eventService.unsubscribe(bootstrapperStatusListener, BootstrapperStatusEvent.class);
    }
  }

  /**
   * Asynchronously starts machine bootstrapping, subscribes to bootstrapper status events and
   * returns the future that contains the result of machine booting.
   *
   * <p>Note that the resulting future must be explicitly cancelled when its completion no longer
   * important because of finalization allocated resources.
   *
   * @return completable future that is completed when one of the following conditions is met:
   *     <ul>
   *       <li>bootstrapping status event is received
   *       <li>exception while performing async bootstrap occurred
   *     </ul>
   *     otherwise, it must be explicitly closed
   */
  public CompletableFuture<Void> bootstrapAsync() {
    if (finishEventFuture != null) {
      throw new IllegalStateException("Bootstrap method must be called only once.");
    }
    // This completable future is used to avoid checking the event state outside this of method
    final CompletableFuture<Void> bootstrapFuture = new CompletableFuture<>();

    finishEventFuture = new CompletableFuture<>();
    finishEventFuture.whenComplete(
        (ok, ex) -> {
          if (ex != null) {
            bootstrapFuture.completeExceptionally(ex);
          } else if (ok != null && BootstrapperStatus.FAILED.equals(ok.getStatus())) {
            bootstrapFuture.completeExceptionally(new InfrastructureException(ok.getError()));
          } else {
            bootstrapFuture.complete(null);
          }
          eventService.unsubscribe(bootstrapperStatusListener, BootstrapperStatusEvent.class);
        });
    bootstrapFuture.whenComplete((ok, ex) -> finishEventFuture.cancel(true));

    eventService.subscribe(bootstrapperStatusListener, BootstrapperStatusEvent.class);
    try {
      doBootstrapAsync(installerEndpoint, outputEndpoint);
    } catch (InfrastructureException ex) {
      finishEventFuture.completeExceptionally(
          new InfrastructureException(
              "Bootstrapping of machine " + machineName + " failed. Cause: " + ex.getMessage(),
              ex));
    }
    return bootstrapFuture;
  }

  /**
   * Launches bootstrapping.
   *
   * @param installerWebsocketEndpoint endpoint for pushing installers and bootstrapper statuses
   * @param outputWebsocketEndpoint endpoint for pushing installer logs
   * @throws InfrastructureException when any other error occurs while bootstrapping launching
   */
  protected abstract void doBootstrapAsync(
      String installerWebsocketEndpoint, String outputWebsocketEndpoint)
      throws InfrastructureException;
}
