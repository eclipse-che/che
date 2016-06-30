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
package org.eclipse.che.ide.extension.machine.client.actions;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import com.google.web.bindery.event.shared.EventBus;
import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.workspace.event.WorkspaceStartedEvent;
import org.eclipse.che.ide.api.workspace.event.WorkspaceStoppedEvent;
import org.eclipse.che.ide.extension.machine.client.MachineLocalizationConstant;
import org.eclipse.che.ide.extension.machine.client.MachineResources;
import org.eclipse.che.ide.extension.machine.client.command.CommandConfiguration;
import org.eclipse.che.ide.extension.machine.client.command.CommandManager;

import java.util.Collections;

import static org.eclipse.che.ide.workspace.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

/**
 * Action to execute command which is selected in drop-down list.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class ExecuteSelectedCommandAction extends AbstractPerspectiveAction implements
        WorkspaceStartedEvent.Handler, WorkspaceStoppedEvent.Handler {

    private final SelectCommandComboBox selectCommandAction;
    private final CommandManager             commandManager;

    private boolean workspaceRunning = false;

    @Inject
    public ExecuteSelectedCommandAction(MachineLocalizationConstant localizationConstant,
                                        MachineResources resources,
                                        SelectCommandComboBox selectCommandAction,
                                        CommandManager commandManager,
                                        EventBus eventBus) {
        super(Collections.singletonList(PROJECT_PERSPECTIVE_ID),
              localizationConstant.executeSelectedCommandControlTitle(),
              localizationConstant.executeSelectedCommandControlDescription(),
              null,
              resources.execute());
        this.selectCommandAction = selectCommandAction;
        this.commandManager = commandManager;

        eventBus.addHandler(WorkspaceStartedEvent.TYPE, this);
        eventBus.addHandler(WorkspaceStoppedEvent.TYPE, this);
    }

    @Override
    public void updateInPerspective(ActionEvent event) {
        event.getPresentation().setVisible(workspaceRunning && selectCommandAction.getSelectedCommand() != null);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final CommandConfiguration command = selectCommandAction.getSelectedCommand();
        Machine machine = selectCommandAction.getSelectedMachine();

        if (command != null && machine != null) {
            commandManager.execute(command, machine);
        }
    }

    @Override
    public void onWorkspaceStarted(WorkspaceStartedEvent event) {
        workspaceRunning = true;
    }

    @Override
    public void onWorkspaceStopped(WorkspaceStoppedEvent event) {
        workspaceRunning = false;
    }

}
