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
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.commons.lang.Pair;

/**
 * CHE_API endpoint var provided. For all currently supported infrastructures, it reuses {@link
 * CheApiInternalEnvVarProvider} to provide the value.
 *
 * @deprecated this class shall soon be removed, as this variable is provided only for backward
 *     compatibility
 * @author Sergii Leshchenko
 * @author Mykhailo Kuznietsov
 */
@Deprecated
public class CheApiEnvVarProvider implements EnvVarProvider {

  /** Che API url */
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
