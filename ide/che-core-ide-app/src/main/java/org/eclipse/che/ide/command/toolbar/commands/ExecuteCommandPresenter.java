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
import org.eclipse.che.ide.api.command.CommandManager;
import org.eclipse.che.ide.api.command.ContextualCommand;
import org.eclipse.che.ide.api.mvp.Presenter;
import org.eclipse.che.ide.command.CommandUtils;

import java.util.List;
import java.util.Map;

/**
 *
 */
@Singleton
public class ExecuteCommandPresenter implements Presenter, ExecuteCommandView.ActionDelegate {

    private final ExecuteCommandView        view;
    private final CommandManager            commandManager;
    private final CommandUtils              commandUtils;
    private final Provider<CommandExecutor> commandExecutorProvider;

    @Inject
    public ExecuteCommandPresenter(ExecuteCommandView view,
                                   CommandManager commandManager,
                                   CommandUtils commandUtils,
                                   Provider<CommandExecutor> commandExecutorProvider) {
        this.view = view;
        this.commandManager = commandManager;
        this.commandUtils = commandUtils;
        this.commandExecutorProvider = commandExecutorProvider;

        view.setDelegate(this);

        commandManager.addCommandLoadedListener(this::updateView);
        commandManager.addCommandChangedListener(new CommandManager.CommandChangedListener() {
            @Override
            public void onCommandAdded(ContextualCommand command) {
                updateView();
            }

            @Override
            public void onCommandUpdated(ContextualCommand previousCommand, ContextualCommand command) {
                updateView();
            }

            @Override
            public void onCommandRemoved(ContextualCommand command) {
                updateView();
            }
        });
    }

    private void updateView() {
        final Map<CommandGoal, List<ContextualCommand>> commandsByGoals = commandUtils.groupCommandsByGoal(commandManager.getCommands());
        view.setCommands(commandsByGoals);
    }

    @Override
    public void go(AcceptsOneWidget container) {
        container.setWidget(view);
    }

    @Override
    public void onCommandExecute(ContextualCommand command, @Nullable Machine machine) {
        final CommandExecutor commandExecutor = commandExecutorProvider.get();

        if (machine == null) {
            commandExecutor.executeCommand(command);
        } else {
            commandExecutor.executeCommand(command, machine);
        }
    }
}
