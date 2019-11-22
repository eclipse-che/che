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
   * @param identity the runtime identity
   * @param environment incoming internal environment
   * @return new RuntimeContext object
   * @throws ValidationException if incoming environment is not valid
   * @throws InfrastructureException if any other error occurred
   */
  public RuntimeContext prepare(RuntimeIdentity identity, InternalEnvironment environment)
      throws ValidationException, InfrastructureException {
    for (InternalEnvironmentProvisioner provisioner : internalEnvironmentProvisioners) {
      provisioner.provision(identity, environment);
    }
    return internalPrepare(identity, environment);
  }

  /**
   * Returns the namespace a workspace should be deployed into when user do not specify it.
   *
   * <p>May be used for evaluating a default namespace or for workspaces that does not have stored
   * infrastructure namespace info(legacy workspaces).
   *
   * @param resolutionCtx the runtime holder specifying which user and workspace runtime targets.
   * @throws InfrastructureException when there is no configured default namespace or on any other
   *     error
   */
  public abstract String evaluateInfraNamespace(NamespaceResolutionContext resolutionCtx)
      throws InfrastructureException;

  /**
   * Returns the namespace a workspace should be deployed into when using the legacy (pre 7.5.0)
   * logic. This is required to handle a case where we're starting a workspace created using an old
   * version of the Che server that didn't store the infrastructure namespace in the workspace
   * attributes.
   *
   * @param resolutionContext the runtime holder specifying which user and workspace runtime
   *     targets.
   * @throws InfrastructureException when there is no configured default namespace or any other
   *     error
   */
  public abstract String evaluateLegacyInfraNamespace(NamespaceResolutionContext resolutionContext)
      throws InfrastructureException;

  /**
   * An Infrastructure implementation should be able to prepare RuntimeContext. This method is not
   * supposed to be called by clients of class {@link RuntimeInfrastructure}.
   *
   * @param identity the runtime identity
   * @param environment incoming internal environment
   * @return new RuntimeContext object
   * @throws ValidationException if incoming environment is not valid
   * @throws InfrastructureException if any other error occurred
   */
  protected abstract RuntimeContext internalPrepare(
      RuntimeIdentity identity, InternalEnvironment environment)
      throws ValidationException, InfrastructureException;
}
