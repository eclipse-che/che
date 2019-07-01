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
 * Add environment variable that defines support for requests with credentials for {@link
 * CheCorsFilterConfig} of WS Agent
 *
 * @author Mykhailo Kuznietsov
 */
public class WorkspaceAgentCorsAllowCredentialsEnvVarProvider implements LegacyEnvVarProvider {

  private String wsAgentCorsAllowCredentials;

  @Inject
  public WorkspaceAgentCorsAllowCredentialsEnvVarProvider(
      @Nullable @Named("che.wsagent.cors.allow_credentials") String wsAgentCorsAllowCredentials) {
    this.wsAgentCorsAllowCredentials = wsAgentCorsAllowCredentials;
  }

  @Override
  public Pair<String, String> get(RuntimeIdentity runtimeIdentity) throws InfrastructureException {
    return wsAgentCorsAllowCredentials == null
        ? null
        : Pair.of("CHE_WSAGENT_CORS_ALLOW__CREDENTIALS", wsAgentCorsAllowCredentials);
  }
}
