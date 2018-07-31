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
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.commons.lang.Pair;

/** @author Mykhailo Kuznietsov */
public interface CheApiInternalEnvVarProvider extends EnvVarProvider {

  /** Env variable for machine that contains url of Che API */
  String CHE_API_INTERNAL_VARIABLE = "CHE_API_INTERNAL";

  /**
   * Returns Che API environment variable which should be injected into machines. Internal API URL
   * is meant to be used from the inside of other machines.
   *
   * @param runtimeIdentity which may be needed to evaluate environment variable value
   */
  @Override
  Pair<String, String> get(RuntimeIdentity runtimeIdentity) throws InfrastructureException;
}
