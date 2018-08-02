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
package org.eclipse.che.api.workspace.server.spi.environment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.Warning;
import org.eclipse.che.api.core.model.workspace.config.Environment;
import org.eclipse.che.api.workspace.server.spi.RuntimeInfrastructure;

/**
 * Representation of {@link Environment} which holds internal representations of environment
 * components to ease implementation of {@link RuntimeInfrastructure}.
 *
 * <p>It is related but not really bound to some specific infrastructure. It lets an infrastructure
 * apply multiple different implementations, some of which can be considered as a "native format",
 * while others as rather "supported, adopted formats".
 *
 * @author Alexander Garagatyi
 * @author gazarenkov
 */
public abstract class InternalEnvironment {
  private InternalRecipe recipe;
  private Map<String, InternalMachineConfig> machines;
  private List<Warning> warnings;

  protected InternalEnvironment() {}

  protected InternalEnvironment(
      InternalRecipe recipe, Map<String, InternalMachineConfig> machines, List<Warning> warnings) {
    this.recipe = recipe;
    this.machines = machines;
    this.warnings = warnings;
  }

  /** Returns environment recipe which includes recipe content. */
  public InternalRecipe getRecipe() {
    return recipe;
  }

  public InternalEnvironment setRecipe(InternalRecipe recipe) {
    this.recipe = recipe;
    return this;
  }

  /**
   * Returns unmodifiable map of internal machines configs which include all information about
   * machine configuration which may be needed by infrastructure implementation.
   */
  public Map<String, InternalMachineConfig> getMachines() {
    if (machines == null) {
      machines = new HashMap<>();
    }
    return machines;
  }

  public InternalEnvironment setMachines(Map<String, InternalMachineConfig> machines) {
    this.machines = machines;
    return this;
  }

  /**
   * Returns the list of the warnings indicating that the environment violates some non-critical
   * constraints or some preferable configuration is missing so defaults are used.
   */
  public List<Warning> getWarnings() {
    if (warnings == null) {
      warnings = new ArrayList<>();
    }
    return warnings;
  }

  /** Adds an {@link Warning}. */
  public void addWarning(Warning warning) {
    warnings.add(warning);
  }

  public InternalEnvironment setWarnings(List<Warning> warnings) {
    this.warnings = warnings;
    return this;
  }
}
