/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.core.model.workspace;

import org.eclipse.che.api.core.model.machine.MachineStatus;

/**
 * Defines the contract between workspace and its dev-machine.
 *
 * <p>Workspace status itself shows the state of the workspace dev-machine.
 * If workspace environment which is running contains not only the dev-machine, those machines
 * states won't affect workspace status at all.
 *
 * <p>{@link MachineStatus} is responsible for states of machines different from the dev-machine.
 *
 * <p>Workspace is rather part of the {@link Workspace} than {@link WorkspaceRuntime} or {@link WorkspaceConfig},
 * as it shows the state of <b>certain</b> user's workspace and exists <b>earlier</b> than runtime workspace instance
 * e.g. UsersWorkspace may be considered as 'STARTING' before it becomes runtime('RUNNING').
 *
 * @author Alexander Garagatyi
 * @author Yevhenii Voevodin
 */
public enum WorkspaceStatus {

    /**
     * Workspace considered as starting if and only if its dev-machine is booting(creating).
     *
     * <p>Workspace becomes starting only if it was {@link #STOPPED}.
     * The status map:
     * <pre>
     *  STOPPED -> <b>STARTING</b> -> RUNNING (normal behaviour)
     *  STOPPED -> <b>STARTING</b> -> STOPPED (failed to start)
     * </pre>
     *
     * @see MachineStatus#CREATING
     */
    STARTING,

    /**
     * Workspace considered as running if and only if its dev-machine was successfully started and it is running.
     *
     * <p>Workspace becomes running after it was {@link #STARTING}.
     * The status map:
     * <pre>
     *  STARTING -> <b>RUNNING</b> -> STOPPING (normal behaviour)
     *  STARTING -> <b>RUNNING</b> -> STOPPED (dev-machine was interrupted)
     * </pre>
     *
     * @see MachineStatus#RUNNING
     */
    RUNNING,

    /**
     * Workspace considered as stopping if and only if its dev-machine is shutting down(destroying).
     *
     * <p>Workspace is in stopping status only if it was in {@link #RUNNING} status before.
     * The status map:
     * <pre>
     *  RUNNING -> <b>STOPPING</b> -> STOPPED (normal behaviour)/(error while stopping)
     * </pre>
     *
     * @see MachineStatus#DESTROYING
     */
    STOPPING,

    /**
     * Workspace considered as stopped when:
     * <ul>
     * <li>Dev-machine was successfully destroyed(stopped)</li>
     * <li>Error occurred while dev-machine was stopping</li>
     * <li>Dev-machine failed to start</li>
     * <li>Running dev-machine was interrupted by internal problem(e.g. OOM)</li>
     * <li>Workspace hasn't been started yet(e.g stopped is the status of the user's workspace instance without its runtime)</li>
     * </ul>
     *
     * <p>The status map:
     * <pre>
     *  STOPPING -> <b>STOPPED</b> (normal behaviour)/(error while stopping)
     *  STARTING -> <b>STOPPED</b> (failed to start)
     *  RUNNING  -> <b>STOPPED</b> (dev-machine was interrupted)
     * </pre>
     */
    STOPPED
}
