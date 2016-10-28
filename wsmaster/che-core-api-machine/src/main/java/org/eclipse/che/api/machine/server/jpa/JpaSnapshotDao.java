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
package org.eclipse.che.api.machine.server.jpa;

import com.google.inject.persist.Transactional;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.jdbc.jpa.DuplicateKeyException;
import org.eclipse.che.api.machine.server.exception.SnapshotException;
import org.eclipse.che.api.machine.server.model.impl.SnapshotImpl;
import org.eclipse.che.api.machine.server.spi.SnapshotDao;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.util.Collection;
import java.util.List;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

/**
 * JPA based {@link SnapshotDao} implementation.
 *
 * @author Yevhenii Voevodin
 */
@Singleton
public class JpaSnapshotDao implements SnapshotDao {

    @Inject
    private Provider<EntityManager> managerProvider;

    @Override
    @Transactional
    public SnapshotImpl getSnapshot(String snapshotId) throws NotFoundException, SnapshotException {
        requireNonNull(snapshotId, "Required non-null snapshotId");
        try {
            final SnapshotImpl snapshot = managerProvider.get().find(SnapshotImpl.class, snapshotId);
            if (snapshot == null) {
                throw new NotFoundException(format("Snapshot with id '%s' doesn't exist", snapshotId));
            }
            return snapshot;
        } catch (RuntimeException x) {
            throw new SnapshotException(x.getLocalizedMessage(), x);
        }
    }

    @Override
    @Transactional
    public SnapshotImpl getSnapshot(String workspaceId, String envName, String machineName) throws NotFoundException, SnapshotException {
        requireNonNull(workspaceId, "Required non-null workspace id");
        requireNonNull(envName, "Required non-null environment name");
        requireNonNull(machineName, "Required non-null machine name");
        try {
            return managerProvider.get()
                                  .createNamedQuery("Snapshot.getByMachine", SnapshotImpl.class)
                                  .setParameter("workspaceId", workspaceId)
                                  .setParameter("envName", envName)
                                  .setParameter("machineName", machineName)
                                  .getSingleResult();
        } catch (NoResultException x) {
            throw new NotFoundException(format("Snapshot for machine '%s:%s:%s' doesn't exist",
                                               workspaceId,
                                               envName,
                                               machineName));
        } catch (RuntimeException x) {
            throw new SnapshotException(x.getLocalizedMessage(), x);
        }
    }

    @Override
    @Transactional
    public List<SnapshotImpl> findSnapshots(String workspaceId) throws SnapshotException {
        requireNonNull(workspaceId, "Required non-null workspace id");
        try {
            return managerProvider.get()
                                  .createNamedQuery("Snapshot.findSnapshots", SnapshotImpl.class)
                                  .setParameter("workspaceId", workspaceId)
                                  .getResultList();
        } catch (RuntimeException x) {
            throw new SnapshotException(x.getLocalizedMessage(), x);
        }
    }

    @Override
    public void saveSnapshot(SnapshotImpl snapshot) throws SnapshotException {
        requireNonNull(snapshot, "Required non-null snapshot");
        try {
            doSave(snapshot);
        } catch (DuplicateKeyException x) {
            throw new SnapshotException(format("Snapshot with id '%s' or for machine '%s:%s:%s' already exists",
                                               snapshot.getId(),
                                               snapshot.getWorkspaceId(),
                                               snapshot.getEnvName(),
                                               snapshot.getMachineName()));
        } catch (RuntimeException x) {
            throw new SnapshotException(x.getLocalizedMessage(), x);
        }
    }

    @Override
    public void removeSnapshot(String snapshotId) throws NotFoundException, SnapshotException {
        requireNonNull(snapshotId, "Required non-null snapshot id");
        try {
            doRemove(snapshotId);
        } catch (RuntimeException x) {
            throw new SnapshotException(x.getLocalizedMessage(), x);
        }
    }

    @Override
    public List<SnapshotImpl> replaceSnapshots(String workspaceId,
                                               String envName,
                                               Collection<? extends SnapshotImpl> newSnapshots) throws SnapshotException {
        requireNonNull(workspaceId, "Required non-null workspace id");
        requireNonNull(envName, "Required non-null environment name");
        requireNonNull(newSnapshots, "Required non-null new snapshots");
        try {
            return doReplaceSnapshots(workspaceId, envName, newSnapshots);
        } catch (RuntimeException x) {
            throw new SnapshotException(x.getLocalizedMessage(), x);
        }
    }

    @Transactional
    protected void doSave(SnapshotImpl snapshot) {
        managerProvider.get().persist(snapshot);
    }

    @Transactional
    protected void doRemove(String snapshotId) throws NotFoundException {
        final EntityManager manager = managerProvider.get();
        final SnapshotImpl snapshot = manager.find(SnapshotImpl.class, snapshotId);
        if (snapshot == null) {
            throw new NotFoundException(format("Snapshot with id '%s' doesn't exist", snapshotId));
        }
        manager.remove(snapshot);
    }

    @Transactional
    protected List<SnapshotImpl> doReplaceSnapshots(String workspaceId,
                                                    String envName,
                                                    Collection<? extends SnapshotImpl> newSnapshots) {
        final EntityManager manager = managerProvider.get();
        final List<SnapshotImpl> existing = manager.createNamedQuery("Snapshot.findByWorkspaceAndEnvironment", SnapshotImpl.class)
                                                   .setParameter("workspaceId", workspaceId)
                                                   .setParameter("envName", envName)
                                                   .getResultList();
        existing.forEach(manager::remove);
        manager.flush();
        newSnapshots.forEach(manager::persist);
        return existing;
    }
}
