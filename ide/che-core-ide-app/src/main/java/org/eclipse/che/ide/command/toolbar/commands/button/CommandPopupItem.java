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

import org.eclipse.che.ide.api.command.ContextualCommand;
import org.eclipse.che.ide.ui.menubutton.PopupItem;

/**
 * Represent command group for {@link ContextualCommand}
 */
public class CommandPopupItem implements PopupItem {


    private final ContextualCommand command;

    public CommandPopupItem(ContextualCommand command) {
        this.command = command;
    }

    @Override
    public String getName() {
        return command.getName();
    }

    @Override
    public boolean isDisabled() {
        return command.getApplicableContext().isProjectApplicable()
               || command.getApplicableContext().isWorkspaceApplicable()
               || command.getApplicableContext().isFileApplicable();
    }

    public ContextualCommand getCommand() {
        return command;
    }
}
