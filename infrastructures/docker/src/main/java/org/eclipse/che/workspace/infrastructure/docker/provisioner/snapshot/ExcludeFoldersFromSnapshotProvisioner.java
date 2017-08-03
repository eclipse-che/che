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
package org.eclipse.che.workspace.infrastructure.docker.provisioner.snapshot;

import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.docker.provisioner.ConfigurationProvisioner;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerEnvironment;

import java.util.Collections;
import java.util.List;

/**
 * Adds volumes to containers to exclude them from snapshot and improve snapshot save/restore time.
 *
 * @author Alexander Garagatyi
 */
public class ExcludeFoldersFromSnapshotProvisioner implements ConfigurationProvisioner {
    private static final List<String> SNAPSHOT_EXCLUDED_DIRECTORIES = Collections.singletonList("/tmp");

    @Override
    public void provision(EnvironmentImpl envConfig, DockerEnvironment internalEnv, RuntimeIdentity identity)
            throws InfrastructureException {

        // create volume for each directory to exclude from a snapshot
        internalEnv.getContainers()
                   .values()
                   .forEach(container -> container.getVolumes().addAll(SNAPSHOT_EXCLUDED_DIRECTORIES));
    }
}
