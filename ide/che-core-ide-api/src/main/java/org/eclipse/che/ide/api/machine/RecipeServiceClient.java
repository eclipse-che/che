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
package org.eclipse.che.ide.api.machine;

import org.eclipse.che.api.machine.shared.dto.recipe.NewOldRecipe;
import org.eclipse.che.api.machine.shared.dto.recipe.OldRecipeDescriptor;
import org.eclipse.che.api.machine.shared.dto.recipe.OldRecipeUpdate;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.commons.annotation.Nullable;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Client for Recipe API.
 *
 * @author Artem Zatsarynnyi
 * @author Valeriy Svydenko
 */
public interface RecipeServiceClient {

    /**
     * Create recipe.
     *
     * @param newRecipe
     *         describer of the recipe
     * @return a promise that resolves to the {@link OldRecipeDescriptor}, or rejects with an error
     */
    Promise<OldRecipeDescriptor> createRecipe(@NotNull final NewOldRecipe newRecipe);

    /**
     * Get recipe script by recipe's ID.
     *
     * @param id
     *         recipe's ID
     * @return a promise that will provide the recipe's script, or rejects with an error
     */
    Promise<String> getRecipeScript(@NotNull String id);

    /**
     * Get recipe by ID.
     *
     * @param id
     *         recipe's ID
     * @return a promise that resolves to the {@link OldRecipeDescriptor}, or rejects with an error
     */
    Promise<OldRecipeDescriptor> getRecipe(@NotNull String id);

    /**
     * Get all recipes.
     *
     * @return a promise that will provide a list of {@link OldRecipeDescriptor}s, or rejects with an error
     */
    Promise<List<OldRecipeDescriptor>> getAllRecipes();

    /**
     * Search for recipes which type is equal to the specified {@code type}
     * and tags contain all of the specified {@code tags}.
     *
     * @param tags
     *         recipe tags
     * @param type
     *         recipe type
     * @param skipCount
     *         count of items which should be skipped
     * @param maxItems
     *         max count of items to fetch
     * @return a promise that will provide a list of {@link OldRecipeDescriptor}s, or rejects with an error
     */
    Promise<List<OldRecipeDescriptor>> searchRecipes(@NotNull List<String> tags, @Nullable String type, int skipCount, int maxItems);

    /**
     * Update recipe.
     *
     * @param recipeUpdate
     *         describer of the recipe updater
     * @return a promise that resolves to the {@link OldRecipeDescriptor}, or rejects with an error
     */
    Promise<OldRecipeDescriptor> updateRecipe(@NotNull OldRecipeUpdate recipeUpdate);

    /**
     * Remove recipe with the given ID.
     *
     * @param id
     *         recipe's ID
     * @return a promise that will resolve when the recipe has been removed, or rejects with an error
     */
    Promise<Void> removeRecipe(@NotNull String id);
}
