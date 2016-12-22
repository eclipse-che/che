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

package org.eclipse.che.ide.command.execute;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.command.CommandExecutor;
import org.eclipse.che.ide.api.command.ContextualCommand;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.api.selection.Selection;
import org.eclipse.che.ide.api.selection.SelectionAgent;
import org.eclipse.che.ide.command.CommandUtils;
import org.eclipse.che.ide.machine.chooser.MachineChooser;
import org.vectomatic.dom.svg.ui.SVGResource;

import java.util.List;

/**
 * Action for executing a {@link ContextualCommand}.
 *
 * @author Artem Zatsarynnyi
 */
class ExecuteCommandAction extends Action {

    private final ContextualCommand command;
    private final CommandExecutor   commandExecutor;
    private final AppContext        appContext;
    private final SelectionAgent    selectionAgent;
    private final MachineChooser    machineChooser;

    @Inject
    ExecuteCommandAction(@Assisted ContextualCommand command,
                         CommandExecutor commandExecutor,
                         AppContext appContext,
                         SelectionAgent selectionAgent,
                         MachineChooser machineChooser,
                         CommandUtils commandUtils) {
        super(command.getName());

        this.command = command;
        this.commandExecutor = commandExecutor;
        this.appContext = appContext;
        this.selectionAgent = selectionAgent;
        this.machineChooser = machineChooser;

        final SVGResource commandIcon = commandUtils.getCommandTypeIcon(command.getType());
        if (commandIcon != null) {
            getTemplatePresentation().setSVGResource(commandIcon);
        }
    }

    @Override
    public void update(ActionEvent e) {
        // it should be possible to execute any command
        // if machine is currently selected
        if (isMachineSelected()) {
            e.getPresentation().setEnabledAndVisible(true);

            return;
        }

        // let's check applicable projects
        final List<String> applicableProjects = command.getApplicableContext().getApplicableProjects();

        if (applicableProjects.isEmpty()) {
            e.getPresentation().setEnabledAndVisible(true);
        } else {
            // action should be visible only for the applicable projects

            final Resource currentResource = appContext.getResource();

            if (currentResource != null) {
                final Optional<Project> currentProjectOptional = currentResource.getRelatedProject();

                if (currentProjectOptional.isPresent()) {
                    final Project currentProject = currentProjectOptional.get();

                    if (applicableProjects.contains(currentProject.getPath())) {
                        e.getPresentation().setEnabledAndVisible(true);

                        return;
                    }
                }
            }

            e.getPresentation().setEnabledAndVisible(false);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (isMachineSelected()) {
            commandExecutor.executeCommand(command, getSelectedMachine());
        } else {
            machineChooser.show().then(new Operation<Machine>() {
                @Override
                public void apply(Machine arg) throws OperationException {
                    commandExecutor.executeCommand(command, arg);
                }
            });
        }
    }

    /** Whether machine is currently selected? */
    private boolean isMachineSelected() {
        return getSelectedMachine() != null;
    }

    /** Returns the currently selected machine. */
    @Nullable
    private Machine getSelectedMachine() {
        final Selection<?> selection = selectionAgent.getSelection();

        if (selection != null && !selection.isEmpty() && selection.isSingleSelection()) {
            final Object possibleNode = selection.getHeadElement();

            if (possibleNode instanceof Machine) {
                return (Machine)possibleNode;
            }
        }

        return null;
    }
}
