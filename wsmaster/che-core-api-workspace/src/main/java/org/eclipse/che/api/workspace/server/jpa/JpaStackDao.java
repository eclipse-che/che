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
package org.eclipse.che.api.workspace.server.jpa;

import com.google.inject.persist.Transactional;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.core.db.jpa.DuplicateKeyException;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.workspace.server.event.StackPersistedEvent;
import org.eclipse.che.api.workspace.server.model.impl.ProjectConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.stack.StackImpl;
import org.eclipse.che.api.workspace.server.spi.StackDao;
import org.eclipse.che.commons.annotation.Nullable;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

/**
 * JPA based implementation of {@link StackDao}.
 *
 * @author Yevhenii Voevodin
 */
@Singleton
public class JpaStackDao implements StackDao {

    @Inject
    private Provider<EntityManager> managerProvider;

    @Inject
    private EventService eventService;

    @Override
    public void create(StackImpl stack) throws ConflictException, ServerException {
        requireNonNull(stack, "Required non-null stack");
        try {
            doCreate(stack);
            eventService.publish(new StackPersistedEvent(stack));
        } catch (DuplicateKeyException x) {
            throw new ConflictException(format("Stack with id '%s' or name '%s' already exists", stack.getId(), stack.getName()));
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
            return stack;
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
    public StackImpl update(StackImpl update) throws NotFoundException, ServerException, ConflictException {
        requireNonNull(update, "Required non-null update");
        try {
            return doUpdate(update);
        } catch (DuplicateKeyException x) {
            throw new ConflictException(format("Stack with name '%s' already exists", update.getName()));
        } catch (RuntimeException x) {
            throw new ServerException(x.getLocalizedMessage(), x);
        }
    }

    @Override
    @Transactional
    public List<StackImpl> searchStacks(@Nullable String user,
                                        @Nullable List<String> tags,
                                        int skipCount,
                                        int maxItems) throws ServerException {
        final TypedQuery<StackImpl> query;
        if (tags == null || tags.isEmpty()) {
            query = managerProvider.get().createNamedQuery("Stack.getAll", StackImpl.class);
        } else {
            query = managerProvider.get()
                                   .createNamedQuery("Stack.getByTags", StackImpl.class)
                                   .setParameter("tags", tags)
                                   .setParameter("tagsSize", tags.size());
        }
        try {
            return query.setMaxResults(maxItems)
                        .setFirstResult(skipCount)
                        .getResultList();
        } catch (RuntimeException x) {
            throw new ServerException(x.getLocalizedMessage(), x);
        }
    }

    @Transactional
    protected void doCreate(StackImpl stack) {
        if (stack.getWorkspaceConfig() != null) {
            stack.getWorkspaceConfig().getProjects().forEach(ProjectConfigImpl::prePersistAttributes);
        }
        managerProvider.get().persist(stack);
    }

    @Transactional
    protected void doRemove(String id) {
        final EntityManager manager = managerProvider.get();
        final StackImpl stack = manager.find(StackImpl.class, id);
        if (stack != null) {
            manager.remove(stack);
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
        return manager.merge(update);
    }
}
