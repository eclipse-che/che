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
package org.eclipse.che.api.devfile.server.jpa;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.eclipse.che.api.devfile.server.jpa.JpaUserDevfileDao.UserDevfileSearchQueryBuilder.newBuilder;

import com.google.common.annotations.Beta;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.persist.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import org.eclipse.che.account.shared.model.Account;
import org.eclipse.che.account.spi.AccountDao;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.workspace.devfile.UserDevfile;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.devfile.server.event.BeforeDevfileRemovedEvent;
import org.eclipse.che.api.devfile.server.model.impl.UserDevfileImpl;
import org.eclipse.che.api.devfile.server.spi.UserDevfileDao;
import org.eclipse.che.commons.lang.Pair;
import org.eclipse.che.core.db.jpa.DuplicateKeyException;
import org.eclipse.che.core.db.jpa.IntegrityConstraintViolationException;

/** JPA based implementation of {@link UserDevfileDao}. */
@Singleton
@Beta
public class JpaUserDevfileDao implements UserDevfileDao {

  protected final Provider<EntityManager> managerProvider;
  protected final AccountDao accountDao;
  protected final EventService eventService;
  /** sorting order that would be used by default during search. */
  public static final List<Pair<String, String>> DEFAULT_ORDER =
      ImmutableList.of(new Pair<>("id", "ASC"));
  /** Set of field that is eligible to use for search. */
  public static final Set<String> VALID_SEARCH_FIELDS = ImmutableSet.of("name");
  /** Set of field that is eligible to use for sorting during search. */
  public static final Set<String> VALID_ORDER_FIELDS = ImmutableSet.of("id", "name");

  @Inject
  public JpaUserDevfileDao(
      Provider<EntityManager> managerProvider, AccountDao accountDao, EventService eventService) {
    this.managerProvider = managerProvider;
    this.accountDao = accountDao;
    this.eventService = eventService;
  }

  @Override
  public UserDevfile create(UserDevfile userDevfile) throws ConflictException, ServerException {
    requireNonNull(userDevfile);
    try {
      Account account = accountDao.getByName(userDevfile.getNamespace());
      UserDevfileImpl userDevfileImpl = new UserDevfileImpl(userDevfile, account);
      doCreate(userDevfileImpl);
      return userDevfileImpl;
    } catch (DuplicateKeyException ex) {
      throw new ConflictException(
          format(
              "Devfile with name '%s' already exists in the specified account '%s'",
              userDevfile.getName(), userDevfile.getNamespace()));
    } catch (IntegrityConstraintViolationException ex) {
      throw new ConflictException(
          "Could not create devfile with creator that refers to a non-existent user");
    } catch (RuntimeException ex) {
      throw new ServerException(ex.getMessage(), ex);
    } catch (NotFoundException e) {
      throw new ConflictException(
          format(
              "Not able to create devfile in requested namespace %s bacause it is not found",
              userDevfile.getNamespace()));
    }
  }

  @Override
  public Optional<UserDevfile> update(UserDevfile userDevfile)
      throws ConflictException, ServerException, NotFoundException {
    requireNonNull(userDevfile);
    try {
      Account account = accountDao.getByName(userDevfile.getNamespace());
      return doUpdate(new UserDevfileImpl(userDevfile, account)).map(UserDevfileImpl::new);
    } catch (DuplicateKeyException ex) {
      throw new ConflictException(
          format(
              "Devfile with name '%s' already exists in current account '%s'",
              userDevfile.getName(), userDevfile.getNamespace()));
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
  @Transactional(rollbackOn = {ServerException.class, RuntimeException.class})
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

  @Transactional(rollbackOn = {ServerException.class, RuntimeException.class})
  @Override
  public Page<UserDevfile> getByNamespace(String namespace, int maxItems, long skipCount)
      throws ServerException {
    requireNonNull(namespace, "Required non-null namespace");
    try {
      final EntityManager manager = managerProvider.get();
      final List<UserDevfileImpl> list =
          manager
              .createNamedQuery("UserDevfile.getByNamespace", UserDevfileImpl.class)
              .setParameter("namespace", namespace)
              .setMaxResults(maxItems)
              .setFirstResult((int) skipCount)
              .getResultList()
              .stream()
              .map(UserDevfileImpl::new)
              .collect(Collectors.toList());
      final long count =
          manager
              .createNamedQuery("UserDevfile.getByNamespaceCount", Long.class)
              .setParameter("namespace", namespace)
              .getSingleResult();
      return new Page<>(list, skipCount, maxItems, count);
    } catch (RuntimeException x) {
      throw new ServerException(x.getLocalizedMessage(), x);
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

    return doGetDevfiles(
        maxItems, skipCount, filter, order, () -> newBuilder(managerProvider.get()));
  }

  @Transactional(rollbackOn = {ServerException.class})
  protected Page<UserDevfile> doGetDevfiles(
      int maxItems,
      int skipCount,
      List<Pair<String, String>> filter,
      List<Pair<String, String>> order,
      Supplier<UserDevfileSearchQueryBuilder> queryBuilderSupplier)
      throws ServerException {
    if (filter != null && !filter.isEmpty()) {
      List<Pair<String, String>> invalidFilter =
          filter
              .stream()
              .filter(p -> !VALID_SEARCH_FIELDS.contains(p.first.toLowerCase()))
              .collect(toList());
      if (!invalidFilter.isEmpty()) {
        throw new IllegalArgumentException(
            "Filtering allowed only by " + VALID_SEARCH_FIELDS + " but got: " + invalidFilter);
      }
    }
    List<Pair<String, String>> effectiveOrder = DEFAULT_ORDER;
    if (order != null && !order.isEmpty()) {
      List<Pair<String, String>> invalidOrder =
          order
              .stream()
              .filter(p -> !VALID_ORDER_FIELDS.contains(p.first.toLowerCase()))
              .collect(toList());
      if (!invalidOrder.isEmpty()) {
        throw new IllegalArgumentException(
            "Order allowed only by " + VALID_ORDER_FIELDS + "¬ but got: " + invalidOrder);
      }

      List<Pair<String, String>> invalidSortOrder =
          order
              .stream()
              .filter(p -> !p.second.equalsIgnoreCase("asc") && !p.second.equalsIgnoreCase("desc"))
              .collect(Collectors.toList());
      if (!invalidSortOrder.isEmpty()) {
        throw new IllegalArgumentException(
            "Invalid sort order direction. Possible values are 'asc' or 'desc' but got: "
                + invalidSortOrder);
      }
      effectiveOrder = order;
    }
    try {
      final long count =
          queryBuilderSupplier.get().withFilter(filter).buildCountQuery().getSingleResult();

      if (count == 0) {
        return new Page<>(emptyList(), skipCount, maxItems, count);
      }
      List<UserDevfileImpl> result =
          queryBuilderSupplier
              .get()
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

  @Override
  @Transactional
  public long getTotalCount() throws ServerException {
    try {
      return managerProvider
          .get()
          .createNamedQuery("UserDevfile.getTotalCount", Long.class)
          .getSingleResult();
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

  @Transactional(rollbackOn = {RuntimeException.class, ServerException.class})
  protected void doRemove(String id) throws ServerException {
    final EntityManager manager = managerProvider.get();
    final UserDevfileImpl devfile = manager.find(UserDevfileImpl.class, id);
    if (devfile != null) {
      eventService
          .publish(new BeforeDevfileRemovedEvent(new UserDevfileImpl(devfile)))
          .propagateException();
      manager.remove(devfile);
      manager.flush();
    }
  }

  public static class UserDevfileSearchQueryBuilder {
    protected EntityManager entityManager;
    protected int maxItems;
    protected int skipCount;
    protected String filter;
    protected Map<String, String> params;
    protected String order;

    public UserDevfileSearchQueryBuilder(EntityManager entityManager) {
      this.entityManager = entityManager;
      this.params = new HashMap<>();
      this.filter = "";
      this.order = "";
    }

    public static UserDevfileSearchQueryBuilder newBuilder(EntityManager entityManager) {
      return new JpaUserDevfileDao.UserDevfileSearchQueryBuilder(entityManager);
    }

    public UserDevfileSearchQueryBuilder withMaxItems(int maxItems) {
      this.maxItems = maxItems;
      return this;
    }

    public UserDevfileSearchQueryBuilder withSkipCount(int skipCount) {
      this.skipCount = skipCount;
      return this;
    }

    public UserDevfileSearchQueryBuilder withFilter(List<Pair<String, String>> filter) {
      if (filter == null || filter.isEmpty()) {
        return this;
      }
      final StringJoiner matcher = new StringJoiner(" AND ", " WHERE ", " ");
      int i = 0;
      for (Pair<String, String> attribute : filter) {
        if (!VALID_SEARCH_FIELDS.contains(attribute.first.toLowerCase())) {
          throw new IllegalArgumentException(
              "Filtering allowed only by " + VALID_SEARCH_FIELDS + " but got: " + attribute.first);
        }
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

    public UserDevfileSearchQueryBuilder withOrder(List<Pair<String, String>> order) {
      if (order == null || order.isEmpty()) {
        return this;
      }
      final StringJoiner matcher = new StringJoiner(", ", " ORDER BY ", " ");
      for (Pair<String, String> pair : order) {
        if (!VALID_ORDER_FIELDS.contains(pair.first.toLowerCase())) {
          throw new IllegalArgumentException(
              "Order allowed only by " + VALID_ORDER_FIELDS + " but got: " + pair.first);
        }
        matcher.add("userdevfile." + pair.first + " " + pair.second);
      }
      this.order = matcher.toString();

      return this;
    }

    public TypedQuery<Long> buildCountQuery() {
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

    public TypedQuery<UserDevfileImpl> buildSelectItemsQuery() {

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
