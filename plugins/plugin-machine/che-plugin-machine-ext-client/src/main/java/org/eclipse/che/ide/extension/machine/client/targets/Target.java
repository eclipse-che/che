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

import javax.validation.constraints.NotNull;

/**
 * Wrapper for the machine.
 *
 * @author Vitaliy Guliy
 * @author Oleksii Orel
 */
public interface Target {

    /** Sets the display name of the target. */
    void setName(@NotNull String name);

    /** Returns the display name of the target. */
    String getName();

    /** Sets the category of the target. */
    void setCategory(@NotNull String category);

    /** Returns the category of the target. */
    String getCategory();

    /** Sets the recipe of the target. */
    void setRecipe(RecipeDescriptor recipe);

    /** Returns the recipe of the target. */
    RecipeDescriptor getRecipe();

    /** Returns the connecting status. */
    boolean isConnected();

    /** Sets the connecting status. */
    void setConnected(boolean connected);

    /** Returns status of unsaved changes. */
    boolean isDirty();

    /** Sets status of unsaved changes. */
    void setDirty(boolean dirty);
}
