/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.core.model.workspace;

import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.core.model.machine.MachineStatus;
import org.eclipse.che.commons.annotation.Nullable;

import java.util.List;

/**
 * Defines a contract for workspace runtime.
 *
 * <p>Workspace has runtime when workspace is <b>running</b>
 * (its {@link Workspace#getStatus() status} is one of the
 * {@link WorkspaceStatus#STARTING}, {@link WorkspaceStatus#RUNNING},
 * {@link WorkspaceStatus#STOPPING}).
 *
 * <p>Workspace runtime defines workspace attributes which
 * exist only when workspace is running. All those attributes
 * are strongly related to the runtime environment.
 * Workspace runtime always exists in couple with {@link Workspace} instance.
 *
 * @author Yevhenii Voevodin
 */
public interface WorkspaceRuntime {

    /**
     * Returns an active environment name.
     * The environment with such name must exist
     * in {@link WorkspaceConfig#getEnvironments()}.
     */
    String getActiveEnv();

    /**
     * Returns a workspace root folder.
     * The base folder for the workspace projects.
     */
    @Nullable
    String getRootFolder();

    /**
     * Returns development machine only if its status is either {@link MachineStatus#RUNNING running}
     * or {@link MachineStatus#DESTROYING destroying}, otherwise returns null
     * which means that machine is starting or hasn't been started yet.
     *
     * <p>Returned machine used for extensions management.
     * It is guaranteed that configuration of that machine exists
     * in the active environment.
     *
     * <p>There is a contract between this method and {@link #getMachines()} method,
     * if this method returns null then {@code getMachines()} method returns an empty list,
     * if this method returns dev-machine then {@code getMachines()} method result includes dev-machine.
     */
    @Nullable
    Machine getDevMachine();

    /**
     * Returns all the machines which statuses are either {@link MachineStatus#RUNNING running}
     * or {@link MachineStatus#DESTROYING}.
     *
     * <p>Returned list always contains dev-machine.
     */
    List<? extends Machine> getMachines();
}
