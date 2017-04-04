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
package org.eclipse.che.api.workspace.server.model.impl;

import org.eclipse.che.api.core.model.workspace.Warning;
import org.eclipse.che.api.core.model.workspace.config.Environment;
import org.eclipse.che.api.core.model.workspace.config.Recipe;
import org.eclipse.che.api.core.model.workspace.config.MachineConfig;

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
import javax.persistence.Transient;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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

    @Embedded
    private RecipeImpl recipe;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "machines_id")
    @MapKeyColumn(name = "machines_key")
    private Map<String, MachineConfigImpl> machines;

    @Transient
    private List<Warning> warnings;

    public EnvironmentImpl() {}

    public EnvironmentImpl(Recipe recipe,
                           Map<String, ? extends MachineConfig> machines) {
        if (recipe != null) {
            this.recipe = new RecipeImpl(recipe);
        }
        if (machines != null) {
            this.machines = machines.entrySet()
                                    .stream()
                                    .collect(Collectors.toMap(Map.Entry::getKey,
                                                              entry -> new MachineConfigImpl(entry.getValue())));
        }
    }

    public EnvironmentImpl(Environment environment) {
        if (environment.getRecipe() != null) {
            this.recipe = new RecipeImpl(environment.getRecipe());
        }
        if (environment.getMachines() != null) {
            this.machines = environment.getMachines()
                                       .entrySet()
                                       .stream()
                                       .collect(Collectors.toMap(Map.Entry::getKey,
                                                                 entry -> new MachineConfigImpl(entry.getValue())));
        }
    }

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

    @Override
    public List<? extends Warning> getWarnings() {
        if (warnings == null) {
            warnings = new ArrayList<>();
        }
        return warnings;
    }

    public void setMachines(Map<String, MachineConfigImpl> machines) {
        this.machines = machines;
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
        return Objects.equals(id, that.id)
               && Objects.equals(recipe, that.recipe)
               && getMachines().equals(that.getMachines());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + Objects.hashCode(id);
        hash = 31 * hash + Objects.hashCode(recipe);
        hash = 31 * hash + getMachines().hashCode();
        return hash;
    }

    @Override
    public String toString() {
        return "EnvironmentImpl{" +
               "id=" + id +
               ", recipe=" + recipe +
               ", machines=" + machines +
               '}';
    }
}
