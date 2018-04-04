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
package org.eclipse.che.api.core.model.workspace;

import java.util.List;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.runtime.Machine;

/**
 * Defines a contract for workspace runtime.
 *
 * <p>Workspace has runtime when workspace is <b>running</b> (its {@link Workspace#getStatus()
 * status} is one of the {@link WorkspaceStatus#STARTING}, {@link WorkspaceStatus#RUNNING}, {@link
 * WorkspaceStatus#STOPPING}).
 *
 * <p>Workspace runtime defines workspace attributes which exist only when workspace is running. All
 * those attributes are strongly related to the runtime environment. Workspace runtime always exists
 * in couple with {@link Workspace} instance.
 *
 * @author Yevhenii Voevodin
 */
public interface Runtime {

  /**
   * Returns an active environment name. The environment with such name must exist in {@link
   * WorkspaceConfig#getEnvironments()}.
   */
  String getActiveEnv();

  /**
   * Returns all the machines which statuses are either {MachineStatus#RUNNING running} or
   * {MachineStatus#DESTROYING}.
   *
   * <p>Returned list always contains dev-machine.
   */
  Map<String, ? extends Machine> getMachines();

  String getOwner();

  /**
   * Returns the list of the warnings indicating that the runtime violates some non-critical
   * constraints or default configuration values are used to boot it.
   */
  List<? extends Warning> getWarnings();
}
