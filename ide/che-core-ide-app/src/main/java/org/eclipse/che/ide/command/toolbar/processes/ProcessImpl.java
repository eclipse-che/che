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

import org.eclipse.che.ide.api.workspace.model.MachineImpl;

import java.util.Objects;

/** Data object for {@link Process}. */
public class ProcessImpl implements Process {

    private final String      commandName;
    private final String      commandLine;
    private final int         pid;
    private final boolean     alive;
    private final MachineImpl machine;

    public ProcessImpl(String commandName, String commandLine, int pid, boolean alive, MachineImpl machine) {
        this.commandName = commandName;
        this.commandLine = commandLine;
        this.pid = pid;
        this.alive = alive;
        this.machine = machine;
    }

    @Override
    public String getName() {
        return commandName;
    }

    @Override
    public String getCommandLine() {
        return commandLine;
    }

    @Override
    public int getPid() {
        return pid;
    }

    @Override
    public boolean isAlive() {
        return alive;
    }

    @Override
    public MachineImpl getMachine() {
        return machine;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProcessImpl process = (ProcessImpl)o;

        return pid == process.pid &&
               alive == process.alive &&
               Objects.equals(commandName, process.commandName) &&
               Objects.equals(commandLine, process.commandLine) &&
               Objects.equals(machine, process.machine);
    }

    @Override
    public int hashCode() {
        return Objects.hash(commandName, commandLine, pid, alive, machine);
    }
}
