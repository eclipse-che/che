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
package org.eclipse.che.ide.command.toolbar.commands.button;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.ide.api.command.CommandImpl;
import org.eclipse.che.ide.ui.menubutton.MenuItem;

/** Item contains {@link CommandImpl} and {@link Machine}. */
public class MachineItem implements MenuItem {

    private final CommandImpl command;
    private final Machine     machine;
    private final String      name;

    @AssistedInject
    public MachineItem(@Assisted CommandImpl command, @Assisted Machine machine) {
        this.command = command;
        this.machine = machine;
        this.name = machine.getConfig().getName();
    }

    @AssistedInject
    public MachineItem(@Assisted MachineItem item) {
        this.command = item.command;
        this.machine = item.machine;
        this.name = command.getName() + " on " + machine.getConfig().getName();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isDisabled() {
        return false;
    }

    public CommandImpl getCommand() {
        return command;
    }

    public Machine getMachine() {
        return machine;
    }
}
