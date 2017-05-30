/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.api.workspace.model;

import org.eclipse.che.api.core.model.workspace.Warning;
import org.eclipse.che.api.core.model.workspace.config.Environment;
import org.eclipse.che.api.core.model.workspace.config.MachineConfig;
import org.eclipse.che.api.core.model.workspace.config.Recipe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/** Data object for {@link Environment}. */
public class EnvironmentImpl implements Environment {

    private RecipeImpl                     recipe;
    private Map<String, MachineConfigImpl> machines;
    private List<WarningImpl>              warnings;

    public EnvironmentImpl(Recipe recipe,
                           Map<String, ? extends MachineConfig> machines,
                           List<? extends Warning> warnings) {
        if (recipe != null) {
            this.recipe = new RecipeImpl(recipe);
        }
        if (machines != null) {
            this.machines = machines.entrySet()
                                    .stream()
                                    .collect(Collectors.toMap(Map.Entry::getKey,
                                                              entry -> new MachineConfigImpl(entry.getValue())));
        }
        if (warnings != null) {
            this.warnings = warnings.stream()
                                    .map(WarningImpl::new)
                                    .collect(toList());
        }
    }

    public EnvironmentImpl(Environment environment) {
        this(environment.getRecipe(), environment.getMachines(), environment.getWarnings());
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
    public List<? extends WarningImpl> getWarnings() {
        if (warnings == null) {
            warnings = new ArrayList<>();
        }
        return warnings;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof EnvironmentImpl)) {
            return false;
        }
        final EnvironmentImpl that = (EnvironmentImpl)obj;
        return Objects.equals(recipe, that.recipe)
               && getMachines().equals(that.getMachines())
               && getWarnings().equals(that.getWarnings());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + Objects.hashCode(recipe);
        hash = 31 * hash + getMachines().hashCode();
        hash = 31 * hash + getWarnings().hashCode();
        return hash;
    }

    @Override
    public String toString() {
        return "EnvironmentImpl{" +
               "recipe=" + recipe +
               ", machines=" + machines +
               ", warnings=" + warnings +
               '}';
    }
}
