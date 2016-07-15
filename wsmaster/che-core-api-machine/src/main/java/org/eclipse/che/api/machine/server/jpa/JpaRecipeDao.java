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
package org.eclipse.che.api.machine.server.jpa;

import com.google.inject.persist.Transactional;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.jdbc.jpa.DuplicateKeyException;
import org.eclipse.che.api.core.jdbc.jpa.IntegrityConstraintViolationException;
import org.eclipse.che.api.machine.server.recipe.RecipeImpl;
import org.eclipse.che.api.machine.server.spi.RecipeDao;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.Collections;
import java.util.List;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

/**
 * Implementation of {@link RecipeDao}.
 *
 * @author Anton Korneta
 */
@Singleton
public class JpaRecipeDao implements RecipeDao {

    @Inject
    private Provider<EntityManager> managerProvider;

    @Override
    public void create(RecipeImpl recipe) throws ConflictException, ServerException {
        requireNonNull(recipe);
        try {
            doCreateRecipe(recipe);
        } catch (DuplicateKeyException ex) {
            throw new ConflictException(format("Recipe with id %s already exists", recipe.getId()));
        } catch (IntegrityConstraintViolationException ex) {
            throw new ConflictException("Could not create recipe with permissions for non-existent user");
        } catch (RuntimeException ex) {
            throw new ServerException(ex.getLocalizedMessage(), ex);
        }
    }

    @Override
    public RecipeImpl update(RecipeImpl update) throws NotFoundException, ServerException {
        requireNonNull(update);
        try {
            return doUpdate(update);
        } catch (RuntimeException ex) {
            throw new ServerException(ex.getLocalizedMessage(), ex);
        }
    }

    @Override
    public void remove(String id) throws ServerException {
        requireNonNull(id);
        try {
            doRemove(id);
        } catch (RuntimeException x) {
            throw new ServerException(x.getLocalizedMessage(), x);
        }
    }

    @Override
    public RecipeImpl getById(String id) throws NotFoundException, ServerException {
        requireNonNull(id);

        try {
            final EntityManager manager = managerProvider.get();
            final RecipeImpl recipe = manager.find(RecipeImpl.class, id);
            if (recipe == null) {
                throw new NotFoundException(format("Recipe with id '%s' doesn't exist", id));
            }
            return recipe;
        } catch (RuntimeException ex) {
            throw new ServerException(ex.getLocalizedMessage(), ex);
        }
    }

    @Override
    public List<RecipeImpl> search(String user,
                                   List<String> tags,
                                   String type,
                                   int skipCount,
                                   int maxItems) throws ServerException {

        try {
            final EntityManager manager = managerProvider.get();
            tags = tags == null ? Collections.emptyList() : tags;
            final TypedQuery<RecipeImpl> query = manager.createNamedQuery("Recipe.search", RecipeImpl.class)
                                                        .setParameter("user", user)
                                                        .setParameter("tags", tags)
                                                        .setParameter("recipeType", type)
                                                        .setParameter("requiredCount", tags.size());
            return query.setFirstResult(skipCount)
                        .setMaxResults(maxItems)
                        .getResultList();
        } catch (RuntimeException ex) {
            throw new ServerException(ex.getLocalizedMessage(), ex);
        }
    }

    @Transactional
    protected void doRemove(String id) {
        final EntityManager manager = managerProvider.get();
        final RecipeImpl recipe = manager.find(RecipeImpl.class, id);
        if (recipe != null) {
            manager.remove(recipe);
        }
    }

    @Transactional
    protected RecipeImpl doUpdate(RecipeImpl update) throws NotFoundException {
        final EntityManager manager = managerProvider.get();
        if (manager.find(RecipeImpl.class, update.getId()) == null) {
            throw new NotFoundException(format("Could not update recipe with id %s because it doesn't exist", update.getId()));
        }
        return manager.merge(update);
    }

    @Transactional
    protected void doCreateRecipe(RecipeImpl recipe) {
        managerProvider.get().persist(recipe);
    }
}
