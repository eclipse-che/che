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
import org.eclipse.che.commons.lang.Pair;

/**
 * Add environment variable that defines allowed origins for {@link CheCorsFilterConfig} of WS Agent
 *
 * @author Mykhailo Kuznietsov
 */
public class WorkspaceAgentCorsAllowedOriginsEnvVarProvider implements EnvVarProvider {

  private String wsAgentCorsAllowedOrigins;

  @Inject
  public WorkspaceAgentCorsAllowedOriginsEnvVarProvider(
      @Named("che.wsagent.cors.allowed_origins") String cheWsMasterAllowedOrigins) {
    this.wsAgentCorsAllowedOrigins = cheWsMasterAllowedOrigins;
  }

  @Override
  public Pair<String, String> get(RuntimeIdentity runtimeIdentity) throws InfrastructureException {
    return Pair.of("CHE_CORS_ALLOWED__ORIGINS", wsAgentCorsAllowedOrigins);
  }
}
