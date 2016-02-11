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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.toList;

//TODO move?

/**
 * Data object for {@link Environment}.
 *
 * @author Eugene Voevodin
 */
public class EnvironmentImpl implements Environment {

    private String                  name;
    private Recipe                  recipe;
    private List<MachineConfigImpl> machineConfigs;

    public EnvironmentImpl(String name, Recipe recipe, List<? extends MachineConfig> machineConfigs) {
        this.name = name;
        // TODO here should be the copy of the recipe
        this.recipe = recipe;
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

    @Override
    public Recipe getRecipe() {
        return recipe;
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
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof EnvironmentImpl)) return false;
        final EnvironmentImpl other = (EnvironmentImpl)obj;
        return Objects.equals(name, other.name) &&
               Objects.equals(recipe, other.recipe) &&
               getMachineConfigs().equals(other.getMachineConfigs());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = hash * 31 + Objects.hashCode(name);
        hash = hash * 31 + Objects.hashCode(recipe);
        hash = hash * 31 + getMachineConfigs().hashCode();
        return hash;
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
