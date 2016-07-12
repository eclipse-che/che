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

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.jdbc.jpa.DuplicateKeyException;
import org.eclipse.che.api.core.jdbc.jpa.IntegrityConstraintViolationException;
import org.eclipse.che.api.machine.server.recipe.RecipeImpl;
import org.eclipse.che.api.machine.server.spi.RecipeDao;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
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
    private EntityManagerFactory factory;

    @Override
    public void create(RecipeImpl recipe) throws ConflictException, ServerException {
        requireNonNull(recipe);
        final EntityManager manager = factory.createEntityManager();
        try {
            manager.getTransaction().begin();
            manager.persist(recipe);
            manager.getTransaction().commit();
        } catch (DuplicateKeyException ex) {
            throw new ConflictException(format("Recipe with id %s already exists", recipe.getId()));
        } catch (IntegrityConstraintViolationException ex) {
            throw new ConflictException("Could not create recipe with permissions for non-existent user");
        } catch (RuntimeException ex) {
            throw new ServerException(ex.getLocalizedMessage(), ex);
        } finally {
            if (manager.getTransaction().isActive()) {
                manager.getTransaction().rollback();
            }
            manager.close();
        }
    }

    @Override
    public RecipeImpl update(RecipeImpl update) throws NotFoundException, ServerException {
        requireNonNull(update);
        final EntityManager manager = factory.createEntityManager();
        try {
            manager.getTransaction().begin();
            if (manager.find(RecipeImpl.class, update.getId()) == null) {
                throw new NotFoundException(format("Could not update recipe with id %s because it doesn't exist", update.getId()));
            }
            final RecipeImpl updated = manager.merge(update);
            manager.getTransaction().commit();
            return updated;
        } catch (RuntimeException ex) {
            throw new ServerException(ex.getLocalizedMessage(), ex);
        } finally {
            manager.close();
        }
    }

    @Override
    public void remove(String id) throws ServerException {
        requireNonNull(id);
        final EntityManager manager = factory.createEntityManager();
        try {
            manager.getTransaction().begin();
            final RecipeImpl recipe = manager.find(RecipeImpl.class, id);
            if (recipe != null) {
                manager.remove(recipe);
            }
            manager.getTransaction().commit();
        } catch (RuntimeException x) {
            throw new ServerException(x.getLocalizedMessage(), x);
        } finally {
            if (manager.getTransaction().isActive()) {
                manager.getTransaction().rollback();
            }
            manager.close();
        }
    }

    @Override
    public RecipeImpl getById(String id) throws NotFoundException, ServerException {
        requireNonNull(id);
        final EntityManager manager = factory.createEntityManager();
        try {
            final RecipeImpl recipe = manager.find(RecipeImpl.class, id);
            if (recipe == null) {
                throw new NotFoundException(format("Recipe with id '%s' doesn't exist", id));
            }
            return recipe;
        } catch (RuntimeException ex) {
            throw new ServerException(ex.getLocalizedMessage(), ex);
        } finally {
            if (manager.getTransaction().isActive()) {
                manager.getTransaction().rollback();
            }
            manager.close();
        }
    }

    @Override
    public List<RecipeImpl> search(String user,
                                   List<String> tags,
                                   String type,
                                   int skipCount,
                                   int maxItems) throws ServerException {
        final EntityManager manager = factory.createEntityManager();
        try {
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
        } finally {
            if (manager.getTransaction().isActive()) {
                manager.getTransaction().rollback();
            }
            manager.close();
        }
    }
}
