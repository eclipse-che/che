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
package org.eclipse.che.ide.extension.machine.client.processes;

import com.google.inject.Inject;

import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.extension.machine.client.MachineLocalizationConstant;
import org.eclipse.che.ide.extension.machine.client.MachineResources;

import javax.validation.constraints.NotNull;
import java.util.Collections;

import static org.eclipse.che.ide.workspace.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

/**
 * Action to open new terminal for the selected machine.
 *
 * @author Vitaliy Guliy
 */
public class NewTerminalAction extends AbstractPerspectiveAction {

    private final ConsolesPanelPresenter consolesPanelPresenter;

    @Inject
    public NewTerminalAction(MachineLocalizationConstant locale,
                             MachineResources machineResources,
                             ConsolesPanelPresenter consolesPanelPresenter) {
        super(Collections.singletonList( PROJECT_PERSPECTIVE_ID),
                locale.newTerminal(),
                locale.newTerminalDescription(),
                null, machineResources.addTerminalIcon());

        this.consolesPanelPresenter = consolesPanelPresenter;
    }

    /** {@inheritDoc} */
    @Override
    public void updateInPerspective(@NotNull ActionEvent event) {
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed(@NotNull ActionEvent event) {
        consolesPanelPresenter.newTerminal();
    }
}
