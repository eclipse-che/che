/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.multiuser.permission.workspace.server.spi.jpa;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import com.google.inject.persist.Transactional;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.workspace.server.event.BeforeStackRemovedEvent;
import org.eclipse.che.api.workspace.server.event.StackPersistedEvent;
import org.eclipse.che.api.workspace.server.model.impl.ProjectConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.stack.StackImpl;
import org.eclipse.che.api.workspace.server.spi.StackDao;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.core.db.jpa.DuplicateKeyException;

/**
 * JPA based implementation of {@link StackDao}.
 *
 * @author Yevhenii Voevodin
 */
@Singleton
public class MultiuserJpaStackDao implements StackDao {

  @Inject private Provider<EntityManager> managerProvider;

  @Inject private EventService eventService;

  private static final String findByPermissionsQuery =
      " SELECT stack FROM StackPermissions perm "
          + "        LEFT JOIN perm.stack stack  "
          + "        WHERE (perm.userId IS NULL OR perm.userId  = :userId) "
          + "        AND 'search' MEMBER OF perm.actions";

  private static final String findByPermissionsAndTagsQuery =
      " SELECT stack FROM StackPermissions perm "
          + "        LEFT JOIN perm.stack stack  "
          + "        LEFT JOIN stack.tags tag    "
          + "        WHERE (perm.userId IS NULL OR perm.userId  = :userId) "
          + "        AND 'search' MEMBER OF perm.actions"
          + "        AND tag IN :tags "
          + "        GROUP BY stack.id HAVING COUNT(tag) = :tagsSize";

  @Override
  public void create(StackImpl stack) throws ConflictException, ServerException {
    requireNonNull(stack, "Required non-null stack");
    try {
      doCreate(stack);
    } catch (DuplicateKeyException x) {
      throw new ConflictException(
          format("Stack with id '%s' or name '%s' already exists", stack.getId(), stack.getName()));
    } catch (RuntimeException x) {
      throw new ServerException(x.getLocalizedMessage(), x);
    }
  }

  @Override
  @Transactional
  public StackImpl getById(String id) throws NotFoundException, ServerException {
    requireNonNull(id, "Required non-null id");
    try {
      final StackImpl stack = managerProvider.get().find(StackImpl.class, id);
      if (stack == null) {
        throw new NotFoundException(format("Stack with id '%s' doesn't exist", id));
      }
      return new StackImpl(stack);
    } catch (RuntimeException x) {
      throw new ServerException(x.getLocalizedMessage(), x);
    }
  }

  @Override
  public void remove(String id) throws ServerException {
    requireNonNull(id, "Required non-null id");
    try {
      doRemove(id);
    } catch (RuntimeException x) {
      throw new ServerException(x.getLocalizedMessage(), x);
    }
  }

  @Override
  public StackImpl update(StackImpl update)
      throws NotFoundException, ServerException, ConflictException {
    requireNonNull(update, "Required non-null update");
    try {
      return new StackImpl(doUpdate(update));
    } catch (DuplicateKeyException x) {
      throw new ConflictException(format("Stack with name '%s' already exists", update.getName()));
    } catch (RuntimeException x) {
      throw new ServerException(x.getLocalizedMessage(), x);
    }
  }

  @Override
  @Transactional
  public List<StackImpl> searchStacks(
      @Nullable String userId, @Nullable List<String> tags, int skipCount, int maxItems)
      throws ServerException {
    final TypedQuery<StackImpl> query;
    if (tags == null || tags.isEmpty()) {
      query = managerProvider.get().createQuery(findByPermissionsQuery, StackImpl.class);
    } else {
      query =
          managerProvider
              .get()
              .createQuery(findByPermissionsAndTagsQuery, StackImpl.class)
              .setParameter("tags", tags)
              .setParameter("tagsSize", tags.size());
    }
    try {
      return query
          .setParameter("userId", userId)
          .setMaxResults(maxItems)
          .setFirstResult(skipCount)
          .getResultList()
          .stream()
          .map(StackImpl::new)
          .collect(Collectors.toList());
    } catch (RuntimeException x) {
      throw new ServerException(x.getLocalizedMessage(), x);
    }
  }

  @Transactional(rollbackOn = {RuntimeException.class, ApiException.class})
  protected void doCreate(StackImpl stack) throws ConflictException, ServerException {
    if (stack.getWorkspaceConfig() != null) {
      stack.getWorkspaceConfig().getProjects().forEach(ProjectConfigImpl::prePersistAttributes);
    }
    EntityManager manager = managerProvider.get();
    manager.persist(stack);
    manager.flush();
    eventService.publish(new StackPersistedEvent(stack)).propagateException();
  }

  @Transactional(rollbackOn = {RuntimeException.class, ServerException.class})
  protected void doRemove(String id) throws ServerException {
    final EntityManager manager = managerProvider.get();
    final StackImpl stack = manager.find(StackImpl.class, id);
    if (stack != null) {
      eventService.publish(new BeforeStackRemovedEvent(new StackImpl(stack))).propagateException();
      manager.remove(stack);
      manager.flush();
    }
  }

  @Transactional
  protected StackImpl doUpdate(StackImpl update) throws NotFoundException {
    final EntityManager manager = managerProvider.get();
    if (manager.find(StackImpl.class, update.getId()) == null) {
      throw new NotFoundException(format("Workspace with id '%s' doesn't exist", update.getId()));
    }
    if (update.getWorkspaceConfig() != null) {
      update.getWorkspaceConfig().getProjects().forEach(ProjectConfigImpl::prePersistAttributes);
    }
    StackImpl merged = manager.merge(update);
    manager.flush();
    return merged;
  }
}
