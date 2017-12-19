/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.workspace.server.spi;

import static java.util.stream.Collectors.toMap;
import static org.eclipse.che.api.core.model.workspace.config.ServerConfig.INTERNAL_SERVER_ATTRIBUTE;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import org.eclipse.che.api.core.model.workspace.Runtime;
import org.eclipse.che.api.core.model.workspace.Warning;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.core.model.workspace.runtime.Machine;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.core.model.workspace.runtime.Server;
import org.eclipse.che.api.workspace.server.URLRewriter;
import org.eclipse.che.api.workspace.server.model.impl.MachineImpl;
import org.eclipse.che.api.workspace.server.model.impl.ServerImpl;
import org.eclipse.che.api.workspace.server.model.impl.WarningImpl;

/**
 * Implementation of concrete Runtime
 *
 * @author gazarenkov
 */
public abstract class InternalRuntime<T extends RuntimeContext> implements Runtime {

  private final T context;
  private final URLRewriter urlRewriter;
  private final List<Warning> warnings;
  private WorkspaceStatus status;

  public InternalRuntime(
      T context, URLRewriter urlRewriter, List<Warning> warnings, boolean running) {
    this.context = context;
    this.urlRewriter = urlRewriter;
    this.warnings = new CopyOnWriteArrayList<>();
    if (warnings != null) {
      this.warnings.addAll(warnings);
    }
    if (running) {
      status = WorkspaceStatus.RUNNING;
    }
  }

  @Override
  public String getActiveEnv() {
    return context.getIdentity().getEnvName();
  }

  @Override
  public String getOwner() {
    return context.getIdentity().getOwner();
  }

  @Override
  public List<? extends Warning> getWarnings() {
    return warnings;
  }

  @Override
  public Map<String, ? extends Machine> getMachines() {
    return getInternalMachines()
        .entrySet()
        .stream()
        .collect(
            toMap(
                Map.Entry::getKey,
                e ->
                    new MachineImpl(
                        e.getValue().getAttributes(),
                        rewriteExternalServers(e.getValue().getServers()),
                        e.getValue().getStatus())));
  }

  /**
   * Returns map of machine name to machine instance entries.
   *
   * <p>Implementation should not return null
   */
  protected abstract Map<String, ? extends Machine> getInternalMachines();

  /**
   * Starts Runtime. In practice this method launching supposed to take unpredictable long time so
   * normally it should be launched in separated thread
   *
   * @param startOptions optional parameters
   * @throws StateException when the context is already used
   * @throws InternalInfrastructureException when error that indicates system internal problem
   *     occurs
   * @throws RuntimeStartInterruptedException when start execution is cancelled
   * @throws InfrastructureException when any other error occurs
   */
  public void start(Map<String, String> startOptions) throws InfrastructureException {
    if (this.status != null) {
      throw new StateException("Runtime already started");
    }
    status = WorkspaceStatus.STARTING;
    internalStart(startOptions);
    status = WorkspaceStatus.RUNNING;
  }

  /**
   * Starts underlying environment in implementation specific way.
   *
   * @param startOptions options of workspace that may be used in environment start
   * @throws InternalInfrastructureException when error that indicates system internal problem
   *     occurs
   * @throws RuntimeStartInterruptedException when start execution is cancelled
   * @throws InfrastructureException when any other error occurs
   */
  protected abstract void internalStart(Map<String, String> startOptions)
      throws InfrastructureException;

  /**
   * Stops Runtime. Presumably can take some time so considered to launch in separate thread.
   *
   * <p>Runtime will be stopped only if its state {@link WorkspaceStatus#RUNNING} or {@link
   * WorkspaceStatus#STARTING}.
   *
   * @param stopOptions options of workspace that may used in environment stop
   * @throws StateException when the context can't be stopped because otherwise it would be in
   *     inconsistent status (e.g. stop(interrupt) might not be allowed during start)
   * @throws InfrastructureException when any other error occurs
   */
  public final void stop(Map<String, String> stopOptions) throws InfrastructureException {
    if (status != WorkspaceStatus.RUNNING && status != WorkspaceStatus.STARTING) {
      throw new StateException("The environment must be running or starting");
    }
    status = WorkspaceStatus.STOPPING;

    try {
      internalStop(stopOptions);
    } finally {
      status = WorkspaceStatus.STOPPED;
    }
  }

  /**
   * Stops Runtime in an implementation specific way.
   *
   * <ul>
   *   <li>When runtime state is {@link WorkspaceStatus#STARTING} then process of start must be
   *       cancelled and all the resources must be released including the cases when an exception
   *       occurs.
   *   <li>When runtime state is {@link WorkspaceStatus#RUNNING} then runtime must be normally
   *       stopped and all the resources must be released including the cases when an exception
   *       occurs.
   * </ul>
   *
   * @param stopOptions workspace options that may be used on runtime stop
   * @throws InfrastructureException when any other error occurs
   */
  protected abstract void internalStop(Map<String, String> stopOptions)
      throws InfrastructureException;

  /** @return some implementation specific properties if any */
  public abstract Map<String, String> getProperties();

  /** @return the Context */
  public final T getContext() {
    return context;
  }

  /**
   * Convenient method to rewrite incoming external servers in a loop
   *
   * @param incoming servers
   * @return rewritten Map of Servers (name -> Server)
   */
  private Map<String, Server> rewriteExternalServers(Map<String, ? extends Server> incoming) {
    Map<String, Server> outgoing = new HashMap<>();
    RuntimeIdentity identity = context.getIdentity();
    for (Map.Entry<String, ? extends Server> entry : incoming.entrySet()) {
      String name = entry.getKey();
      Server incomingServer = entry.getValue();
      if (Boolean.parseBoolean(incomingServer.getAttributes().get(INTERNAL_SERVER_ATTRIBUTE))) {
        outgoing.put(name, incomingServer);
      } else {
        try {
          ServerImpl server =
              new ServerImpl(incomingServer)
                  .withUrl(urlRewriter.rewriteURL(identity, name, incomingServer.getUrl()));
          outgoing.put(name, server);
        } catch (InfrastructureException e) {
          warnings.add(new WarningImpl(101, "Malformed URL for " + name + " : " + e.getMessage()));
        }
      }
    }

    return outgoing;
  }
}
