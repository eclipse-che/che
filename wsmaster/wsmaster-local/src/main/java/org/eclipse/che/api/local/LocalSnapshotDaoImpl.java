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
package org.eclipse.che.api.local;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static java.lang.String.format;
import static java.util.Collections.singletonMap;
import static java.util.stream.Collectors.toList;

/**
 * In-memory implementation of {@link SnapshotDao}.
 *
 * @author Yevhenii Voevodin
 */
@Singleton
public class LocalSnapshotDaoImpl implements SnapshotDao {

    private final Map<String, SnapshotImpl> snapshots;
    private final LocalStorage              snapshotStorage;

    @Inject
    public LocalSnapshotDaoImpl(LocalStorageFactory storageFactory) throws IOException {
        snapshots = new HashMap<>();
        snapshotStorage = storageFactory.create("snapshots.json", singletonMap(MachineSource.class, new MachineSourceAdapter()));
    }

    @Override
    public synchronized SnapshotImpl getSnapshot(String workspaceId, String envName, String machineName) throws NotFoundException,
                                                                                                                SnapshotException {
        final Optional<SnapshotImpl> snapshotOpt = doGetSnapshot(workspaceId, envName, machineName);
        if (!snapshotOpt.isPresent()) {
            throw new NotFoundException(format("Snapshot with workspace id '%s', environment name '%s', machine name %s doesn't exist",
                                               workspaceId, envName, machineName));
        }
        return snapshotOpt.get();
    }

    @Override
    public synchronized SnapshotImpl getSnapshot(String snapshotId) throws NotFoundException, SnapshotException {
        final SnapshotImpl snapshot = snapshots.get(snapshotId);
        if (snapshot == null) {
            throw new NotFoundException("Snapshot with id '" + snapshotId + "' doesn't exist");
        }
        return snapshot;
    }

    @Override
    public synchronized void saveSnapshot(SnapshotImpl snapshot) throws SnapshotException {
        Objects.requireNonNull(snapshot, "Required non-null snapshot");
        final Optional<SnapshotImpl> opt = doGetSnapshot(snapshot.getWorkspaceId(), snapshot.getEnvName(), snapshot.getMachineName());
        if (opt.isPresent()) {
            snapshots.remove(opt.get().getId());
        }
        snapshots.put(snapshot.getId(), snapshot);
    }

    @Override
    public synchronized List<SnapshotImpl> findSnapshots(String namespace, String workspaceId) throws SnapshotException {
        return snapshots.values()
                        .stream()
                        .filter(snapshot -> snapshot.getNamespace().equals(namespace) && snapshot.getWorkspaceId().equals(workspaceId))
                        .collect(toList());
    }

    @Override
    public synchronized void removeSnapshot(String snapshotId) throws NotFoundException, SnapshotException {
        snapshots.remove(snapshotId);
    }

    @PostConstruct
    public synchronized void loadSnapshots() {
        snapshots.putAll(snapshotStorage.loadMap(new TypeToken<Map<String, SnapshotImpl>>() {}));
    }

    @PreDestroy
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
