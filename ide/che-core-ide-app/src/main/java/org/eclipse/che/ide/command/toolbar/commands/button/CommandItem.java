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

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import org.eclipse.che.ide.api.command.CommandImpl;
import org.eclipse.che.ide.ui.menubutton.MenuItem;

/** A {@link MenuItem} represents {@link CommandImpl}. */
public class CommandItem implements MenuItem {

    private final CommandImpl command;

    @Inject
    public CommandItem(@Assisted CommandImpl command) {
        this.command = command;
    }

    @Override
    public String getName() {
        return command.getName();
    }

    @Override
    public boolean isDisabled() {
        return command.getApplicableContext().isWorkspaceApplicable();
    }

    public CommandImpl getCommand() {
        return command;
    }
}
