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
package org.eclipse.che.ide.command.toolbar;

import com.google.common.base.Optional;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.ide.api.command.CommandExecutor;
import org.eclipse.che.ide.api.command.CommandGoal;
import org.eclipse.che.ide.api.command.CommandManager;
import org.eclipse.che.ide.api.command.ContextualCommand;
import org.eclipse.che.ide.api.command.PredefinedCommandGoalRegistry;
import org.eclipse.che.ide.api.mvp.Presenter;
import org.eclipse.che.ide.command.CommandUtils;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.List;
import java.util.Map;

/**
 * Presenter for run and debug buttons
 */
@Singleton
public class CommandToolbarPresenter implements Presenter, CommandToolbarView.ActionDelegate {


    private CommandToolbarView view;
    private final CommandManager commandManager;
    private final CommandUtils commandUtils;
    private final PredefinedCommandGoalRegistry commandGoalRegistry;
    private final Provider<CommandExecutor> commandExecutor;

    @Inject
    public CommandToolbarPresenter(final CommandToolbarView view,
                                   final CommandManager commandManager,
                                   final CommandUtils commandUtils,
                                   final PredefinedCommandGoalRegistry commandGoalRegistry,
                                   final Provider<CommandExecutor> commandExecutor) {

        this.view = view;
        this.commandManager = commandManager;
        this.commandUtils = commandUtils;
        this.commandGoalRegistry = commandGoalRegistry;
        this.commandExecutor = commandExecutor;
        view.setDelegate(this);

        commandManager.addCommandLoadedListener(new CommandManager.CommandLoadedListener() {
            @Override
            public void onCommandsLoaded() {
                updateCommands();
            }
        });

        commandManager.addCommandChangedListener(new CommandManager.CommandChangedListener() {
            @Override
            public void onCommandAdded(ContextualCommand command) {
                updateCommands();
            }

            @Override
            public void onCommandUpdated(ContextualCommand previousCommand, ContextualCommand command) {

            }

            @Override
            public void onCommandRemoved(ContextualCommand command) {
                updateCommands();
            }
        });
    }

    private void updateCommands() {
        Map<CommandGoal, List<ContextualCommand>> goalListMap = commandUtils.groupCommandsByGoal(commandManager.getCommands());
        Optional<CommandGoal> run = commandGoalRegistry.getGoalById("run");
        if (run.isPresent()) {
            List<ContextualCommand> commands = goalListMap.get(run.get());
            view.setRunCommands(commands);
        }
    }

    @Override
    public void go(AcceptsOneWidget container) {
        container.setWidget(view);
    }

    @Override
    public void runCommand(ContextualCommand command, Machine machine) {
        commandExecutor.get().executeCommand(command, machine);
    }
}
