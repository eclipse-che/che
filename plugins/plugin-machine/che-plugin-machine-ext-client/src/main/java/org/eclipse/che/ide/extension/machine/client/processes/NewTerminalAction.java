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
package org.eclipse.che.ide.extension.machine.client.processes;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import com.google.web.bindery.event.shared.EventBus;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.extension.machine.client.MachineLocalizationConstant;
import org.eclipse.che.ide.extension.machine.client.MachineResources;
import org.eclipse.che.ide.extension.machine.client.processes.panel.ProcessesPanelPresenter;

import java.util.Collections;

import static org.eclipse.che.ide.workspace.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

/**
 * Action to open new terminal for the selected machine.
 *
 * @author Vitaliy Guliy
 */
@Singleton
public class NewTerminalAction extends AbstractPerspectiveAction implements ProcessTreeNodeSelectedEvent.Handler {

    private final ProcessesPanelPresenter processesPanelPresenter;

    private ProcessTreeNode selectedNode;

    @Inject
    public NewTerminalAction(MachineLocalizationConstant locale,
                             MachineResources machineResources,
                             ProcessesPanelPresenter processesPanelPresenter,
                             EventBus eventBus) {
        super(Collections.singletonList(PROJECT_PERSPECTIVE_ID),
              locale.newTerminal(),
              locale.newTerminalDescription(),
              null,
              machineResources.addTerminalIcon());

        this.processesPanelPresenter = processesPanelPresenter;

        eventBus.addHandler(ProcessTreeNodeSelectedEvent.TYPE, this);
    }

    @Override
    public void updateInPerspective(ActionEvent event) {
        if (selectedNode == null) {
            event.getPresentation().setEnabledAndVisible(false);
            return;
        }

        event.getPresentation().setVisible(true);

        ProcessTreeNode node = selectedNode;

        if (ProcessTreeNode.ProcessNodeType.TERMINAL_NODE == node.getType() ||
                ProcessTreeNode.ProcessNodeType.COMMAND_NODE == node.getType()) {
            node = node.getParent();
        }

        if (ProcessTreeNode.ProcessNodeType.MACHINE_NODE == node.getType()) {
            event.getPresentation().setEnabled(node.hasTerminalAgent());
            return;
        }

        event.getPresentation().setEnabled(false);
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        processesPanelPresenter.newTerminal(this);
    }

    @Override
    public void onProcessTreeNodeSelected(ProcessTreeNodeSelectedEvent event) {
        selectedNode = event.getProcessTreeNode();
    }

}
