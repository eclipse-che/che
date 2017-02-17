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

import com.google.gwt.user.client.ui.AcceptsOneWidget;

import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.ide.api.command.CommandExecutor;
import org.eclipse.che.ide.api.command.CommandGoal;
import org.eclipse.che.ide.api.command.CommandManager;
import org.eclipse.che.ide.api.command.ContextualCommand;
import org.eclipse.che.ide.api.mvp.Presenter;
import org.eclipse.che.ide.command.CommandUtils;
import org.eclipse.che.ide.command.goal.RunGoal;
import org.eclipse.che.ide.command.toolbar.previewurl.PreviewUrlListPresenter;
import org.eclipse.che.ide.command.toolbar.processes.ProcessesListPresenter;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Presenter for run and debug buttons
 */
@Singleton
public class CommandToolbarPresenter implements Presenter, CommandToolbarView.ActionDelegate {

    private final ProcessesListPresenter    processesListPresenter;
    private final PreviewUrlListPresenter   previewUrlListPresenter;
    private final CommandManager            commandManager;
    private final CommandUtils              commandUtils;
    private final Provider<CommandExecutor> commandExecutor;
    private final RunGoal                   runGoal;
    private final CommandToolbarView        view;

    @Inject
    public CommandToolbarPresenter(CommandToolbarView view,
                                   ProcessesListPresenter processesListPresenter,
                                   PreviewUrlListPresenter previewUrlListPresenter,
                                   CommandManager commandManager,
                                   CommandUtils commandUtils,
                                   Provider<CommandExecutor> commandExecutor,
                                   RunGoal runGoal) {
        this.view = view;
        this.processesListPresenter = processesListPresenter;
        this.previewUrlListPresenter = previewUrlListPresenter;
        this.commandManager = commandManager;
        this.commandUtils = commandUtils;
        this.commandExecutor = commandExecutor;
        this.runGoal = runGoal;

        view.setDelegate(this);

        commandManager.addCommandLoadedListener(this::updateCommands);

        commandManager.addCommandChangedListener(new CommandManager.CommandChangedListener() {
            @Override
            public void onCommandAdded(ContextualCommand command) {
                updateCommands();
            }

            @Override
            public void onCommandUpdated(ContextualCommand previousCommand, ContextualCommand command) {
                updateCommands();
            }

            @Override
            public void onCommandRemoved(ContextualCommand command) {
                updateCommands();
            }
        });
    }

    private void updateCommands() {
        final Map<CommandGoal, List<ContextualCommand>> commandsByGoal = commandUtils.groupCommandsByGoal(commandManager.getCommands());
        final List<ContextualCommand> runCommands = commandsByGoal.get(runGoal);

        view.setRunCommands(runCommands != null ? runCommands : Collections.emptyList());
    }

    @Override
    public void go(AcceptsOneWidget container) {
        container.setWidget(view);

        processesListPresenter.go(view.getProcessesListContainer());
        previewUrlListPresenter.go(view.getPreviewUrlsListContainer());
    }

    @Override
    public void onCommandRun(ContextualCommand command, Machine machine) {
        commandExecutor.get().executeCommand(command, machine);
    }
}
