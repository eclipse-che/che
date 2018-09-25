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

import com.google.common.annotations.Beta;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.Warning;
import org.eclipse.che.api.core.model.workspace.WorkspaceConfig;
import org.eclipse.che.api.core.model.workspace.config.Environment;
import org.eclipse.che.api.workspace.server.spi.RuntimeInfrastructure;
import org.eclipse.che.commons.annotation.Nullable;

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
  private Map<String, String> attributes;
  private String type;

  protected InternalEnvironment() {}

  protected InternalEnvironment(
      InternalRecipe recipe, Map<String, InternalMachineConfig> machines, List<Warning> warnings) {
    this.recipe = recipe;
    this.machines = machines;
    this.warnings = warnings;
    this.type = recipe != null ? recipe.getType() : null;
  }

  /**
   * Returns internal environment type - an identifier of the type of the environment.
   *
   * <p>It can differ from the type of {@link InternalRecipe#getType()} in certain cases. An example
   * of such a case is converting of an environment from one type to another for the purposes of an
   * infrastructure. In this case, {@link InternalRecipe#getType()} shows an origin type of the
   * environment whereas this method might return the type of the environment after the conversion.
   */
  @Nullable
  public String getType() {
    return type;
  }

  public InternalEnvironment setType(String type) {
    this.type = type;
    return this;
  }

  /** Returns environment recipe which includes recipe content. */
  @Nullable
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

  /**
   * Note that this field is in Beta and is subject to be removed at any point of time without any
   * notification.
   *
   * <p>Returns map of workspace config attributes that can be used for workspace runtime creation.
   *
   * @see WorkspaceConfig#getAttributes()
   */
  @Beta
  public Map<String, String> getAttributes() {
    if (attributes == null) {
      attributes = new HashMap<>();
    }
    return attributes;
  }

  /**
   * Note that this field is in Beta and is subject to be removed at any point of time without any
   * notification.
   *
   * @param attributes workspace config attributes that might be used in creation of workspace
   *     runtime
   * @see WorkspaceConfig#getAttributes()
   */
  @Beta
  public InternalEnvironment setAttributes(Map<String, String> attributes) {
    this.attributes = attributes;
    return this;
  }
}
