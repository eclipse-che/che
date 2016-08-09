/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.workspace.server.model.impl;

import org.eclipse.che.api.core.model.machine.MachineConfig;
import org.eclipse.che.api.core.model.machine.Recipe;
import org.eclipse.che.api.core.model.workspace.Environment;
import org.eclipse.che.api.machine.server.model.impl.MachineConfigImpl;
import org.eclipse.che.api.machine.server.recipe.RecipeImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.toList;

/**
 * Data object for {@link Environment}.
 *
 * @author Yevhenii Voevodin
 */
public class EnvironmentImpl implements Environment {

    private String                  name;
    private RecipeImpl              recipe;
    private List<MachineConfigImpl> machineConfigs;

    public EnvironmentImpl(String name, Recipe recipe, List<? extends MachineConfig> machineConfigs) {
        this.name = name;
        if (recipe != null) {
            this.recipe = new RecipeImpl(recipe);
        }
        if (machineConfigs != null) {
            this.machineConfigs = machineConfigs.stream()
                                                .map(MachineConfigImpl::new)
                                                .collect(toList());
        }
    }

    public EnvironmentImpl(Environment environment) {
        this(environment.getName(), environment.getRecipe(), environment.getMachineConfigs());
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Recipe getRecipe() {
        return recipe;
    }

    public void setRecipe(RecipeImpl recipe) {
        this.recipe = recipe;
    }

    @Override
    public List<MachineConfigImpl> getMachineConfigs() {
        if (machineConfigs == null) {
            machineConfigs = new ArrayList<>();
        }
        return machineConfigs;
    }

    public void setMachineConfigs(List<MachineConfigImpl> machineConfigs) {
        this.machineConfigs = machineConfigs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EnvironmentImpl)) return false;
        EnvironmentImpl that = (EnvironmentImpl)o;
        return Objects.equals(name, that.name) &&
               Objects.equals(recipe, that.recipe) &&
               Objects.equals(machineConfigs, that.machineConfigs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, recipe, machineConfigs);
    }

    @Override
    public String toString() {
        return "EnvironmentImpl{" +
               "name='" + name + '\'' +
               ", recipe=" + recipe +
               ", machineConfigs=" + machineConfigs +
               '}';
    }
}
