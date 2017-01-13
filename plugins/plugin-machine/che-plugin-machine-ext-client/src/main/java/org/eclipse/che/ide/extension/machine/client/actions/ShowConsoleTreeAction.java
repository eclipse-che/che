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
package org.eclipse.che.ide.extension.machine.client.actions;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.ToggleAction;
import org.eclipse.che.ide.api.event.ActivePartChangedEvent;
import org.eclipse.che.ide.api.event.ActivePartChangedHandler;
import org.eclipse.che.ide.api.parts.PartPresenter;
import org.eclipse.che.ide.extension.machine.client.MachineLocalizationConstant;
import org.eclipse.che.ide.extension.machine.client.processes.panel.ProcessesPanelPresenter;

/**
 * Action to show / hide console tree.
 *
 * @author Vitaliy Guliy
 */
@Singleton
public class ShowConsoleTreeAction extends ToggleAction implements ActivePartChangedHandler {

    private final ProcessesPanelPresenter processesPanelPresenter;

    private PartPresenter activePart;

    @Inject
    public ShowConsoleTreeAction(final EventBus eventBus,
                                 final ProcessesPanelPresenter processesPanelPresenter,
                                 final MachineLocalizationConstant machineLocalizationConstant) {
        super(machineLocalizationConstant.actionShowConsoleTreeTitle());

        this.processesPanelPresenter = processesPanelPresenter;

        eventBus.addHandler(ActivePartChangedEvent.TYPE, this);
    }

    @Override
    public boolean isSelected(ActionEvent e) {
        return processesPanelPresenter.isProcessesTreeVisible();
    }

    @Override
    public void setSelected(ActionEvent e, boolean state) {
        processesPanelPresenter.setProcessesTreeVisible(state);
    }

    @Override
    public void onActivePartChanged(ActivePartChangedEvent event) {
        activePart = event.getActivePart();
    }

    @Override
    public void update(ActionEvent e) {
        e.getPresentation().setEnabledAndVisible(activePart instanceof ProcessesPanelPresenter);
    }

}
