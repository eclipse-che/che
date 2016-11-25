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

import org.eclipse.che.api.core.model.workspace.Environment;
import org.eclipse.che.api.core.model.workspace.EnvironmentRecipe;
import org.eclipse.che.api.core.model.workspace.ExtendedMachine;

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
    private EnvironmentRecipeImpl recipe;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "machines_id")
    @MapKeyColumn(name = "machines_key")
    private Map<String, ExtendedMachineImpl> machines;

    public EnvironmentImpl() {}

    public EnvironmentImpl(EnvironmentRecipe recipe,
                           Map<String, ? extends ExtendedMachine> machines) {
        if (recipe != null) {
            this.recipe = new EnvironmentRecipeImpl(recipe);
        }
        if (machines != null) {
            this.machines = machines.entrySet()
                                    .stream()
                                    .collect(Collectors.toMap(Map.Entry::getKey,
                                                              entry -> new ExtendedMachineImpl(entry.getValue())));
        }
    }

    public EnvironmentImpl(Environment environment) {
        if (environment.getRecipe() != null) {
            this.recipe = new EnvironmentRecipeImpl(environment.getRecipe());
        }
        if (environment.getMachines() != null) {
            this.machines = environment.getMachines()
                                       .entrySet()
                                       .stream()
                                       .collect(Collectors.toMap(Map.Entry::getKey,
                                                                 entry -> new ExtendedMachineImpl(entry.getValue())));
        }
    }

    public EnvironmentRecipeImpl getRecipe() {
        return recipe;
    }

    public void setRecipe(EnvironmentRecipeImpl environmentRecipe) {
        this.recipe = environmentRecipe;
    }

    @Override
    public Map<String, ExtendedMachineImpl> getMachines() {
        return machines;
    }

    public void setMachines(Map<String, ExtendedMachineImpl> machines) {
        this.machines = machines;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EnvironmentImpl)) return false;
        EnvironmentImpl that = (EnvironmentImpl)o;
        return Objects.equals(recipe, that.recipe) &&
               Objects.equals(machines, that.machines);
    }

    @Override
    public int hashCode() {
        return Objects.hash(recipe, machines);
    }

    @Override
    public String toString() {
        return "EnvironmentImpl{" +
               "recipe=" + recipe +
               ", machines=" + machines +
               '}';
    }
}
