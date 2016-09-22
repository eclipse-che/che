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
package org.eclipse.che.api.machine.server.spi;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.machine.server.recipe.RecipeImpl;
import org.eclipse.che.api.machine.shared.ManagedRecipe;

import java.util.List;

/**
 * Data access object for {@link ManagedRecipe}.
 *
 * @author Eugene Voevodin
 */
public interface RecipeDao {

    /**
     * Creates recipe
     *
     * @param recipe
     *         recipe to create
     * @throws NullPointerException
     *         when {@code recipe} is not specified
     * @throws ConflictException
     *         when recipe with id equal to {@code recipe.getId()} already exists
     * @throws ServerException
     *         when any other error occurs
     */
    void create(RecipeImpl recipe) throws ConflictException, ServerException;

    /**
     * Updates existing recipe
     * <p/>
     * All data except of recipe identifier and recipe creator may be updated
     *
     * @param recipe
     *         recipe update
     * @return updated instance of the recipe
     * @throws NullPointerException
     *         when {@code recipe} is not specified
     * @throws NotFoundException
     *         when recipe with id equal to {@code recipe.getId()} doesn't exist
     * @throws ServerException
     *         when any other error occurs
     */
    RecipeImpl update(RecipeImpl recipe) throws NotFoundException, ServerException;

    /**
     * Removes existing recipe
     * <p/>
     * If recipe with specified {@code id} doesn't exist nothing will be done
     *
     * @param id
     *         recipe identifier to remove recipe
     * @throws NullPointerException
     *         when recipe {@code id} is not specified
     * @throws ServerException
     *         when any error occurs
     */
    void remove(String id) throws ServerException;

    /**
     * Returns recipe with specified {@code id} or throws {@link NotFoundException}
     * when recipe with such identifier doesn't exist
     *
     * @param id
     *         recipe identifier to search recipe
     * @return recipe with specified {@code id}
     * @throws NullPointerException
     *         when recipe {@code id} is not specified
     * @throws NotFoundException
     *         when recipe with specified {@code id} doesn't exist
     * @throws ServerException
     *         when any error occurs
     */
    RecipeImpl getById(String id) throws NotFoundException, ServerException;

    /**
     * Searches for recipes which have read permissions for specified user and type is equal to specified {@code type}
     * and tags contain all of specified {@code tags}.
     * <p/>
     * Not specified {@code tags} or {@code type} will not take a part of search,
     * i.e. when {@code type} is {@code null} then only recipes which tags
     * contain all of specified {@code tags} will be returned,
     * when {@code tags} are {@code null} then only recipes which type
     * is equal to specified {@code type} will be returned,
     * when both {@code type} and {@code tags} are {@code null} then all available
     * recipes will be returned.
     *
     * @param user
     *         user id for permission checking
     * @param tags
     *         recipe tags to search recipes, may be {@code null}
     * @param type
     *         recipe type to search recipes, may be {@code null}
     * @param skipCount
     *         count of items which should be skipped,
     *         if found items contain fewer than {@code skipCount} items
     *         then empty list will be returned
     * @param maxItems
     *         max count of items to fetch
     * @return recipes which type is equal to specified {@code type}
     * and tags contain all of specified {@code tags}
     * @throws ServerException
     *         when any error occurs
     */
    List<RecipeImpl> search(String user, List<String> tags, String type, int skipCount, int maxItems) throws ServerException;
}
