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
package org.eclipse.che.ide.command.toolbar.commands;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.command.CommandExecutor;
import org.eclipse.che.ide.api.command.CommandGoal;
import org.eclipse.che.ide.api.command.CommandImpl;
import org.eclipse.che.ide.api.command.CommandManager;
import org.eclipse.che.ide.api.mvp.Presenter;
import org.eclipse.che.ide.command.CommandUtils;
import org.eclipse.che.ide.command.toolbar.CommandCreationGuide;

/** Presenter drives UI of the toolbar for executing commands. */
@Singleton
public class ExecuteCommandPresenter implements Presenter, ExecuteCommandView.ActionDelegate {

    private final ExecuteCommandView        view;
    private final CommandManager            commandManager;
    private final CommandUtils              commandUtils;
    private final Provider<CommandExecutor> commandExecutorProvider;
    private final CommandCreationGuide      commandCreationGuide;

    @Inject
    public ExecuteCommandPresenter(ExecuteCommandView view,
                                   CommandManager commandManager,
                                   CommandUtils commandUtils,
                                   Provider<CommandExecutor> commandExecutorProvider,
                                   CommandCreationGuide commandCreationGuide) {
        this.view = view;
        this.commandManager = commandManager;
        this.commandUtils = commandUtils;
        this.commandExecutorProvider = commandExecutorProvider;
        this.commandCreationGuide = commandCreationGuide;

        view.setDelegate(this);

        commandManager.addCommandLoadedListener(this::updateView);
        commandManager.addCommandChangedListener(new CommandManager.CommandChangedListener() {
            @Override
            public void onCommandAdded(CommandImpl command) {
                updateView();
            }

            @Override
            public void onCommandUpdated(CommandImpl previousCommand, CommandImpl command) {
                updateView();
            }

            @Override
            public void onCommandRemoved(CommandImpl command) {
                updateView();
            }
        });
    }

    private void updateView() {
        view.setCommands(commandUtils.groupCommandsByGoal(commandManager.getCommands()));
    }

    @Override
    public void go(AcceptsOneWidget container) {
        container.setWidget(view);
    }

    @Override
    public void onCommandExecute(CommandImpl command, @Nullable Machine machine) {
        final CommandExecutor commandExecutor = commandExecutorProvider.get();

        if (machine == null) {
            commandExecutor.executeCommand(command);
        } else {
            commandExecutor.executeCommand(command, machine);
        }
    }

    @Override
    public void onGuide(CommandGoal goal) {
        commandCreationGuide.guide(goal);
    }
}
