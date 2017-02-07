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
package org.eclipse.che.ide.command.toolbar.processes;

import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.ide.ui.dropdown.DropDownListItem;

/**
 *
 */
class RunningProcess extends AbstractProcess implements DropDownListItem {

    private final int pid;

    public RunningProcess(String commandName, String commandLine, int pid, Machine machine) {
        super(commandName, commandLine, machine);
        this.pid = pid;
    }

    public int getNativePid() {
        return pid;
    }
}
