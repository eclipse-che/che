/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.multiuser.permission.devfile.server.spi.jpa;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.List;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import org.eclipse.che.account.spi.AccountDao;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.workspace.devfile.UserDevfile;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.devfile.server.jpa.JpaUserDevfileDao;
import org.eclipse.che.api.devfile.server.model.impl.UserDevfileImpl;
import org.eclipse.che.api.devfile.server.spi.UserDevfileDao;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.lang.Pair;
import org.eclipse.che.commons.subject.Subject;

/** JPA based implementation of {@link UserDevfileDao}. */
@Singleton
public class MultiuserJpaUserDevfileDao extends JpaUserDevfileDao {

  @Inject
  public MultiuserJpaUserDevfileDao(
      Provider<EntityManager> managerProvider, AccountDao accountDao, EventService eventService) {
    super(managerProvider, accountDao, eventService);
  }

  @Override
  public Page<UserDevfile> getDevfiles(
      int maxItems,
      int skipCount,
      List<Pair<String, String>> filter,
      List<Pair<String, String>> order)
      throws ServerException {
    checkArgument(maxItems > 0, "The number of items has to be positive.");
    checkArgument(
        skipCount >= 0,
        "The number of items to skip can't be negative or greater than " + Integer.MAX_VALUE);

    final Subject subject = EnvironmentContext.getCurrent().getSubject();
    if (subject.isAnonymous()) {
      throw new ServerException("Unexpected state. Current user is not set.");
    }

    return doGetDevfiles(
        maxItems,
        skipCount,
        filter,
        order,
        () ->
            MultiuserUserDevfileSearchQueryBuilder.newBuilder(managerProvider.get())
                .withUserId(subject.getUserId()));
  }

  public static class MultiuserUserDevfileSearchQueryBuilder
      extends JpaUserDevfileDao.UserDevfileSearchQueryBuilder {

    MultiuserUserDevfileSearchQueryBuilder(EntityManager entityManager) {
      super(entityManager);
    }

    public MultiuserUserDevfileSearchQueryBuilder withUserId(String userId) {
      params.put("userId", userId);
      return this;
    }

    public static MultiuserUserDevfileSearchQueryBuilder newBuilder(EntityManager entityManager) {
      return new MultiuserUserDevfileSearchQueryBuilder(entityManager);
    }

    @Override
    public JpaUserDevfileDao.UserDevfileSearchQueryBuilder withFilter(
        List<Pair<String, String>> filter) {
      super.withFilter(filter);
      if (this.filter.isEmpty()) {
        this.filter = "WHERE permission.userId = :userId AND 'read' MEMBER OF permission.actions";
      } else {
        this.filter += " AND permission.userId = :userId AND 'read' MEMBER OF permission.actions";
      }
      return this;
    }

    @Override
    public TypedQuery<Long> buildCountQuery() {
      StringBuilder query =
          new StringBuilder()
              .append("SELECT ")
              .append(" COUNT(userdevfile) ")
              .append("FROM UserDevfilePermission permission ")
              .append("LEFT JOIN permission.userDevfile userdevfile ")
              .append(filter);
      TypedQuery<Long> typedQuery = entityManager.createQuery(query.toString(), Long.class);
      params.forEach(typedQuery::setParameter);
      return typedQuery;
    }

    @Override
    public TypedQuery<UserDevfileImpl> buildSelectItemsQuery() {
      StringBuilder query =
          new StringBuilder()
              .append("SELECT ")
              .append(" userdevfile ")
              .append("FROM UserDevfilePermission permission ")
              .append("LEFT JOIN permission.userDevfile userdevfile ")
              .append(filter)
              .append(order);
      TypedQuery<UserDevfileImpl> typedQuery =
          entityManager
              .createQuery(query.toString(), UserDevfileImpl.class)
              .setFirstResult(skipCount)
              .setMaxResults(maxItems);
      params.forEach((k, v) -> typedQuery.setParameter(k, v));
      return typedQuery;
    }
  }
}
