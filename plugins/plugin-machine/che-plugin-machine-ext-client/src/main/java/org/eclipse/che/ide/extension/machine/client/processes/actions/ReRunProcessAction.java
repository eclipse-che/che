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
package org.eclipse.che.ide.extension.machine.client.processes.actions;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import static java.util.Collections.singletonList;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.outputconsole.OutputConsole;
import org.eclipse.che.ide.extension.machine.client.MachineLocalizationConstant;
import org.eclipse.che.ide.extension.machine.client.MachineResources;
import org.eclipse.che.ide.extension.machine.client.outputspanel.console.CommandOutputConsolePresenter;
import org.eclipse.che.ide.extension.machine.client.processes.ConsolesPanelPresenter;
import org.eclipse.che.ide.extension.machine.client.processes.ProcessTreeNode;
import static org.eclipse.che.ide.workspace.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

import javax.validation.constraints.NotNull;

/**
 * Re-run selected process action.
 *
 * @author Vitaliy Guliy
 */
@Singleton
public class ReRunProcessAction extends AbstractPerspectiveAction {

    private ConsolesPanelPresenter consolesPanelPresenter;

    @Inject
    public ReRunProcessAction(ConsolesPanelPresenter consolesPanelPresenter,
                              MachineLocalizationConstant locale,
                              MachineResources machineResources) {
        super(singletonList(PROJECT_PERSPECTIVE_ID),
                locale.reRunControlTitle(),
                locale.reRunControlDescription(),
                null,
                machineResources.reRunIcon());
        this.consolesPanelPresenter = consolesPanelPresenter;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        OutputConsole outputConsole = consolesPanelPresenter.getContextOutputConsole();
        if (outputConsole != null && outputConsole instanceof CommandOutputConsolePresenter) {
            CommandOutputConsolePresenter commandOutputConsolePresenter = (CommandOutputConsolePresenter)outputConsole;
            commandOutputConsolePresenter.reRunProcessButtonClicked();
        }
    }

    @Override
    public void updateInPerspective(@NotNull ActionEvent event) {
        ProcessTreeNode processTreeNode = consolesPanelPresenter.getContextTreeNode();

        if (processTreeNode == null) {
            event.getPresentation().setEnabled(false);
            event.getPresentation().setVisible(false);
            return;
        }

        if (consolesPanelPresenter.getContextOutputConsole() instanceof CommandOutputConsolePresenter) {
            event.getPresentation().setEnabled(true);
            event.getPresentation().setVisible(true);
            return;
        }

        event.getPresentation().setEnabled(false);
        event.getPresentation().setVisible(false);
    }

}
