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
package org.eclipse.che.api.workspace.server.jpa;

import com.google.inject.persist.Transactional;

import org.eclipse.che.account.event.BeforeAccountRemovedEvent;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.workspace.server.WorkspaceManager;
import org.eclipse.che.api.workspace.server.event.BeforeWorkspaceRemovedEvent;
import org.eclipse.che.api.workspace.server.event.WorkspaceRemovedEvent;
import org.eclipse.che.api.workspace.server.model.impl.ProjectConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.api.workspace.server.spi.WorkspaceDao;
import org.eclipse.che.core.db.cascade.CascadeEventSubscriber;
import org.eclipse.che.core.db.jpa.DuplicateKeyException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

/**
 * JPA based implementation of {@link WorkspaceDao}.
 *
 * @author Yevhenii Voevodin
 */
@Singleton
public class JpaWorkspaceDao implements WorkspaceDao {

    @Inject
    private EventService            eventService;
    @Inject
    private Provider<EntityManager> managerProvider;

    @Override
    public WorkspaceImpl create(WorkspaceImpl workspace) throws ConflictException, ServerException {
        requireNonNull(workspace, "Required non-null workspace");
        try {
            doCreate(workspace);
        } catch (DuplicateKeyException dkEx) {
            throw new ConflictException(format("Workspace with id '%s' or name '%s' in namespace '%s' already exists",
                                               workspace.getId(),
                                               workspace.getConfig().getName(),
                                               workspace.getNamespace()));
        } catch (RuntimeException x) {
            throw new ServerException(x.getMessage(), x);
        }
        return new WorkspaceImpl(workspace);
    }

    @Override
    public WorkspaceImpl update(WorkspaceImpl update) throws NotFoundException, ConflictException, ServerException {
        requireNonNull(update, "Required non-null update");
        try {
            return new WorkspaceImpl(doUpdate(update));
        } catch (DuplicateKeyException dkEx) {
            throw new ConflictException(format("Workspace with name '%s' in namespace '%s' already exists",
                                               update.getConfig().getName(),
                                               update.getNamespace()));
        } catch (RuntimeException x) {
            throw new ServerException(x.getMessage(), x);
        }
    }

    @Override
    public void remove(String id) throws ServerException {
        requireNonNull(id, "Required non-null id");
        try {
            Optional<WorkspaceImpl> workspaceOpt = doRemove(id);
            workspaceOpt.ifPresent(workspace -> eventService.publish(new WorkspaceRemovedEvent(workspace)));
        } catch (RuntimeException x) {
            throw new ServerException(x.getLocalizedMessage(), x);
        }
    }

    @Override
    @Transactional
    public WorkspaceImpl get(String id) throws NotFoundException, ServerException {
        requireNonNull(id, "Required non-null id");
        try {
            final WorkspaceImpl workspace = managerProvider.get().find(WorkspaceImpl.class, id);
            if (workspace == null) {
                throw new NotFoundException(format("Workspace with id '%s' doesn't exist", id));
            }
            return new WorkspaceImpl(workspace);
        } catch (RuntimeException x) {
            throw new ServerException(x.getLocalizedMessage(), x);
        }
    }

    @Override
    @Transactional
    public WorkspaceImpl get(String name, String namespace) throws NotFoundException, ServerException {
        requireNonNull(name, "Required non-null name");
        requireNonNull(namespace, "Required non-null namespace");
        try {
            return new WorkspaceImpl(managerProvider.get()
                                                    .createNamedQuery("Workspace.getByName", WorkspaceImpl.class)
                                                    .setParameter("namespace", namespace)
                                                    .setParameter("name", name)
                                                    .getSingleResult());
        } catch (NoResultException noResEx) {
            throw new NotFoundException(format("Workspace with name '%s' in namespace '%s' doesn't exist",
                                               name,
                                               namespace));
        } catch (RuntimeException x) {
            throw new ServerException(x.getLocalizedMessage(), x);
        }
    }

    @Override
    @Transactional
    public List<WorkspaceImpl> getByNamespace(String namespace) throws ServerException {
        requireNonNull(namespace, "Required non-null namespace");
        try {
            return managerProvider.get()
                                  .createNamedQuery("Workspace.getByNamespace", WorkspaceImpl.class)
                                  .setParameter("namespace", namespace)
                                  .getResultList()
                                  .stream()
                                  .map(WorkspaceImpl::new)
                                  .collect(Collectors.toList());
        } catch (RuntimeException x) {
            throw new ServerException(x.getLocalizedMessage(), x);
        }
    }

    @Override
    @Transactional
    public List<WorkspaceImpl> getWorkspaces(String userId) throws ServerException {
        try {
            return managerProvider.get()
                                  .createNamedQuery("Workspace.getAll", WorkspaceImpl.class)
                                  .getResultList()
                                  .stream()
                                  .map(WorkspaceImpl::new)
                                  .collect(Collectors.toList());
        } catch (RuntimeException x) {
            throw new ServerException(x.getLocalizedMessage(), x);
        }
    }

    @Override
    @Transactional
    public List<WorkspaceImpl> getWorkspaces(boolean isTemporary, int skipCount, int maxItems) throws ServerException {
        checkArgument(maxItems >= 0, "The number of items to return can't be negative.");
        checkArgument(skipCount >= 0, "The number of items to skip can't be negative or greater than " + Integer.MAX_VALUE);
        try {
            return managerProvider.get()
                                  .createNamedQuery("Workspace.getByTemporary", WorkspaceImpl.class)
                                  .setParameter("temporary", isTemporary)
                                  .setMaxResults(maxItems)
                                  .setFirstResult(skipCount)
                                  .getResultList()
                                  .stream()
                                  .map(WorkspaceImpl::new)
                                  .collect(toList());
        } catch (RuntimeException x) {
            throw new ServerException(x.getLocalizedMessage(), x);
        }
    }

    @Transactional
    protected void doCreate(WorkspaceImpl workspace) {
        if (workspace.getConfig() != null) {
            workspace.getConfig().getProjects().forEach(ProjectConfigImpl::prePersistAttributes);
        }
        EntityManager manager = managerProvider.get();
        manager.persist(workspace);
        manager.flush();
    }

    @Transactional(rollbackOn = {RuntimeException.class, ServerException.class})
    protected Optional<WorkspaceImpl> doRemove(String id) throws ServerException {
        final WorkspaceImpl workspace = managerProvider.get().find(WorkspaceImpl.class, id);
        if (workspace == null) {
            return Optional.empty();
        }
        final EntityManager manager = managerProvider.get();
        eventService.publish(new BeforeWorkspaceRemovedEvent(new WorkspaceImpl(workspace))).propagateException();
        manager.remove(workspace);
        manager.flush();
        return Optional.of(workspace);
    }

    @Transactional
    protected WorkspaceImpl doUpdate(WorkspaceImpl update) throws NotFoundException {
        EntityManager manager = managerProvider.get();
        if (manager.find(WorkspaceImpl.class, update.getId()) == null) {
            throw new NotFoundException(format("Workspace with id '%s' doesn't exist", update.getId()));
        }
        if (update.getConfig() != null) {
            update.getConfig().getProjects().forEach(ProjectConfigImpl::prePersistAttributes);
        }
        WorkspaceImpl merged = manager.merge(update);
        manager.flush();
        return merged;
    }

    @Singleton
    public static class RemoveWorkspaceBeforeAccountRemovedEventSubscriber
            extends CascadeEventSubscriber<BeforeAccountRemovedEvent> {

        @Inject
        private EventService     eventService;
        @Inject
        private WorkspaceManager workspaceManager;

        @PostConstruct
        public void subscribe() {
            eventService.subscribe(this, BeforeAccountRemovedEvent.class);
        }

        @PreDestroy
        public void unsubscribe() {
            eventService.unsubscribe(this, BeforeAccountRemovedEvent.class);
        }

        @Override
        public void onCascadeEvent(BeforeAccountRemovedEvent event) throws Exception {
            for (WorkspaceImpl workspace : workspaceManager.getByNamespace(event.getAccount().getName(), false)) {
                workspaceManager.removeWorkspace(workspace.getId());
            }
        }
    }

    @Singleton
    public static class RemoveSnapshotsBeforeWorkspaceRemovedEventSubscriber
            extends CascadeEventSubscriber<BeforeWorkspaceRemovedEvent> {
        @Inject
        private EventService     eventService;
        @Inject
        private WorkspaceManager workspaceManager;

        @PostConstruct
        public void subscribe() {
            eventService.subscribe(this, BeforeWorkspaceRemovedEvent.class);
        }

        @PreDestroy
        public void unsubscribe() {
            eventService.unsubscribe(this, BeforeWorkspaceRemovedEvent.class);
        }

        @Override
        public void onCascadeEvent(BeforeWorkspaceRemovedEvent event) throws Exception {
            workspaceManager.removeSnapshots(event.getWorkspace().getId());
        }
    }
}
