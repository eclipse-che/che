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
package org.eclipse.che.workspace.infrastructure.docker.provisioner.server;

import static org.eclipse.che.workspace.infrastructure.docker.DockerMachine.CHE_WORKSPACE_ID;

import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.commons.lang.Pair;

/**
 * Provides environment variable with workspace ID that may be needed for accessing Che master API
 * from a container.
 *
 * @author Alexander Garagatyi
 */
public class WorkspaceIdEnvVarProvider implements ServerEnvironmentVariableProvider {
  @Override
  public Pair<String, String> get(RuntimeIdentity runtimeIdentity) {
    return Pair.of(CHE_WORKSPACE_ID, runtimeIdentity.getWorkspaceId());
  }
}
