/*
 * Copyright (c) 2012-2019 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.workspace.server.spi;

import java.net.URI;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.environment.InternalEnvironment;

/**
 * A Context for Workspace's Runtime
 *
 * @author gazarenkov
 */
public abstract class RuntimeContext<T extends InternalEnvironment> {

  private final T environment;
  private final RuntimeIdentity identity;
  private final RuntimeInfrastructure infrastructure;

  public RuntimeContext(
      T internalEnvironment, RuntimeIdentity identity, RuntimeInfrastructure infrastructure) {
    this.environment = internalEnvironment;
    this.identity = identity;
    this.infrastructure = infrastructure;
  }

  /**
   * Context must return the Runtime object whatever its status is (STOPPED status including)
   *
   * @return Runtime object
   * @throws InfrastructureException when any error during runtime retrieving/creation
   */
  public abstract InternalRuntime getRuntime() throws InfrastructureException;

  /**
   * Infrastructure should assign channel (usual WebSocket) to push long lived processes messages.
   * Examples of such messages include:
   *
   * <ul>
   *   <li>Start/Stop logs output
   *   <li>Installers output
   *   <li>etc
   * </ul>
   *
   * It is expected that ones returning this URI implementation guarantees supporting and not
   * changing it during the whole life time of Runtime. Repeating calls of this method should return
   * the same URI If infrastructure implementation provides a channel it guarantees:
   *
   * <ul>
   *   <li>this endpoint is open and ready to use
   *   <li>this endpoint emits only messages of specified formats (TODO specify the formats)
   *   <li>high loaded infrastructure provides scaling of "messaging server" to avoid overloading
   * </ul>
   *
   * @return URI of the channels endpoint
   * @throws UnsupportedOperationException if implementation does not provide channel
   * @throws InfrastructureException when any other error occurs
   */
  public abstract URI getOutputChannel()
      throws InfrastructureException, UnsupportedOperationException;

  /**
   * Runtime Identity contains information allowing uniquely identify a Runtime It is not necessary
   * that all of this information is used for identifying Runtime outside of SPI framework (in
   * practice workspace ID looks like enough)
   *
   * @return the RuntimeIdentityImpl
   */
  public RuntimeIdentity getIdentity() {
    return identity;
  }

  /** @return RuntimeInfrastructure the Context created from */
  public RuntimeInfrastructure getInfrastructure() {
    return infrastructure;
  }

  /** Returns {@link InternalEnvironment} runtime is based on */
  public T getEnvironment() {
    return environment;
  }
}
