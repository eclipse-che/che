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
package org.eclipse.che.api.workspace.server.spi.provision.env;

import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.commons.lang.Pair;

/** @author Sergii Leshchenko */
public class WorkspaceIdEnvVarProvider implements EnvVarProvider {
  /**
   * Environment variable that will be setup in machine will contain ID of a workspace for which
   * this machine has been created
   */
  public static final String WORKSPACE_ID_ENV_VAR = "CHE_WORKSPACE_ID";

  @Override
  public Pair<String, String> get(RuntimeIdentity runtimeIdentity) {
    return Pair.of(WORKSPACE_ID_ENV_VAR, runtimeIdentity.getWorkspaceId());
  }
}
