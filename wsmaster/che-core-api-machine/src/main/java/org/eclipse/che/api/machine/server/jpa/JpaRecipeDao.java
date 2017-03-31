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
package org.eclipse.che.api.machine.server.jpa;

import com.google.inject.persist.Transactional;

import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.machine.server.event.BeforeRecipeRemovedEvent;
import org.eclipse.che.api.machine.server.event.RecipePersistedEvent;
import org.eclipse.che.api.machine.server.recipe.OldRecipeImpl;
import org.eclipse.che.api.machine.server.spi.RecipeDao;
import org.eclipse.che.core.db.jpa.DuplicateKeyException;
import org.eclipse.che.core.db.jpa.IntegrityConstraintViolationException;

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
    public void create(OldRecipeImpl recipe) throws ConflictException, ServerException {
        requireNonNull(recipe);
        try {
            doCreate(recipe);
        } catch (DuplicateKeyException ex) {
            throw new ConflictException(format("OldRecipe with id %s already exists", recipe.getId()));
        } catch (IntegrityConstraintViolationException ex) {
            throw new ConflictException("Could not create recipe with permissions for non-existent user");
        } catch (RuntimeException ex) {
            throw new ServerException(ex.getLocalizedMessage(), ex);
        }
    }

    @Override
    public OldRecipeImpl update(OldRecipeImpl update) throws NotFoundException, ServerException {
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
    public OldRecipeImpl getById(String id) throws NotFoundException, ServerException {
        requireNonNull(id);

        try {
            final EntityManager manager = managerProvider.get();
            final OldRecipeImpl recipe = manager.find(OldRecipeImpl.class, id);
            if (recipe == null) {
                throw new NotFoundException(format("OldRecipe with id '%s' doesn't exist", id));
            }
            return recipe;
        } catch (RuntimeException ex) {
            throw new ServerException(ex.getLocalizedMessage(), ex);
        }
    }

    @Override
    @Transactional
    public List<OldRecipeImpl> search(String user,
                                      List<String> tags,
                                      String type,
                                      int skipCount,
                                      int maxItems) throws ServerException {
        try {
            final EntityManager manager = managerProvider.get();
            final CriteriaBuilder cb = manager.getCriteriaBuilder();
            final CriteriaQuery<OldRecipeImpl> query = cb.createQuery(OldRecipeImpl.class);
            final Root<OldRecipeImpl> fromRecipe = query.from(OldRecipeImpl.class);
            final ParameterExpression<String> typeParam = cb.parameter(String.class, "recipeType");
            final Predicate checkType = cb.or(cb.isNull(typeParam),
                                              cb.equal(fromRecipe.get("type"), typeParam));
            final TypedQuery<OldRecipeImpl> typedQuery;
            if (tags != null && !tags.isEmpty()) {
                final Join<OldRecipeImpl, String> tag = fromRecipe.join("tags");
                query.select(cb.construct(OldRecipeImpl.class, tag.getParent()))
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

    @Transactional(rollbackOn = {RuntimeException.class, ServerException.class})
    protected void doRemove(String id) throws ServerException {
        final EntityManager manager = managerProvider.get();
        final OldRecipeImpl recipe = manager.find(OldRecipeImpl.class, id);
        if (recipe != null) {
            eventService.publish(new BeforeRecipeRemovedEvent(new OldRecipeImpl(recipe))).propagateException();
            manager.remove(recipe);
            manager.flush();
        }
    }

    @Transactional
    protected OldRecipeImpl doUpdate(OldRecipeImpl update) throws NotFoundException {
        final EntityManager manager = managerProvider.get();
        if (manager.find(OldRecipeImpl.class, update.getId()) == null) {
            throw new NotFoundException(format("Could not update recipe with id %s because it doesn't exist", update.getId()));
        }
        OldRecipeImpl merged = manager.merge(update);
        manager.flush();
        return merged;
    }

    @Transactional(rollbackOn = {RuntimeException.class, ApiException.class})
    protected void doCreate(OldRecipeImpl recipe) throws ConflictException, ServerException {
        EntityManager manage = managerProvider.get();
        manage.persist(recipe);
        manage.flush();
        eventService.publish(new RecipePersistedEvent(recipe)).propagateException();
    }
}
