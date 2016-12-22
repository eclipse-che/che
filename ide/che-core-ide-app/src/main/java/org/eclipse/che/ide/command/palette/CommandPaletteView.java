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
package org.eclipse.che.ide.command.palette;

import org.eclipse.che.ide.api.command.CommandGoal;
import org.eclipse.che.ide.api.command.ContextualCommand;
import org.eclipse.che.ide.api.mvp.View;

import java.util.List;
import java.util.Map;

/**
 * The view of {@link CommandPalettePresenter}.
 *
 * @author Artem Zatsarynnyi
 */
public interface CommandPaletteView extends View<CommandPaletteView.ActionDelegate> {

    /** Show the view. */
    void show();

    /** Close the view. */
    void close();

    /**
     * Sets the commands to display in the view.
     *
     * @param commands
     *         commands grouped by type
     */
    void setCommands(Map<CommandGoal, List<ContextualCommand>> commands);

    /** The action delegate for this view. */
    interface ActionDelegate {

        /** Called when filtering commands is requested. */
        void onFilterChanged(String filterValue);

        /** Called when command execution is requested. */
        void onCommandExecute(ContextualCommand command);
    }
}
