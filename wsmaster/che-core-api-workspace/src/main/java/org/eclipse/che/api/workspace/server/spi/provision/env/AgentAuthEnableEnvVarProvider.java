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

import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.commons.lang.Pair;

/**
 * Provides environment variable that is used for enabling/disabling authentication on agents.
 *
 * @author Sergii Leshchenko
 */
public class AgentAuthEnableEnvVarProvider implements EnvVarProvider {

  public static final String CHE_AUTH_ENABLED_ENV = "CHE_AUTH_ENABLED";

  private final boolean agentsAuthEnabled;

  @Inject
  public AgentAuthEnableEnvVarProvider(
      @Named("che.agents.auth_enabled") boolean agentsAuthEnabled) {
    this.agentsAuthEnabled = agentsAuthEnabled;
  }

  @Override
  public Pair<String, String> get(RuntimeIdentity runtimeIdentity) {
    return Pair.of(CHE_AUTH_ENABLED_ENV, Boolean.toString(agentsAuthEnabled));
  }
}
