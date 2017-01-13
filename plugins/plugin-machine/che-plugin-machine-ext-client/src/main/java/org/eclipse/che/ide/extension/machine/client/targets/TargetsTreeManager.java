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
package org.eclipse.che.ide.extension.machine.client.targets;

import org.eclipse.che.ide.api.machine.MachineEntity;

/**
 * Targets tree manager interface.
 *
 * @author Oleksii Orel
 */
public interface TargetsTreeManager {

    /**
     * Reread all targets.
     *
     * @param preselectTargetName
     *            name of preselected target
     */
    void updateTargets(String preselectTargetName);

    /** Returns name existing status. */
    boolean isTargetNameExist(String targetName);

    /**
     * Returns machine by machineName.
     *
     * @param machineName
     * */
    MachineEntity getMachineByName(String machineName);
}
