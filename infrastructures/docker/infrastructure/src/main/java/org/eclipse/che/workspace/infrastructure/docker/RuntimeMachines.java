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
import org.eclipse.che.workspace.infrastructure.docker.DockerMachine.StartingDockerMachine;

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
    return machines != null ? machines : emptyMap();
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
   * Adds a machine into the storage.
   *
   * @param name the name of a machine
   * @param machine machine instance
   * @throws InternalInfrastructureException if runtime start is cancelled which led to removal of
   *     machines from this storage
   */
  public synchronized void addMachine(String name, DockerMachine machine)
      throws InternalInfrastructureException {
    if (machines == null) {
      throw new InternalInfrastructureException("Start of runtime is canceled");
    }
    machines.put(name, machine);
  }

  /**
   * Replaces existing machine with the provided one. Is supposed to be used to replace initial fake
   * state of machine with the real one that returns all the information about machine.
   *
   * @param name name of the machine to replace
   * @param machine replacement
   * @throws InternalInfrastructureException if runtime start is cancelled which led to removal of
   *     machines from this storage
   * @throws InternalInfrastructureException if machine is not present in this storage which means
   *     that correct runtime start flow is broken
   */
  public synchronized void replaceMachine(String name, DockerMachine machine)
      throws InternalInfrastructureException {
    if (machines == null) {
      throw new InternalInfrastructureException("Start of runtime is canceled");
    }
    DockerMachine existingMachine = machines.get(name);
    if (existingMachine == null || !(existingMachine instanceof StartingDockerMachine)) {
      throw new InternalInfrastructureException("Machine is not in a STARTING state");
    }
    machines.replace(name, machine);
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
