/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.devfile.server.jpa;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.eclipse.che.api.devfile.server.jpa.JpaUserDevfileDao.UserDevfileSearchQueryBuilder.newBuilder;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import com.google.inject.persist.Transactional;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.workspace.devfile.UserDevfile;
import org.eclipse.che.api.devfile.server.model.impl.UserDevfileImpl;
import org.eclipse.che.api.devfile.server.spi.UserDevfileDao;
import org.eclipse.che.commons.lang.Pair;
import org.eclipse.che.core.db.jpa.DuplicateKeyException;
import org.eclipse.che.core.db.jpa.IntegrityConstraintViolationException;

@Singleton
@Beta
public class JpaUserDevfileDao implements UserDevfileDao {

  @Inject private Provider<EntityManager> managerProvider;
  public static final List<Pair<String, String>> DEFAULT_ORDER =
      ImmutableList.of(new Pair<>("id", "ASC"));

  @Override
  public UserDevfile create(UserDevfile devfile) throws ConflictException, ServerException {
    requireNonNull(devfile);
    try {
      doCreate(new UserDevfileImpl(devfile));
    } catch (DuplicateKeyException ex) {
      throw new ConflictException(
          format("Devfile with name '%s' already exists for current user", devfile.getName()));
    } catch (IntegrityConstraintViolationException ex) {
      throw new ConflictException(
          "Could not create devfile with creator that refers on non-existent user");
    } catch (RuntimeException ex) {
      throw new ServerException(ex.getMessage(), ex);
    }
    return new UserDevfileImpl(devfile);
  }

  @Override
  public Optional<UserDevfile> update(UserDevfile update)
      throws ConflictException, ServerException {
    requireNonNull(update);
    try {
      return doUpdate(new UserDevfileImpl(update)).map(UserDevfileImpl::new);
    } catch (DuplicateKeyException ex) {
      throw new ConflictException(
          format("Devfile with name '%s' already exists for current user", update.getName()));
    } catch (RuntimeException ex) {
      throw new ServerException(ex.getLocalizedMessage(), ex);
    }
  }

  @Override
  public void remove(String id) throws ServerException {
    requireNonNull(id);
    try {
      doRemove(id);
    } catch (RuntimeException ex) {
      throw new ServerException(ex.getLocalizedMessage(), ex);
    }
  }

  @Override
  @Transactional(rollbackOn = {ServerException.class})
  public Optional<UserDevfile> getById(String id) throws ServerException {
    requireNonNull(id);
    try {
      final UserDevfileImpl devfile = managerProvider.get().find(UserDevfileImpl.class, id);
      if (devfile == null) {
        return Optional.empty();
      }
      return Optional.of(new UserDevfileImpl(devfile));
    } catch (RuntimeException ex) {
      throw new ServerException(ex.getLocalizedMessage(), ex);
    }
  }

  @Override
  @Transactional(rollbackOn = {ServerException.class})
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

    if (filter != null && !filter.isEmpty()) {
      List<Pair<String, String>> invalidFilter =
          filter
              .stream()
              .filter(p -> !p.first.equalsIgnoreCase("devfile.metadata.name"))
              .collect(toList());
      if (!invalidFilter.isEmpty()) {
        throw new IllegalArgumentException(
            "Filtering allowed only on `devfile.metadata.name`. But got: " + invalidFilter);
      }
    }
    List<Pair<String, String>> effectiveOrder = DEFAULT_ORDER;
    if (order != null && !order.isEmpty()) {
      List<Pair<String, String>> invalidSortOrder =
          order
              .stream()
              .filter(p -> !p.second.equalsIgnoreCase("asc"))
              .filter(p -> !p.second.equalsIgnoreCase("desc"))
              .collect(Collectors.toList());
      if (!invalidSortOrder.isEmpty()) {
        throw new IllegalArgumentException(
            "Invalid sort order direction. Possible values 'asc' or 'desc'. But got: "
                + invalidSortOrder);
      }
      effectiveOrder = order;
    }
    try {
      final long count =
          newBuilder(managerProvider.get()).withFilter(filter).buildCountQuery().getSingleResult();

      if (count == 0) {
        return new Page<>(emptyList(), skipCount, maxItems, count);
      }
      List<UserDevfileImpl> result =
          newBuilder(managerProvider.get())
              .withFilter(filter)
              .withOrder(effectiveOrder)
              .withMaxItems(maxItems)
              .withSkipCount(skipCount)
              .buildSelectItemsQuery()
              .getResultList()
              .stream()
              .map(UserDevfileImpl::new)
              .collect(toList());
      return new Page<>(result, skipCount, maxItems, count);

    } catch (RuntimeException x) {
      throw new ServerException(x.getLocalizedMessage(), x);
    }
  }

  @Transactional
  protected void doCreate(UserDevfileImpl devfile) {
    final EntityManager manager = managerProvider.get();
    manager.persist(devfile);
    manager.flush();
  }

  @Transactional
  protected Optional<UserDevfileImpl> doUpdate(UserDevfileImpl update) {
    final EntityManager manager = managerProvider.get();
    if (manager.find(UserDevfileImpl.class, update.getId()) == null) {
      return Optional.empty();
    }
    UserDevfileImpl merged = manager.merge(update);
    manager.flush();
    return Optional.of(merged);
  }

  @Transactional
  protected void doRemove(String id) {
    final EntityManager manager = managerProvider.get();
    final UserDevfileImpl devfile = manager.find(UserDevfileImpl.class, id);
    if (devfile != null) {
      manager.remove(devfile);
      manager.flush();
    }
  }

  protected static class UserDevfileSearchQueryBuilder {
    private EntityManager entityManager;
    private String userId;
    private int maxItems;
    private int skipCount;
    private String filter;
    private Map<String, String> params;
    private String order;

    private UserDevfileSearchQueryBuilder(EntityManager entityManager) {
      this.entityManager = entityManager;
      this.params = Collections.emptyMap();
    }

    static UserDevfileSearchQueryBuilder newBuilder(EntityManager entityManager) {
      return new JpaUserDevfileDao.UserDevfileSearchQueryBuilder(entityManager);
    }

    protected UserDevfileSearchQueryBuilder withUserId(String userId) {
      this.userId = userId;
      return this;
    }

    protected UserDevfileSearchQueryBuilder withMaxItems(int maxItems) {
      this.maxItems = maxItems;
      return this;
    }

    protected UserDevfileSearchQueryBuilder withSkipCount(int skipCount) {
      this.skipCount = skipCount;
      return this;
    }

    protected UserDevfileSearchQueryBuilder withFilter(List<Pair<String, String>> filter) {
      if (filter == null || filter.isEmpty()) {
        this.filter = "";
        return this;
      }
      this.params = new HashMap<>();
      final StringJoiner matcher = new StringJoiner(" AND ", " WHERE ", " ");
      int i = 0;
      for (Pair<String, String> attribute : filter) {

        final String parameterName = "parameterName" + i++;
        if (attribute.second.startsWith("like:")) {
          params.put(parameterName, attribute.second.substring(5));
          matcher.add("userdevfile." + attribute.first + " LIKE :" + parameterName);
        } else {
          params.put(parameterName, attribute.second);
          matcher.add("userdevfile." + attribute.first + " = :" + parameterName);
        }
      }
      this.filter = matcher.toString();
      return this;
    }

    protected UserDevfileSearchQueryBuilder withOrder(List<Pair<String, String>> order) {
      if (order == null || order.isEmpty()) {
        this.order = "";
        return this;
      }
      final StringJoiner matcher = new StringJoiner(", ", " ORDER BY ", " ");
      order.forEach(pair -> matcher.add("userdevfile." + pair.first + " " + pair.second));
      this.order = matcher.toString();

      return this;
    }

    protected TypedQuery<Long> buildCountQuery() {
      StringBuilder query =
          new StringBuilder()
              .append("SELECT ")
              .append(" COUNT(userdevfile) ")
              .append("FROM UserDevfile userdevfile")
              .append(filter);
      TypedQuery<Long> typedQuery = entityManager.createQuery(query.toString(), Long.class);
      params.forEach((k, v) -> typedQuery.setParameter(k, v));
      return typedQuery;
    }

    protected TypedQuery<UserDevfileImpl> buildSelectItemsQuery() {

      StringBuilder query =
          new StringBuilder()
              .append("SELECT ")
              .append(" userdevfile ")
              .append("FROM UserDevfile userdevfile")
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
