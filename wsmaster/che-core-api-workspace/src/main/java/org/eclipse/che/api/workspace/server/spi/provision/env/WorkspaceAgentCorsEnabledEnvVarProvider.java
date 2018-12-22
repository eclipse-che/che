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
package org.eclipse.che.api.workspace.server.spi.provision.env;

import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.commons.lang.Pair;

/**
 * Add environment variable that enables CORS filter on WS Agent
 *
 * @author Mykhailo Kuznietsov
 */
public class WorkspaceAgentCorsEnabledEnvVarProvider implements EnvVarProvider {

  private String wsAgentCorsEnabled;

  @Inject
  public WorkspaceAgentCorsEnabledEnvVarProvider(
      @Nullable @Named("che.wsagent.cors.enabled") String wsAgentCorsEnabled) {
    this.wsAgentCorsEnabled = wsAgentCorsEnabled;
  }

  @Override
  public Pair<String, String> get(RuntimeIdentity runtimeIdentity) throws InfrastructureException {
    return wsAgentCorsEnabled == null
        ? null
        : Pair.of("CHE_WSAGENT_CORS_ENABLED", wsAgentCorsEnabled);
  }
}
