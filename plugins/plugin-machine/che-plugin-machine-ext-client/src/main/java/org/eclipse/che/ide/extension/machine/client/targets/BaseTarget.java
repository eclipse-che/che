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
package org.eclipse.che.ide.extension.machine.client.targets;

import org.eclipse.che.api.machine.shared.dto.recipe.RecipeDescriptor;

import java.util.Objects;

/**
 * Base implementation of {@link Target} that is intended
 * to be extended instead of directly implementing an interface.
 *
 * @author Oleksii Orel
 * */
public class BaseTarget implements Target {

    private String name;

    private String category;

    private RecipeDescriptor recipe;

    /**
     * Indicate if target has unsaved changes.
     */
    private boolean dirty;

    private boolean connected;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getCategory() {
        return category;
    }

    @Override
    public void setCategory(String category) {
        this.category = category;
    }

    @Override
    public RecipeDescriptor getRecipe() {
        return recipe;
    }

    @Override
    public void setRecipe(RecipeDescriptor recipe) {
        this.recipe = recipe;
    }

    @Override
    public boolean isConnected() {
        return connected;
    }

    @Override
    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    @Override
    public boolean isDirty() {
        return dirty;
    }

    @Override
    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof BaseTarget)) {
            return false;
        }

        BaseTarget other = (BaseTarget)o;

        return Objects.equals(getName(), other.getName())
               && Objects.equals(getCategory(), other.getCategory())
               && Objects.equals(getRecipe(), other.getRecipe());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getCategory(), getRecipe());
    }
}
