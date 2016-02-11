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

import java.util.List;

/**
 * Defines runtime workspace.
 *
 * @author Eugene Voevodin
 * @author gazarenkov
 */
public interface RuntimeWorkspace extends UsersWorkspace {

    /**
     * Returns active environment name, implementation should guarantee that environment
     * with returned name exists for current runtime workspace
     */
    String getActiveEnvName();

    /**
     * Returns development machine.
     * This machine used for extensions management
     */
    Machine getDevMachine();

    /**
     * Returns non empty list which contains at least one dev machine and other machines related to workspace.
     */
    List<? extends Machine> getMachines();

    /**
     * Returns workspace root folder.
     */
    String getRootFolder();
}
