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

import org.eclipse.che.api.core.model.machine.Machine;
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
     * Returns development machine.
     *
     * <p>Returned machine used for extensions management.
     * It is guaranteed that configuration of that machine exists
     * in the active environment.
     */
    @Nullable
    Machine getDevMachine();

    /**
     * Returns all the machines which are defined by the active environment.
     */
    List<? extends Machine> getMachines();
}
