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

import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.parts.WorkspaceAgent;
import org.eclipse.che.ide.extension.machine.client.MachineLocalizationConstant;
import org.eclipse.che.ide.extension.machine.client.processes.ConsolesPanelPresenter;

import javax.validation.constraints.NotNull;
import java.util.Collections;

import static org.eclipse.che.ide.workspace.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

/**
 * The action contains business logic which calls special method to create new terminal in processes panel.
 *
 * @author Roman Nikitenko
 */
public class NewTerminalAction extends AbstractPerspectiveAction {

    private final ConsolesPanelPresenter consolesPanelPresenter;
    private final AppContext             appContext;
    private final WorkspaceAgent         workspaceAgent;

    @Inject
    public NewTerminalAction(AppContext appContext,
                             MachineLocalizationConstant locale,
                             ConsolesPanelPresenter consolesPanelPresenter,
                             WorkspaceAgent workspaceAgent) {
        super(Collections.singletonList(PROJECT_PERSPECTIVE_ID),
              locale.newTerminalTitle(),
              locale.newTerminalDescription(),
              null, null);

        this.consolesPanelPresenter = consolesPanelPresenter;
        this.appContext = appContext;
        this.workspaceAgent = workspaceAgent;
    }

    /** {@inheritDoc} */
    @Override
    public void updateInPerspective(@NotNull ActionEvent event) {
        event.getPresentation().setEnabled(appContext.getDevMachine() != null);
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed(@NotNull ActionEvent event) {
        consolesPanelPresenter.onAddTerminal(appContext.getDevMachine().getId());
        workspaceAgent.setActivePart(consolesPanelPresenter);
    }
}
