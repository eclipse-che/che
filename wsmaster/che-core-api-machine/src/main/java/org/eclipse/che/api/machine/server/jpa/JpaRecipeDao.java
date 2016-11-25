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
import org.eclipse.che.core.db.jpa.DuplicateKeyException;
import org.eclipse.che.core.db.jpa.IntegrityConstraintViolationException;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.machine.server.event.RecipePersistedEvent;
import org.eclipse.che.api.machine.server.recipe.RecipeImpl;
import org.eclipse.che.api.machine.server.spi.RecipeDao;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
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

    @Inject
    private EventService eventService;

    @Override
    public void create(RecipeImpl recipe) throws ConflictException, ServerException {
        requireNonNull(recipe);
        try {
            doCreateRecipe(recipe);
            eventService.publish(new RecipePersistedEvent(recipe));
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
    @Transactional
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
    @Transactional
    public List<RecipeImpl> search(String user,
                                   List<String> tags,
                                   String type,
                                   int skipCount,
                                   int maxItems) throws ServerException {
        try {
            final EntityManager manager = managerProvider.get();
            final CriteriaBuilder cb = manager.getCriteriaBuilder();
            final CriteriaQuery<RecipeImpl> query = cb.createQuery(RecipeImpl.class);
            final Root<RecipeImpl> fromRecipe = query.from(RecipeImpl.class);
            final ParameterExpression<String> typeParam = cb.parameter(String.class, "recipeType");
            final Predicate checkType = cb.or(cb.isNull(typeParam),
                                              cb.equal(fromRecipe.get("type"), typeParam));
            final TypedQuery<RecipeImpl> typedQuery;
            if (tags != null && !tags.isEmpty()) {
                final Join<RecipeImpl, String> tag = fromRecipe.join("tags");
                query.select(cb.construct(RecipeImpl.class, tag.getParent()))
                     .where(cb.and(checkType, tag.in(tags)))
                     .groupBy(fromRecipe.get("id"))
                     .having(cb.equal(cb.count(tag), tags.size()));
                typedQuery = manager.createQuery(query)
                                    .setParameter("tags", tags);
            } else {
                typedQuery = manager.createQuery(query.where(checkType));
            }
            return typedQuery.setParameter("recipeType", type)
                             .setFirstResult(skipCount)
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
