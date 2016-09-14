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
package org.eclipse.che.api.environment.server;

import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.core.model.machine.MachineConfig;

/**
 * This class is used for generation container name.
 *
 * @author Alexander Garagatyi
 */
public interface ContainerNameGenerator {

    /**
     * Returns generated name for container.
     *
     * @param workspaceId
     *         unique workspace id, see more (@link WorkspaceConfig#getId)
     * @param machineId
     *         unique machine id, see more {@link Machine#getId()}
     * @param namespace
     *         name of the user who is docker container owner
     * @param machineName
     *         name of the workspace machine, see more {@link MachineConfig#getName()}
     */
    String generateContainerName(String workspaceId,
                                 String machineId,
                                 String namespace,
                                 String machineName);
}
