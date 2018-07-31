/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.docker.provisioner;

import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerEnvironment;

/**
 * Provisioner which adds Docker containers settings related configuration to container. It is
 * supposed that such settings can not rely on environment configuration or {@link RuntimeIdentity}.
 * {@code DockerSettingsProvisioner}s order is not respected, so no provisioner is allowed to be
 * dependent on any other provisioner except it encapsulates such a dependency.
 *
 * @author Alexander Garagatyi
 */
public interface ContainerSystemSettingsProvisioner {
  /**
   * Modifies docker container environment representation with everything needed for infrastructure
   * of workspace.
   *
   * @throws InfrastructureException if any error occurs
   */
  void provision(DockerEnvironment internalEnv) throws InfrastructureException;
}
