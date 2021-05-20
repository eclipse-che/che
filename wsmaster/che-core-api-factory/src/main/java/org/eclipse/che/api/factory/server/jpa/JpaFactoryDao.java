/*
 * Copyright (c) 2012-2020 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.factory.server.jpa;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.eclipse.che.api.core.Pages.iterate;

import com.google.inject.persist.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.factory.server.model.impl.FactoryImpl;
import org.eclipse.che.api.factory.server.spi.FactoryDao;
import org.eclipse.che.api.user.server.event.BeforeUserRemovedEvent;
import org.eclipse.che.api.workspace.server.model.impl.ProjectConfigImpl;
import org.eclipse.che.commons.lang.Pair;
import org.eclipse.che.core.db.cascade.CascadeEventSubscriber;
import org.eclipse.che.core.db.jpa.DuplicateKeyException;
import org.eclipse.che.core.db.jpa.IntegrityConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Anton Korneta */
@Singleton
public class JpaFactoryDao implements FactoryDao {
  private static final Logger LOG = LoggerFactory.getLogger(JpaFactoryDao.class);

  @Inject private Provider<EntityManager> managerProvider;

  @Override
  public FactoryImpl create(FactoryImpl factory) throws ConflictException, ServerException {
    requireNonNull(factory);
    try {
      doCreate(factory);
    } catch (DuplicateKeyException ex) {
      throw new ConflictException(
          format("Factory with name '%s' already exists for current user", factory.getName()));
    } catch (IntegrityConstraintViolationException ex) {
      throw new ConflictException(
          "Could not create factory with creator that refers to a non-existent user");
    } catch (RuntimeException ex) {
      throw new ServerException(ex.getLocalizedMessage(), ex);
    }
    return new FactoryImpl(factory);
  }

  @Override
  public FactoryImpl update(FactoryImpl update)
      throws NotFoundException, ConflictException, ServerException {
    requireNonNull(update);
    try {
      return new FactoryImpl(doUpdate(update));
    } catch (DuplicateKeyException ex) {
      throw new ConflictException(
          format("Factory with name '%s' already exists for current user", update.getName()));
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
  public FactoryImpl getById(String id) throws NotFoundException, ServerException {
    requireNonNull(id);
    try {
      final FactoryImpl factory = managerProvider.get().find(FactoryImpl.class, id);
      if (factory == null) {
        throw new NotFoundException(format("Factory with id '%s' doesn't exist", id));
      }
      return new FactoryImpl(factory);
    } catch (RuntimeException ex) {
      throw new ServerException(ex.getLocalizedMessage(), ex);
    }
  }

  @Override
  @Transactional(rollbackOn = {ServerException.class})
  public Page<FactoryImpl> getByAttributes(
      int maxItems, int skipCount, List<Pair<String, String>> attributes) throws ServerException {
    checkArgument(maxItems >= 0, "The number of items to return can't be negative.");
    checkArgument(
        skipCount >= 0 && skipCount <= Integer.MAX_VALUE,
        "The number of items to skip can't be negative or greater than " + Integer.MAX_VALUE);
    try {
      LOG.debug(
          "FactoryDao#getByAttributes #maxItems: {} #skipCount: {}, #attributes: {}",
          maxItems,
          skipCount,
          attributes);
      final long count = countFactoriesByAttributes(attributes);
      if (count == 0) {
        return new Page<>(emptyList(), skipCount, maxItems, count);
      }
      List<FactoryImpl> result = getFactoriesByAttributes(maxItems, skipCount, attributes);
      return new Page<>(result, skipCount, maxItems, count);
    } catch (RuntimeException ex) {
      throw new ServerException(ex.getLocalizedMessage(), ex);
    }
  }

  @Override
  @Transactional(rollbackOn = {ServerException.class})
  public Page<FactoryImpl> getByUser(String userId, int maxItems, long skipCount)
      throws ServerException {
    requireNonNull(userId);
    final Pair<String, String> factoryCreator = Pair.of("creator.userId", userId);
    try {
      long totalCount = countFactoriesByAttributes(singletonList(factoryCreator));
      return new Page<>(
          getFactoriesByAttributes(maxItems, skipCount, singletonList(factoryCreator)),
          skipCount,
          maxItems,
          totalCount);
    } catch (RuntimeException ex) {
      throw new ServerException(ex.getMessage(), ex);
    }
  }

  private List<FactoryImpl> getFactoriesByAttributes(
      int maxItems, long skipCount, List<Pair<String, String>> attributes) {
    final Map<String, String> params = new HashMap<>();
    StringBuilder query = new StringBuilder("SELECT factory FROM Factory factory");
    if (!attributes.isEmpty()) {
      final StringJoiner matcher = new StringJoiner(" AND ", " WHERE ", " ");
      int i = 0;
      for (Pair<String, String> attribute : attributes) {
        final String parameterName = "parameterName" + i++;
        params.put(parameterName, attribute.second);
        matcher.add("factory." + attribute.first + " = :" + parameterName);
      }
      query.append(matcher);
    }
    TypedQuery<FactoryImpl> typedQuery =
        managerProvider
            .get()
            .createQuery(query.toString(), FactoryImpl.class)
            .setFirstResult((int) skipCount)
            .setMaxResults(maxItems);
    for (Map.Entry<String, String> entry : params.entrySet()) {
      typedQuery.setParameter(entry.getKey(), entry.getValue());
    }
    return typedQuery.getResultList().stream().map(FactoryImpl::new).collect(toList());
  }

  private Long countFactoriesByAttributes(List<Pair<String, String>> attributes) {
    final Map<String, String> params = new HashMap<>();
    StringBuilder query = new StringBuilder("SELECT COUNT(factory) FROM Factory factory");
    if (!attributes.isEmpty()) {
      final StringJoiner matcher = new StringJoiner(" AND ", " WHERE ", " ");
      int i = 0;
      for (Pair<String, String> attribute : attributes) {
        final String parameterName = "parameterName" + i++;
        params.put(parameterName, attribute.second);
        matcher.add("factory." + attribute.first + " = :" + parameterName);
      }
      query.append(matcher);
    }
    TypedQuery<Long> typedQuery = managerProvider.get().createQuery(query.toString(), Long.class);
    for (Map.Entry<String, String> entry : params.entrySet()) {
      typedQuery.setParameter(entry.getKey(), entry.getValue());
    }
    return typedQuery.getSingleResult();
  }

  @Transactional
  protected void doCreate(FactoryImpl factory) {
    final EntityManager manager = managerProvider.get();
    if (factory.getWorkspace() != null) {
      factory.getWorkspace().getProjects().forEach(ProjectConfigImpl::prePersistAttributes);
    }
    manager.persist(factory);
    manager.flush();
  }

  @Transactional
  protected FactoryImpl doUpdate(FactoryImpl update) throws NotFoundException {
    final EntityManager manager = managerProvider.get();
    if (manager.find(FactoryImpl.class, update.getId()) == null) {
      throw new NotFoundException(
          format("Could not update factory with id %s because it doesn't exist", update.getId()));
    }
    if (update.getWorkspace() != null) {
      update.getWorkspace().getProjects().forEach(ProjectConfigImpl::prePersistAttributes);
    }
    FactoryImpl merged = manager.merge(update);
    manager.flush();
    return merged;
  }

  @Transactional
  protected void doRemove(String id) {
    final EntityManager manager = managerProvider.get();
    final FactoryImpl factory = manager.find(FactoryImpl.class, id);
    if (factory != null) {
      manager.remove(factory);
      manager.flush();
    }
  }

  @Singleton
  public static class RemoveFactoriesBeforeUserRemovedEventSubscriber
      extends CascadeEventSubscriber<BeforeUserRemovedEvent> {
    @Inject private FactoryDao factoryDao;
    @Inject private EventService eventService;

    @PostConstruct
    public void subscribe() {
      eventService.subscribe(this, BeforeUserRemovedEvent.class);
    }

    @PreDestroy
    public void unsubscribe() {
      eventService.unsubscribe(this, BeforeUserRemovedEvent.class);
    }

    @Override
    public void onCascadeEvent(BeforeUserRemovedEvent event) throws ServerException {
      for (FactoryImpl factory :
          iterate(
              (maxItems, skipCount) ->
                  factoryDao.getByUser(event.getUser().getId(), maxItems, skipCount))) {
        factoryDao.remove(factory.getId());
      }
    }
  }
}
