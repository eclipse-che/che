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
package org.eclipse.che.ide.api.workspace.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.eclipse.che.api.core.model.workspace.config.Environment;
import org.eclipse.che.api.core.model.workspace.config.MachineConfig;
import org.eclipse.che.api.core.model.workspace.config.Recipe;

/** Data object for {@link Environment}. */
public class EnvironmentImpl implements Environment {

  private RecipeImpl recipe;
  private Map<String, MachineConfigImpl> machines;

  public EnvironmentImpl(Recipe recipe, Map<String, ? extends MachineConfig> machines) {
    if (recipe != null) {
      this.recipe = new RecipeImpl(recipe);
    }
    if (machines != null) {
      this.machines =
          machines
              .entrySet()
              .stream()
              .collect(
                  Collectors.toMap(
                      Map.Entry::getKey, entry -> new MachineConfigImpl(entry.getValue())));
    }
  }

  public EnvironmentImpl(Environment environment) {
    this(environment.getRecipe(), environment.getMachines());
  }

  public RecipeImpl getRecipe() {
    return recipe;
  }

  @Override
  public Map<String, MachineConfigImpl> getMachines() {
    if (machines == null) {
      machines = new HashMap<>();
    }
    return machines;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof EnvironmentImpl)) {
      return false;
    }
    final EnvironmentImpl that = (EnvironmentImpl) obj;
    return Objects.equals(recipe, that.recipe) && getMachines().equals(that.getMachines());
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 31 * hash + Objects.hashCode(recipe);
    hash = 31 * hash + getMachines().hashCode();
    return hash;
  }

  @Override
  public String toString() {
    return "EnvironmentImpl{" + "recipe=" + recipe + ", machines=" + machines + '}';
  }
}
