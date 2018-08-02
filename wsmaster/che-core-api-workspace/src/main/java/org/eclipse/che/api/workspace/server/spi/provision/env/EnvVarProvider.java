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

import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.commons.lang.Pair;

/**
 * Provides an environment variable which is needed for servers used by Che for providing IDE
 * features.
 *
 * @author Sergii Leshchenko
 */
public interface EnvVarProvider {
  /**
   * Returns environment variable which should be injected into machine environment.
   *
   * @param runtimeIdentity which may be needed to evaluate environment variable value
   */
  Pair<String, String> get(RuntimeIdentity runtimeIdentity) throws InfrastructureException;
}
