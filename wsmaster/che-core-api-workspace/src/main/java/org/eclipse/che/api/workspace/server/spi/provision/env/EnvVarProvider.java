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
