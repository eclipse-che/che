/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.workspace.server.spi.provision.env;

import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.commons.lang.Pair;

import javax.inject.Inject;

/**
 * @author Sergii Leshchenko
 */
public class CheApiEnvVarProvider implements EnvVarProvider {

  /** Env variable for machine that contains url of Che API */
  public static final String CHE_API_VARIABLE = "CHE_API";

  private final CheApiInternalEnvVarProvider cheApiInternalEnvVarProvider;

  @Inject
  public CheApiEnvVarProvider(CheApiInternalEnvVarProvider cheApiInternalEnvVarProvider) {
    this.cheApiInternalEnvVarProvider = cheApiInternalEnvVarProvider;
  }

  /**
   * Returns Che API environment variable which should be injected into machines.
   *
   * @param runtimeIdentity which may be needed to evaluate environment variable value
   */
  @Override
  public Pair<String, String> get(RuntimeIdentity runtimeIdentity) throws InfrastructureException {
    return Pair.of(CHE_API_VARIABLE, cheApiInternalEnvVarProvider.get(runtimeIdentity).second);
  }
}
