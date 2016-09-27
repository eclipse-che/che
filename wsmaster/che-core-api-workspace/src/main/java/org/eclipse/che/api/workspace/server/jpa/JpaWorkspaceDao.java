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

import org.eclipse.che.account.event.BeforeAccountRemovedEvent;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.jdbc.jpa.DuplicateKeyException;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.machine.server.model.impl.SnapshotImpl;
import org.eclipse.che.api.machine.server.spi.SnapshotDao;
import org.eclipse.che.api.workspace.server.event.BeforeWorkspaceRemovedEvent;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.api.workspace.server.spi.WorkspaceDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.util.List;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

/**
 * JPA based implementation of {@link WorkspaceDao}.
 *
 * @author Yevhenii Voevodin
 */
@Singleton
public class JpaWorkspaceDao implements WorkspaceDao {

    private static final Logger LOG = LoggerFactory.getLogger(JpaWorkspaceDao.class);

    @Inject
    private Provider<EntityManager> manager;

    @Override
    public WorkspaceImpl create(WorkspaceImpl workspace) throws ConflictException, ServerException {
        requireNonNull(workspace, "Required non-null workspace");
        try {
            doCreate(workspace);
        } catch (DuplicateKeyException dkEx) {
            throw new ConflictException(format("Workspace with id '%s' or name '%s' in namespace '%s' already exists",
                                               workspace.getId(),
                                               workspace.getName(),
                                               workspace.getNamespace()));
        } catch (RuntimeException x) {
            throw new ServerException(x.getMessage(), x);
        }
        return workspace;
    }

    @Override
    public WorkspaceImpl update(WorkspaceImpl update) throws NotFoundException, ConflictException, ServerException {
        requireNonNull(update, "Required non-null update");
        try {
            return doUpdate(update);
        } catch (DuplicateKeyException dkEx) {
            throw new ConflictException(format("Workspace with name '%s' in namespace '%s' already exists",
                                               update.getName(),
                                               update.getNamespace()));
        } catch (RuntimeException x) {
            throw new ServerException(x.getMessage(), x);
        }
    }

    @Override
    public void remove(String id) throws ConflictException, ServerException {
        requireNonNull(id, "Required non-null id");
        try {
            doRemove(id);
        } catch (RuntimeException x) {
            throw new ServerException(x.getLocalizedMessage(), x);
        }
    }

    @Override
    @Transactional
    public WorkspaceImpl get(String id) throws NotFoundException, ServerException {
        requireNonNull(id, "Required non-null id");
        try {
            final WorkspaceImpl workspace = manager.get().find(WorkspaceImpl.class, id);
            if (workspace == null) {
                throw new NotFoundException(format("Workspace with id '%s' doesn't exist", id));
            }
            return workspace;
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
            return manager.get()
                          .createNamedQuery("Workspace.getByName", WorkspaceImpl.class)
                          .setParameter("namespace", namespace)
                          .setParameter("name", name)
                          .getSingleResult();
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
            return manager.get()
                          .createNamedQuery("Workspace.getByNamespace", WorkspaceImpl.class)
                          .setParameter("namespace", namespace)
                          .getResultList();
        } catch (RuntimeException x) {
            throw new ServerException(x.getLocalizedMessage(), x);
        }
    }

    @Override
    @Transactional
    public List<WorkspaceImpl> getWorkspaces(String userId) throws ServerException {
        // TODO respect userId when workers become a part of che
        try {
            return manager.get().createNamedQuery("Workspace.getAll", WorkspaceImpl.class).getResultList();
        } catch (RuntimeException x) {
            throw new ServerException(x.getLocalizedMessage(), x);
        }
    }

    @Transactional
    protected void doCreate(WorkspaceImpl workspace) {
        manager.get().persist(workspace);
    }

    @Transactional
    protected void doRemove(String id) {
        final WorkspaceImpl workspace = manager.get().find(WorkspaceImpl.class, id);
        if (workspace != null) {
            manager.get().remove(workspace);
        }
    }

    @Transactional
    protected WorkspaceImpl doUpdate(WorkspaceImpl update) throws NotFoundException {
        if (manager.get().find(WorkspaceImpl.class, update.getId()) == null) {
            throw new NotFoundException(format("Workspace with id '%s' doesn't exist", update.getId()));
        }
        return manager.get().merge(update);
    }

    @Singleton
    public static class RemoveWorkspaceBeforeAccountRemovedEventSubscriber implements EventSubscriber<BeforeAccountRemovedEvent> {
        @Inject
        private EventService eventService;
        @Inject
        private WorkspaceDao workspaceDao;

        @PostConstruct
        public void subscribe() {
            eventService.subscribe(this);
        }

        @PreDestroy
        public void unsubscribe() {
            eventService.unsubscribe(this);
        }

        @Override
        public void onEvent(BeforeAccountRemovedEvent event) {
            try {
                for (WorkspaceImpl workspace : workspaceDao.getByNamespace(event.getAccount().getName())) {
                    workspaceDao.remove(workspace.getId());
                }
            } catch (Exception x) {
                LOG.error(format("Couldn't remove workspaces before account '%s' is removed", event.getAccount().getId()), x);
            }
        }
    }

    @Singleton
    public static class RemoveSnapshotsBeforeWorkspaceRemovedEventSubscriber implements EventSubscriber<BeforeWorkspaceRemovedEvent> {
        @Inject
        private EventService eventService;
        @Inject
        private SnapshotDao  snapshotDao;

        @PostConstruct
        public void subscribe() {
            eventService.subscribe(this);
        }

        @PreDestroy
        public void unsubscribe() {
            eventService.unsubscribe(this);
        }

        @Override
        public void onEvent(BeforeWorkspaceRemovedEvent event) {
            try {
                for (SnapshotImpl snapshot : snapshotDao.findSnapshots(event.getWorkspace().getId())) {
                    snapshotDao.removeSnapshot(snapshot.getId());
                }
            } catch (Exception x) {
                LOG.error(format("Couldn't remove snapshots before workspace '%s' is removed", event.getWorkspace().getId()), x);
            }
        }
    }
}
