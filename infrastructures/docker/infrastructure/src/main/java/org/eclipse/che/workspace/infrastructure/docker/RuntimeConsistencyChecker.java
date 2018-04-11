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
package org.eclipse.che.workspace.infrastructure.docker;

import java.util.Map;
import javax.inject.Singleton;
import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.core.model.workspace.runtime.Machine;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.environment.InternalEnvironment;
import org.eclipse.che.api.workspace.server.spi.environment.InternalMachineConfig;

/** Checks whether runtime is consistent with its configuration. */
@Singleton
class RuntimeConsistencyChecker {
  void check(InternalEnvironment environment, DockerInternalRuntime runtime)
      throws ValidationException, InfrastructureException {
    Map<String, InternalMachineConfig> configs = environment.getMachines();
    Map<String, ? extends Machine> machines = runtime.getMachines();
    if (configs.size() != machines.size()) {
      throw new ValidationException(
          "Runtime has '%d' machines while configuration defines '%d'."
              + "Runtime machines: %s. Configuration machines: %s",
          machines.size(), configs.size(), machines.keySet(), configs.keySet());
    }
    if (!configs.keySet().containsAll(machines.keySet())) {
      throw new ValidationException(
          "Runtime has different set of machines than defined by configuration. "
              + "Runtime machines: %s. Configuration machines: %s",
          machines.keySet(), configs.keySet());
    }
  }
}
