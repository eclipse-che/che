/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.docker.provisioner.snapshot;

import java.util.Collections;
import java.util.List;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalEnvironment;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerEnvironment;
import org.eclipse.che.workspace.infrastructure.docker.provisioner.ConfigurationProvisioner;

/**
 * Adds volumes to containers to exclude them from snapshot and improve snapshot save/restore time.
 *
 * @author Alexander Garagatyi
 */
public class ExcludeFoldersFromSnapshotProvisioner implements ConfigurationProvisioner {
  private static final List<String> SNAPSHOT_EXCLUDED_DIRECTORIES =
      Collections.singletonList("/tmp");

  @Override
  public void provision(
      InternalEnvironment envConfig, DockerEnvironment internalEnv, RuntimeIdentity identity)
      throws InfrastructureException {

    // create volume for each directory to exclude from a snapshot
    internalEnv
        .getContainers()
        .values()
        .forEach(container -> container.getVolumes().addAll(SNAPSHOT_EXCLUDED_DIRECTORIES));
  }
}
