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
package org.eclipse.che.api.local;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.reflect.TypeToken;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.model.machine.MachineSource;
import org.eclipse.che.api.local.storage.LocalStorage;
import org.eclipse.che.api.local.storage.LocalStorageFactory;
import org.eclipse.che.api.machine.server.spi.SnapshotDao;
import org.eclipse.che.api.machine.server.exception.SnapshotException;
import org.eclipse.che.api.machine.server.model.impl.SnapshotImpl;
import org.eclipse.che.api.machine.server.model.impl.adapter.MachineSourceAdapter;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.lang.String.format;
import static java.util.Collections.singletonMap;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

/**
 * In-memory implementation of {@link SnapshotDao}.
 *
 * <p>The implementation is thread-safe guarded by this instance.
 * Clients may use instance locking to perform extra, thread-safe operation.
 *
 * @author Yevhenii Voevodin
 */
@Singleton
public class LocalSnapshotDaoImpl implements SnapshotDao {

    public static final String FILENAME = "snapshots.json";

    @VisibleForTesting
    final Map<String, SnapshotImpl> snapshots;

    private final LocalStorage snapshotStorage;

    @Inject
    public LocalSnapshotDaoImpl(LocalStorageFactory storageFactory) throws IOException {
        snapshots = new HashMap<>();
        snapshotStorage = storageFactory.create("snapshots.json", singletonMap(MachineSource.class, new MachineSourceAdapter()));
    }

    @Override
    public synchronized SnapshotImpl getSnapshot(String workspaceId, String envName, String machineName) throws NotFoundException,
                                                                                                                SnapshotException {
        requireNonNull(workspaceId, "Required non-null workspace id");
        requireNonNull(envName, "Required non-null environment name");
        requireNonNull(machineName, "Required non-null machine name");
        final Optional<SnapshotImpl> snapshotOpt = doGetSnapshot(workspaceId, envName, machineName);
        if (!snapshotOpt.isPresent()) {
            throw new NotFoundException(format("Snapshot with workspace id '%s', environment name '%s', machine name %s doesn't exist",
                                               workspaceId, envName, machineName));
        }
        return snapshotOpt.get();
    }

    @Override
    public synchronized SnapshotImpl getSnapshot(String snapshotId) throws NotFoundException, SnapshotException {
        requireNonNull(snapshotId, "Required non-null snapshot id");
        final SnapshotImpl snapshot = snapshots.get(snapshotId);
        if (snapshot == null) {
            throw new NotFoundException("Snapshot with id '" + snapshotId + "' doesn't exist");
        }
        return snapshot;
    }

    @Override
    public synchronized void saveSnapshot(SnapshotImpl snapshot) throws SnapshotException {
        requireNonNull(snapshot, "Required non-null snapshot");
        if (snapshots.containsKey(snapshot.getWorkspaceId())) {
            throw new SnapshotException(format("Snapshot with id '%s' already exists", snapshot.getId()));
        }
        final Optional<SnapshotImpl> opt = doGetSnapshot(snapshot.getWorkspaceId(), snapshot.getEnvName(), snapshot.getMachineName());
        if (opt.isPresent()) {
            throw new SnapshotException(format("Snapshot for machine '%s:%s:%s' already exists",
                                               snapshot.getWorkspaceId(),
                                               snapshot.getEnvName(),
                                               snapshot.getMachineName()));
        }
        snapshots.put(snapshot.getId(), snapshot);
    }

    @Override
    public synchronized List<SnapshotImpl> findSnapshots(String workspaceId) throws SnapshotException {
        requireNonNull(workspaceId, "Required non-null workspace id");
        return snapshots.values()
                        .stream()
                        .filter(snapshot -> snapshot.getWorkspaceId().equals(workspaceId))
                        .collect(toList());
    }

    @Override
    public synchronized void removeSnapshot(String snapshotId) throws NotFoundException, SnapshotException {
        requireNonNull(snapshotId, "Required non-null snapshot id");
        if (!snapshots.containsKey(snapshotId)) {
            throw new NotFoundException(format("Snapshot with id '%s' doesn't exist", snapshotId));
        }
        snapshots.remove(snapshotId);
    }

    @Override
    public List<SnapshotImpl> replaceSnapshots(String workspaceId, String envName, Collection<? extends SnapshotImpl> newSnapshots)
            throws SnapshotException {
        throw new RuntimeException("Not implemented");
    }

    @PostConstruct
    public synchronized void loadSnapshots() {
        snapshots.putAll(snapshotStorage.loadMap(new TypeToken<Map<String, SnapshotImpl>>() {}));
    }

    public synchronized void saveSnapshots() throws IOException {
        snapshotStorage.store(snapshots);
    }

    private Optional<SnapshotImpl> doGetSnapshot(String workspaceId, String envName, String machineName) {
        return snapshots.values()
                        .stream()
                        .filter(snapshot -> snapshot.getWorkspaceId().equals(workspaceId)
                                            && snapshot.getEnvName().equals(envName)
                                            && snapshot.getMachineName().equals(machineName))
                        .findFirst();
    }
}
