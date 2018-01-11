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
package org.eclipse.che.workspace.infrastructure.docker;

import static java.util.Collections.emptyMap;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalInfrastructureException;
import org.eclipse.che.commons.annotation.Nullable;

/**
 * Holds machines during runtime lifecycle.
 *
 * @author Alexander Garagatyi
 */
public class RuntimeMachines {

  private Map<String, DockerMachine> machines;

  public RuntimeMachines() {
    this.machines = new HashMap<>();
  }

  /** Returns machines map. If runtime start is canceled returns empty map. */
  public synchronized Map<String, DockerMachine> getMachines() {
    return machines != null ? new HashMap<>(machines) : emptyMap();
  }

  /**
   * Returns machine of the runtime by machine name. Returns {@code null} if there is no machine
   * with such a name or machines lis was cleaned because of stop of the runtime.
   */
  @Nullable
  public synchronized DockerMachine getMachine(String name) {
    if (machines == null) {
      return null;
    }
    return machines.get(name);
  }

  /**
   * Puts a machine into the storage. Replaces machine if another instance is already present.
   *
   * @param name the name of a machine
   * @param machine machine instance
   * @throws InternalInfrastructureException if runtime start is cancelled which led to removal of
   *     machines from this storage
   */
  public synchronized void putMachine(String name, DockerMachine machine)
      throws InternalInfrastructureException {
    if (machines == null) {
      throw new InternalInfrastructureException("Start of runtime is canceled");
    }
    machines.put(name, machine);
  }

  /**
   * Removes all the machine and returns them. Is supposed to be called on normal/abnormal runtime
   * stop.
   *
   * @throws InfrastructureException if there is no machines anymore. Might happen on concurrent
   *     modification.
   */
  public synchronized Map<String, DockerMachine> removeMachines() throws InfrastructureException {
    if (machines == null) {
      throw new InfrastructureException("Runtime doesn't have machines to remove");
    }
    Map<String, DockerMachine> machines = this.machines;
    // unset to identify error if method called second time
    this.machines = null;
    return machines;
  }
}
