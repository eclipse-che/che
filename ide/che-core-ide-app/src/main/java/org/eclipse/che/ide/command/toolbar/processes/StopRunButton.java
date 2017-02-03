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

import com.google.gwt.user.client.ui.Button;

import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.machine.shared.dto.execagent.GetProcessesResponseDto;

import static com.google.gwt.dom.client.Style.Float.RIGHT;

/**
 * Button for stopping or rerunning process.
 */
public class StopRunButton extends Button {

    private final GetProcessesResponseDto process;
    private final Machine                 machine;

    public StopRunButton(GetProcessesResponseDto process, Machine machine) {
        super();

        this.process = process;
        this.machine = machine;

        getElement().getStyle().setFloat(RIGHT);

        updateCaption();

        // TODO: subscribe on process state changing
//        execAgentCommandManager.subscribe
    }

    private void updateCaption() {
        setHTML(process.isAlive() ? "stop" : "re-run");
    }
}
