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
package org.eclipse.che.api.machine.server.spi;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.machine.server.exception.SnapshotException;
import org.eclipse.che.api.machine.server.model.impl.SnapshotImpl;

import java.util.List;

/**
 * Stores metadata of snapshots
 *
 * @author andrew00x
 * @author Yevhenii Voevodin
 */
public interface SnapshotDao {

    /**
     * Retrieves snapshot metadata by machine related information.
     *
     * @param workspaceId
     *         workspace id
     * @param envName
     *         name of environment
     * @param machineName
     *         name of machine
     * @return snapshot which matches given parameters
     * @throws NotFoundException
     *         when snapshot with such workspaceId, envName and machineName doesn't exist
     * @throws SnapshotException
     *         when any other error occurs
     */
    SnapshotImpl getSnapshot(String workspaceId, String envName, String machineName) throws NotFoundException, SnapshotException;

    /**
     * Retrieve snapshot metadata by id
     *
     * @param snapshotId
     *         id of required snapshot
     * @return {@link SnapshotImpl} with specified id
     * @throws NotFoundException
     *         if snapshot with specified id not found
     * @throws SnapshotException
     *         if other error occurs
     */
    SnapshotImpl getSnapshot(String snapshotId) throws NotFoundException, SnapshotException;

    /**
     * Save snapshot metadata
     *
     * @param snapshot
     *         snapshot metadata to store
     * @throws SnapshotException
     *         if error occurs
     */
    void saveSnapshot(SnapshotImpl snapshot) throws SnapshotException;

    /**
     * Find snapshots by workspace.
     *
     * @param workspaceId
     *         workspace specified in desired snapshot
     * @return list of snapshot that satisfy provided queries, or empty list if no desired snapshots found
     * @throws SnapshotException
     *         if error occurs
     */
    List<SnapshotImpl> findSnapshots(String workspaceId) throws SnapshotException;

    /**
     * Remove snapshot by id
     *
     * @param snapshotId
     *         id of snapshot that should be removed
     * @throws NotFoundException
     *         if snapshot with specified id not found
     * @throws SnapshotException
     *         if other error occur
     */
    void removeSnapshot(String snapshotId) throws NotFoundException, SnapshotException;
}
