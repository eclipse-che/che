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

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.core.model.workspace.config.Environment;
import org.eclipse.che.api.core.model.workspace.config.MachineConfig;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.installer.server.InstallerRegistry;
import org.eclipse.che.api.installer.server.exception.InstallerException;
import org.eclipse.che.api.installer.shared.model.Installer;
import org.eclipse.che.api.workspace.server.spi.provision.InternalEnvironmentProvisioner;
import org.eclipse.che.api.workspace.server.model.impl.ServerConfigImpl;

/**
 * Starting point of describing the contract which infrastructure provider should implement for
 * making infrastructure suitable for serving workspace runtimes.
 *
 * @author gazarenkov
 */
public abstract class RuntimeInfrastructure {

  private final Set<String> recipeTypes;
  private final String name;
  private final InstallerRegistry installerRegistry;
  private final RecipeRetriever recipeRetriever;
  private final EventService eventService;
  private final Set<InternalEnvironmentProvisioner> internalEnvironmentProvisioners;

  public RuntimeInfrastructure(
      String name,
      Collection<String> types,
      EventService eventService,
      InstallerRegistry installerRegistry,
      RecipeRetriever recipeRetriever,
      Set<InternalEnvironmentProvisioner> internalEnvironmentProvisioners) {
    Preconditions.checkArgument(!types.isEmpty());
    this.name = Objects.requireNonNull(name);
    this.recipeTypes = ImmutableSet.copyOf(types);
    this.eventService = eventService;
    this.installerRegistry = installerRegistry;
    this.recipeRetriever = recipeRetriever;
    this.internalEnvironmentProvisioners = internalEnvironmentProvisioners;
  }

  /** Returns the name of this runtime infrastructure. */
  public final String getName() {
    return name;
  }

  /**
   * Returns the types of the recipes supported by this runtime infrastructure. The set is never
   * empty and contains at least one recipe type.
   */
  public final Set<String> getRecipeTypes() {
    return recipeTypes;
  }

  /** @return EventService */
  public final EventService getEventService() {
    return eventService;
  }

  /**
   * Preliminary estimates incoming Environment. Should be used for environment validation before
   * storing and creation of {@link RuntimeContext}.
   *
   * <p>It is supposed that result is used as a parameter of the method {@link
   * #prepare(RuntimeIdentity, InternalEnvironment)}. <br>
   * Note: this method will be eventually final, but it is not for now for workaround in Docker
   * infra - in dockerimage environment image should be in content, not in location. It is marked
   * with {@link Beta} annotation to hint that. Do not override this method. <br>
   * Workaround should be removed after resolution of https://github.com/eclipse/che/issues/6006
   *
   * @param environment incoming Environment to estimate
   * @return calculated internal environment.
   * @throws ValidationException if incoming Environment is not valid
   * @throws InfrastructureException if any other error occurred
   */
  @Beta
  public InternalEnvironment estimate(Environment environment)
      throws ValidationException, InfrastructureException {

    InternalEnvironment internalEnvironment = resolveInternalEnvironment(environment);

    internalEstimate(internalEnvironment);

    return internalEnvironment;
  }

  /**
   * An Infrastructure implementation should be able to preliminary estimate incoming environment.
   * This method is not supposed to be called by clients of class {@link RuntimeInfrastructure}.
   *
   * <p>For example: for validating it before storing. The method SHOULD validate an incoming
   * internal environment. If it is valid, an Infrastructure MAY return more fine grained {@link
   * InternalEnvironment}. <br>
   * For example:
   * <li>- if Machines are not described in environment machines list this method may add machine
   *     descriptions calculated against Recipe
   * <li>- implementation may add additional Attributes based on incoming Recipe, e.g. default RAM
   *     amount if it is neither set in Recipe nor attributes
   * <li>- implementation may add warnings which identify some precautions which may be returned to
   *     the user
   *
   * @param env internal representation of environment
   * @throws ValidationException if incoming Environment is not valid
   * @throws InfrastructureException if any other error occurred
   */
  protected abstract void internalEstimate(InternalEnvironment env)
      throws ValidationException, InfrastructureException;

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
   * Making Runtime is a two phase process. On the first phase implementation MUST prepare
   * RuntimeContext, this is supposedly "fast" method On the second phase Runtime is created with
   * RuntimeContext.start() which is supposedly "long" method.
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

  /**
   * Resolves {@link InternalEnvironment} instance based on specified {@link Environment}.
   *
   * <p>Resolved internal environment will have:
   *
   * <ul>
   *   <li>Downloaded recipe;
   *   <li>Fetched information about configured {@link Installer installers};
   * </ul>
   *
   * @param environment environment to resolve
   * @return resolved internal environment
   * @throws InfrastructureException if any exception occurs on environment resolving
   * @see InternalEnvironment
   */
  private InternalEnvironment resolveInternalEnvironment(Environment environment)
      throws InfrastructureException {
    InternalRecipe internalRecipe = recipeRetriever.getRecipe(environment.getRecipe());

    Map<String, InternalMachineConfig> internalMachines = new HashMap<>();
    for (Map.Entry<String, ? extends MachineConfig> machineEntry :
        environment.getMachines().entrySet()) {
      MachineConfig machineConfig = machineEntry.getValue();
      List<Installer> installers = getInstallers(machineConfig.getInstallers());

      //TODO Move to provisioning
      Map<String, ServerConfig> servers = normalizeServers(machineConfig.getServers());

      internalMachines.put(
          machineEntry.getKey(),
          new InternalMachineConfig(
              installers,
              servers,
              machineConfig.getEnv(),
              machineConfig.getAttributes()));
    }
    return new InternalEnvironment(internalRecipe, internalMachines);
  }

  private List<Installer> getInstallers(List<String> installersKeys)
      throws InfrastructureException {
    try {
      return installerRegistry.getOrderedInstallers(installersKeys);
    } catch (InstallerException e) {
      throw new InfrastructureException(e.getMessage(), e);
    }
  }

  //TODO Take a look | Add tests
  private Map<String, ServerConfig> normalizeServers(Map<String, ? extends ServerConfig> servers) {
    return servers
        .entrySet()
        .stream()
        .collect(Collectors.toMap(Entry::getKey, e -> normalizeServer(e.getValue())));
  }

  private ServerConfig normalizeServer(ServerConfig serverConfig) {
    String port = serverConfig.getPort();
    if (port != null && !port.contains("/")) {
      port = port + "/tcp";
    }
    return new ServerConfigImpl(port, serverConfig.getProtocol(), serverConfig.getPath());
  }
}
