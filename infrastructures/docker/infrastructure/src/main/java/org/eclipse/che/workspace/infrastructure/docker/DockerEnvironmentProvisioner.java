/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.docker;

import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerEnvironment;

/**
 * Modifies environment of workspace with everything needed for runtime infrastructure to run.
 *
 * @author Alexander Garagatyi
 */
public interface DockerEnvironmentProvisioner {
  /**
   * Modifies environment config and internal environment representation with everything needed for
   * infrastructure of workspace.
   *
   * @param internalEnv internal environment representation
   * @throws InfrastructureException if any error occurs
   */
  void provision(DockerEnvironment internalEnv, RuntimeIdentity identity)
      throws InfrastructureException;
}
