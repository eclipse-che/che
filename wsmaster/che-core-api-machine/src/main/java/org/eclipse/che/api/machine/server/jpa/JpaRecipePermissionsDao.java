/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.machine.server.jpa;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

import com.google.inject.persist.Transactional;
import java.io.IOException;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.machine.server.event.BeforeRecipeRemovedEvent;
import org.eclipse.che.api.machine.server.recipe.RecipePermissionsImpl;
import org.eclipse.che.api.permission.server.AbstractPermissionsDomain;
import org.eclipse.che.api.permission.server.jpa.AbstractJpaPermissionsDao;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.core.db.cascade.CascadeEventSubscriber;

/**
 * JPA based implementation of recipe permissions DAO.
 *
 * @author Max Shaposhnik
 */
@Singleton
public class JpaRecipePermissionsDao extends AbstractJpaPermissionsDao<RecipePermissionsImpl> {

  @Inject
  public JpaRecipePermissionsDao(AbstractPermissionsDomain<RecipePermissionsImpl> domain)
      throws IOException {
    super(domain);
  }

  @Override
  public RecipePermissionsImpl get(String userId, String instanceId)
      throws ServerException, NotFoundException {
    requireNonNull(instanceId, "Recipe identifier required");
    requireNonNull(userId, "User identifier required");
    try {
      return new RecipePermissionsImpl(getEntity(wildcardToNull(userId), instanceId));
    } catch (RuntimeException x) {
      throw new ServerException(x.getLocalizedMessage(), x);
    }
  }

  @Override
  public List<RecipePermissionsImpl> getByUser(String userId) throws ServerException {
    requireNonNull(userId, "User identifier required");
    return doGetByUser(wildcardToNull(userId))
        .stream()
        .map(RecipePermissionsImpl::new)
        .collect(toList());
  }

  @Override
  @Transactional
  public Page<RecipePermissionsImpl> getByInstance(String instanceId, int maxItems, long skipCount)
      throws ServerException {
    requireNonNull(instanceId, "Recipe identifier required");
    checkArgument(
        skipCount <= Integer.MAX_VALUE,
        "The number of items to skip can't be greater than " + Integer.MAX_VALUE);
    try {
      final EntityManager entityManager = managerProvider.get();
      final List<RecipePermissionsImpl> recipePermissionsList =
          entityManager
              .createNamedQuery("RecipePermissions.getByRecipeId", RecipePermissionsImpl.class)
              .setParameter("recipeId", instanceId)
              .setMaxResults(maxItems)
              .setFirstResult((int) skipCount)
              .getResultList()
              .stream()
              .map(RecipePermissionsImpl::new)
              .collect(toList());
      final Long permissionsCount =
          entityManager
              .createNamedQuery("RecipePermissions.getCountByRecipeId", Long.class)
              .setParameter("recipeId", instanceId)
              .getSingleResult();

      return new Page<>(recipePermissionsList, skipCount, maxItems, permissionsCount);
    } catch (RuntimeException e) {
      throw new ServerException(e.getLocalizedMessage(), e);
    }
  }

  @Override
  protected RecipePermissionsImpl getEntity(String userId, String instanceId)
      throws NotFoundException {
    try {
      return doGet(userId, instanceId);
    } catch (NoResultException e) {
      throw new NotFoundException(
          format("Permissions on recipe '%s' of user '%s' was not found.", instanceId, userId));
    }
  }

  @Transactional
  protected RecipePermissionsImpl doGet(String userId, String instanceId) {
    userId = wildcardToNull(userId);
    if (userId == null) {
      return managerProvider
          .get()
          .createNamedQuery("RecipePermissions.getByRecipeIdPublic", RecipePermissionsImpl.class)
          .setParameter("recipeId", instanceId)
          .getSingleResult();
    } else {
      return managerProvider
          .get()
          .createNamedQuery("RecipePermissions.getByUserAndRecipeId", RecipePermissionsImpl.class)
          .setParameter("recipeId", instanceId)
          .setParameter("userId", userId)
          .getSingleResult();
    }
  }

  @Transactional
  protected List<RecipePermissionsImpl> doGetByUser(@Nullable String userId)
      throws ServerException {
    try {
      return managerProvider
          .get()
          .createNamedQuery("RecipePermissions.getByUserId", RecipePermissionsImpl.class)
          .setParameter("userId", userId)
          .getResultList();
    } catch (RuntimeException e) {
      throw new ServerException(e.getLocalizedMessage(), e);
    }
  }

  @Singleton
  public static class RemovePermissionsBeforeRecipeRemovedEventSubscriber
      extends CascadeEventSubscriber<BeforeRecipeRemovedEvent> {
    private static final int PAGE_SIZE = 100;

    @Inject private EventService eventService;
    @Inject private JpaRecipePermissionsDao dao;

    @PostConstruct
    public void subscribe() {
      eventService.subscribe(this, BeforeRecipeRemovedEvent.class);
    }

    @PreDestroy
    public void unsubscribe() {
      eventService.unsubscribe(this, BeforeRecipeRemovedEvent.class);
    }

    @Override
    public void onCascadeEvent(BeforeRecipeRemovedEvent event) throws Exception {
      removeRecipePermissions(event.getRecipe().getId(), PAGE_SIZE);
    }

    public void removeRecipePermissions(String recipeId, int pageSize)
        throws ServerException, NotFoundException {
      Page<RecipePermissionsImpl> recipePermissionsPage;
      do {
        // skip count always equals to 0 because elements will be shifted after removing previous items
        recipePermissionsPage = dao.getByInstance(recipeId, pageSize, 0);
        for (RecipePermissionsImpl permissions : recipePermissionsPage.getItems()) {
          dao.remove(permissions.getUserId(), permissions.getInstanceId());
        }
      } while (recipePermissionsPage.hasNextPage());
    }
  }
}
