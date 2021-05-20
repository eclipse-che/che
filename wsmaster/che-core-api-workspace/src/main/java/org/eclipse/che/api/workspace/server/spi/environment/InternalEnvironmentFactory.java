/*
 * Copyright (c) 2012-2020 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.workspace.server.spi.environment;

import static org.eclipse.che.api.workspace.shared.Constants.CONTAINER_SOURCE_ATTRIBUTE;
import static org.eclipse.che.api.workspace.shared.Constants.RECIPE_CONTAINER_SOURCE;

import com.google.common.annotations.VisibleForTesting;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.core.model.workspace.Warning;
import org.eclipse.che.api.core.model.workspace.config.Environment;
import org.eclipse.che.api.core.model.workspace.config.MachineConfig;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.api.workspace.server.model.impl.ServerConfigImpl;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.commons.annotation.Nullable;

/**
 * Creates a valid instance of InternalEnvironment.
 *
 * <p>Expected to be bound with a MapBinder with unique String key that contains recipe type, like:
 *
 * <pre>
 *   MapBinder<String, InternalEnvironmentFactory> envFactories =
 *       MapBinder.newMapBinder(binder(), String.class, InternalEnvironmentFactory.class);
 *   envFactories.addBinding("recipe_type_1").to(SubclassOfInternalEnvironmentFactory.class);
 * </pre>
 *
 * @author gazarenkov
 * @author Sergii Leshchenko
 */
public abstract class InternalEnvironmentFactory<T extends InternalEnvironment> {

  private final RecipeRetriever recipeRetriever;
  private final MachineConfigsValidator machinesValidator;

  public InternalEnvironmentFactory(
      RecipeRetriever recipeRetriever, MachineConfigsValidator machinesValidator) {
    this.recipeRetriever = recipeRetriever;
    this.machinesValidator = machinesValidator;
  }

  /**
   * Creates a valid instance of InternalEnvironment.
   *
   * <p>To construct a valid instance it performs the following actions:
   *
   * <ul>
   *   <li>download recipe content if it is needed;
   *   <li>retrieve the configured installers from installers registry;
   *   <li>normalize servers port by adding default protocol in port if it is absent;
   *   <li>validate the environment machines;
   *   <li>invoke implementation specific method that should validate and parse recipe;
   *   <li>ensure there are environment variables pointing to machine names;
   * </ul>
   *
   * @param sourceEnv the environment
   * @return InternalEnvironment a valid InternalEnvironment instance
   * @throws InfrastructureException if exception occurs on recipe downloading
   * @throws InfrastructureException if infrastructure specific error occurs
   * @throws ValidationException if validation fails
   */
  public T create(@Nullable final Environment sourceEnv)
      throws InfrastructureException, ValidationException {

    Map<String, InternalMachineConfig> machines = new HashMap<>();
    List<Warning> warnings = new ArrayList<>();
    InternalRecipe recipe = null;

    if (sourceEnv != null) {
      recipe = recipeRetriever.getRecipe(sourceEnv.getRecipe());

      for (Map.Entry<String, ? extends MachineConfig> machineEntry :
          sourceEnv.getMachines().entrySet()) {
        MachineConfig machineConfig = machineEntry.getValue();
        machines.put(
            machineEntry.getKey(),
            new InternalMachineConfig(
                normalizeServers(machineConfig.getServers()),
                machineConfig.getEnv(),
                machineConfig.getAttributes(),
                machineConfig.getVolumes()));
      }

      machinesValidator.validate(machines);
    }

    T internalEnv = doCreate(recipe, machines, warnings);

    internalEnv
        .getMachines()
        .values()
        .forEach(m -> m.getAttributes().put(CONTAINER_SOURCE_ATTRIBUTE, RECIPE_CONTAINER_SOURCE));

    return internalEnv;
  }

  /**
   * Implementation validates downloaded recipe and creates specific InternalEnvironment.
   *
   * <p>Returned InternalEnvironment must contains all machine that are defined in recipe and in
   * source machines collection. Also, if memory limitation is supported, it may add memory limit
   * attribute {@link MachineConfig#MEMORY_LIMIT_ATTRIBUTE} from recipe or configured system-wide
   * default value.
   *
   * @param recipe downloaded recipe
   * @param machines machines configuration
   * @param warnings list of warnings
   * @return InternalEnvironment
   * @throws InfrastructureException if infrastructure specific error occurs
   * @throws ValidationException if validation fails
   */
  protected abstract T doCreate(
      @Nullable InternalRecipe recipe,
      Map<String, InternalMachineConfig> machines,
      List<Warning> warnings)
      throws InfrastructureException, ValidationException;

  @VisibleForTesting
  Map<String, ServerConfig> normalizeServers(Map<String, ? extends ServerConfig> servers) {
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
    return new ServerConfigImpl(
        port, serverConfig.getProtocol(), serverConfig.getPath(), serverConfig.getAttributes());
  }
}
