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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.Warning;
import org.eclipse.che.api.core.model.workspace.config.Environment;
import org.eclipse.che.api.core.model.workspace.config.MachineConfig;
import org.eclipse.che.api.core.model.workspace.config.Recipe;
import org.eclipse.che.api.installer.server.InstallerRegistry;
import org.eclipse.che.api.workspace.server.RecipeRetriever;

/**
 * Representation of {@link Environment} which holds internal representations of environment
 * components to ease implementation of {@link RuntimeInfrastructure}.
 *
 * @author Alexander Garagatyi
 * @author gazarenkov
 */
public class InternalEnvironment {
  private final InternalRecipe recipe;
  private final Map<String, InternalMachineConfig> machines;
  private final List<Warning> warnings;

  private final InstallerRegistry registry;

  InternalEnvironment(
      Environment environment, InstallerRegistry registry, RecipeRetriever recipeRetriever)
      throws InfrastructureException {
    this.registry = registry;
    this.machines = new HashMap<>();
    this.warnings = new ArrayList<>();
    this.recipe = getInternalRecipe(environment, recipeRetriever);

    for (Map.Entry<String, ? extends MachineConfig> entry : environment.getMachines().entrySet()) {
      machines.put(entry.getKey(), new InternalMachineConfig(entry.getValue(), registry));
    }
  }

  /** Returns environment recipe which includes recipe content. */
  public InternalRecipe getRecipe() {
    return recipe;
  }

  /**
   * Returns map of internal machines configs which include all information about machine
   * configuration which may be needed by infrastructure implementation.
   *
   * <p>Returned map is unmodifiable, addition of a machine is possible with method {@link
   * #addMachine(String, MachineConfig)}
   */
  public Map<String, InternalMachineConfig> getMachines() {
    return Collections.unmodifiableMap(machines);
  }

  /**
   * Adds an {@link InternalMachineConfig} based on provided arguments to machines list.
   *
   * @param name name of machine to add
   * @param machineConfig environment configuration of machine to add
   * @throws InfrastructureException in case any error occurs
   */
  public void addMachine(String name, MachineConfig machineConfig) throws InfrastructureException {
    machines.put(name, new InternalMachineConfig(machineConfig, registry));
  }

  /** Adds an {@link Warning}. */
  public void addWarning(Warning warning) {
    warnings.add(warning);
  }

  /**
   * Returns the list of the warnings indicating that the environment violates some non-critical
   * constraints or some preferable configuration is missing so defaults are used.
   */
  public List<? extends Warning> getWarnings() {
    return Collections.unmodifiableList(warnings);
  }

  private InternalRecipe getInternalRecipe(Environment environment, RecipeRetriever recipeRetriever)
      throws InfrastructureException {
    Recipe recipe = environment.getRecipe();
    String recipeContent = recipeRetriever.getRecipe(recipe);
    return new InternalRecipe(recipe.getType(), recipe.getContentType(), recipeContent);
  }

  /**
   * Recipe of {@link Environment} with content either provided by {@link Recipe#getContent()} or
   * downloaded from {@link Recipe#getLocation()}.
   */
  public static class InternalRecipe {
    private final String type;
    private final String contentType;
    private final String content;

    InternalRecipe(String type, String contentType, String content) {
      this.type = type;
      this.contentType = contentType;
      this.content = content;
    }

    public String getType() {
      return type;
    }

    public String getContentType() {
      return contentType;
    }

    public String getContent() {
      return content;
    }
  }
}
