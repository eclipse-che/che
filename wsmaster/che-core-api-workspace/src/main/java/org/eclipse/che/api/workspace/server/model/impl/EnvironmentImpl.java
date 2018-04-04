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
package org.eclipse.che.api.workspace.server.model.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import org.eclipse.che.api.core.model.workspace.config.Environment;
import org.eclipse.che.api.core.model.workspace.config.MachineConfig;
import org.eclipse.che.api.core.model.workspace.config.Recipe;

/**
 * Data object for {@link Environment}.
 *
 * @author Yevhenii Voevodin
 */
@Entity(name = "Environment")
@Table(name = "environment")
public class EnvironmentImpl implements Environment {

  @Id
  @GeneratedValue
  @Column(name = "id")
  private Long id;

  @Embedded private RecipeImpl recipe;

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
  @JoinColumn(name = "machines_id")
  @MapKeyColumn(name = "machines_key")
  private Map<String, MachineConfigImpl> machines;

  public EnvironmentImpl() {}

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

  @Override
  public RecipeImpl getRecipe() {
    return recipe;
  }

  public void setRecipe(RecipeImpl environmentRecipe) {
    this.recipe = environmentRecipe;
  }

  @Override
  public Map<String, MachineConfigImpl> getMachines() {
    if (machines == null) {
      machines = new HashMap<>();
    }
    return machines;
  }

  public void setMachines(Map<String, MachineConfigImpl> machines) {
    this.machines = machines;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof EnvironmentImpl)) return false;
    EnvironmentImpl that = (EnvironmentImpl) o;
    return Objects.equals(id, that.id)
        && Objects.equals(getRecipe(), that.getRecipe())
        && Objects.equals(getMachines(), that.getMachines());
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, getRecipe(), getMachines());
  }

  @Override
  public String toString() {
    return "EnvironmentImpl{" + "id=" + id + ", recipe=" + recipe + ", machines=" + machines + '}';
  }
}
