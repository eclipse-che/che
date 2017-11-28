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
package org.eclipse.che.workspace.infrastructure.docker.provisioner;

import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.docker.DockerEnvironmentProvisioner;
import org.eclipse.che.workspace.infrastructure.docker.DockerRuntimeInfrastructure;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerEnvironment;

/**
 * Modifies environment of workspace with everything needed for some logical part of {@link
 * DockerRuntimeInfrastructure}. It is supposed that some set of {@code ConfigurationProvisioner}s
 * will be used to implement {@link DockerEnvironmentProvisioner}. </br>Although it is identical to
 * {@link DockerEnvironmentProvisioner} it is separated from it not to mix implementations of {@code
 * ConfigurationProvisioner} as parts of an {@code DockerEnvironmentProvisioner} with
 * implementations of {@code DockerEnvironmentProvisioner}.
 *
 * @author Alexander Garagatyi
 */
public interface ConfigurationProvisioner {
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
