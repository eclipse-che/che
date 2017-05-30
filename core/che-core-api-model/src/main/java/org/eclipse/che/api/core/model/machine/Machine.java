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
package org.eclipse.che.api.core.model.machine;

import org.eclipse.che.commons.annotation.Nullable;

/**
 * Defines runtime machine.
 *
 * @author gazarenkov
 * @author Alexander Garagatyi
 */
public interface Machine {
    /**
     * Returns configuration used to create this machine
     */
    MachineConfig getConfig();

    /**
     * Returns machine identifier. It is unique and mandatory.
     */
    String getId();

    /**
     * Returns ID of workspace this machine belongs to. It is mandatory.
     */
    String getWorkspaceId();

    /**
     * Returns name of environment that started this machine. It is mandatory.
     */
    String getEnvName();

    /**
     * Returns machine owner (users identifier). It is mandatory.
     */
    String getOwner();

    /**
     * Runtime status of the machine
     */
    MachineStatus getStatus();

    /**
     * Runtime information about machine.
     * <p>
     * Is available only when {@link #getStatus()} returns {@link MachineStatus#RUNNING}
     */
    @Nullable
    MachineRuntimeInfo getRuntime();
}
