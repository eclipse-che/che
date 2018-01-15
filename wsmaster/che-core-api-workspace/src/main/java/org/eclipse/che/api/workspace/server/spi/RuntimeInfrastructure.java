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
package org.eclipse.che.api.workspace.server.spi;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.workspace.server.spi.environment.InternalEnvironment;
import org.eclipse.che.api.workspace.server.spi.provision.InternalEnvironmentProvisioner;

/**
 * Starting point of describing the contract which infrastructure provider should implement for
 * making infrastructure suitable for serving workspace runtimes.
 *
 * @author gazarenkov
 */
public abstract class RuntimeInfrastructure {
  private final Set<String> recipeTypes;
  private final String name;
  private final EventService eventService;
  private final Set<InternalEnvironmentProvisioner> internalEnvironmentProvisioners;

  public RuntimeInfrastructure(
      String name,
      Collection<String> types,
      EventService eventService,
      Set<InternalEnvironmentProvisioner> internalEnvProvisioners) {
    Preconditions.checkArgument(!types.isEmpty());
    this.name = Objects.requireNonNull(name);
    this.eventService = eventService;
    this.recipeTypes = ImmutableSet.copyOf(types);
    this.internalEnvironmentProvisioners = internalEnvProvisioners;
  }

  /** Returns the name of this runtime infrastructure. */
  public String getName() {
    return name;
  }

  /**
   * Returns the types of the recipes supported by this runtime infrastructure. The set is never
   * empty and contains at least one recipe type.
   */
  public Set<String> getRecipeTypes() {
    return recipeTypes;
  }

  /** @return EventService */
  public EventService getEventService() {
    return eventService;
  }

  /**
   * An Infrastructure MAY track Runtimes. In this case the method should be overridden.
   *
   * <p>One of the reason for infrastructure to support this is ability to recover infrastructure
   * after shutting down Master server. For this purpose an Infrastructure should also implement
   * getRuntime(id) method
   *
   * @return list of tracked Runtimes' Identities.
   * @throws UnsupportedOperationException if implementation does not support runtimes tracking
   * @throws InfrastructureException if any other error occurred
   */
  public Set<RuntimeIdentity> getIdentities() throws InfrastructureException {
    throw new UnsupportedOperationException("The implementation does not track runtimes");
  }

  /**
   * Starting the Runtime is a two phase process:
   *
   * <pre>
   * <ul>
   *   <li>On the first phase implementation MUST prepare RuntimeContext;</li>
   *   <li>On the second phase the Runtime that can be fetched from RuntimeContext
   *   should be started with InternalRuntime.start().</li>
   * </ul>
   * </pre>
   *
   * @param id the RuntimeIdentity
   * @param environment incoming internal environment
   * @return new RuntimeContext object
   * @throws ValidationException if incoming environment is not valid
   * @throws InfrastructureException if any other error occurred
   */
  public RuntimeContext prepare(RuntimeIdentity id, InternalEnvironment environment)
      throws ValidationException, InfrastructureException {
    for (InternalEnvironmentProvisioner provisioner : internalEnvironmentProvisioners) {
      provisioner.provision(id, environment);
    }
    return internalPrepare(id, environment);
  }

  /**
   * An Infrastructure implementation should be able to prepare RuntimeContext. This method is not
   * supposed to be called by clients of class {@link RuntimeInfrastructure}.
   *
   * @param id the RuntimeIdentity
   * @param environment incoming internal environment
   * @return new RuntimeContext object
   * @throws ValidationException if incoming environment is not valid
   * @throws InfrastructureException if any other error occurred
   */
  protected abstract RuntimeContext internalPrepare(
      RuntimeIdentity id, InternalEnvironment environment)
      throws ValidationException, InfrastructureException;
}
