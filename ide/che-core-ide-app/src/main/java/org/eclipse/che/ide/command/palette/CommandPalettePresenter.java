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

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.core.model.workspace.WorkspaceRuntime;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.command.CommandExecutor;
import org.eclipse.che.ide.api.command.CommandManager;
import org.eclipse.che.ide.api.command.ContextualCommand;
import org.eclipse.che.ide.api.dialogs.DialogFactory;
import org.eclipse.che.ide.command.CommandUtils;
import org.eclipse.che.ide.machine.chooser.MachineChooser;

import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import static org.eclipse.che.ide.util.StringUtils.containsIgnoreCase;

/**
 * Presenter for Command Palette.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class CommandPalettePresenter implements CommandPaletteView.ActionDelegate {

    private final CommandPaletteView view;
    private final CommandManager     commandManager;
    private final CommandExecutor    commandExecutor;
    private final DialogFactory      dialogFactory;
    private final AppContext         appContext;
    private final MachineChooser     machineChooser;
    private final CommandUtils       commandUtils;
    private final PaletteMessages    messages;

    @Inject
    public CommandPalettePresenter(CommandPaletteView view,
                                   CommandManager commandManager,
                                   CommandExecutor commandExecutor,
                                   DialogFactory dialogFactory,
                                   AppContext appContext,
                                   MachineChooser machineChooser,
                                   CommandUtils commandUtils,
                                   PaletteMessages messages) {
        this.view = view;
        this.commandManager = commandManager;
        this.commandExecutor = commandExecutor;
        this.dialogFactory = dialogFactory;
        this.appContext = appContext;
        this.machineChooser = machineChooser;
        this.commandUtils = commandUtils;
        this.messages = messages;

        view.setDelegate(this);
    }

    /** Open Command Palette. */
    public void showDialog() {
        view.show();
        view.setCommands(commandUtils.groupCommandsByGoal(commandManager.getCommands()));
    }

    @Override
    public void onFilterChanged(String filterValue) {
        final List<ContextualCommand> filteredCommands = commandManager.getCommands();

        if (!filterValue.isEmpty()) {
            final ListIterator<ContextualCommand> it = filteredCommands.listIterator();

            while (it.hasNext()) {
                final ContextualCommand command = it.next();

                if (!containsIgnoreCase(command.getName(), filterValue)) {
                    it.remove();
                }
            }
        }

        view.setCommands(commandUtils.groupCommandsByGoal(filteredCommands));
    }

    @Override
    public void onCommandExecute(final ContextualCommand command) {
        view.close();

        if (getMachines().isEmpty()) {
            // should not happen, but let's play safe
            dialogFactory.createMessageDialog("", messages.messageNoMachine(), null).show();
        } else {
            machineChooser.show().then(new Operation<Machine>() {
                @Override
                public void apply(Machine arg) throws OperationException {
                    commandExecutor.executeCommand(command, arg);
                }
            });
        }
    }

    private List<? extends Machine> getMachines() {
        final WorkspaceRuntime runtime = appContext.getWorkspace().getRuntime();

        if (runtime != null) {
            return runtime.getMachines();
        }

        return Collections.emptyList();
    }
}
