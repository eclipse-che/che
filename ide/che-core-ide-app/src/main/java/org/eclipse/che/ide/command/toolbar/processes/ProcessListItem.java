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
import org.eclipse.che.api.machine.shared.dto.execagent.GetProcessesResponseDto;
import org.eclipse.che.ide.ui.dropdown.ListItem;

/**
 *
 */
public class ProcessListItem implements ListItem {

    private final GetProcessesResponseDto process;
    private final Machine                 machine;

    public ProcessListItem(GetProcessesResponseDto process, Machine machine) {
        this.process = process;
        this.machine = machine;
    }

    public GetProcessesResponseDto getProcess() {
        return process;
    }

    public Machine getMachine() {
        return machine;
    }
}
