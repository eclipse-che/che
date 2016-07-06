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
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.dialogs.DialogFactory;
import org.eclipse.che.ide.api.machine.MachineServiceClient;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.outputconsole.OutputConsole;
import org.eclipse.che.ide.api.parts.WorkspaceAgent;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.extension.machine.client.MachineLocalizationConstant;
import org.eclipse.che.ide.extension.machine.client.MachineResources;
import org.eclipse.che.ide.extension.machine.client.command.CommandTypeRegistry;
import org.eclipse.che.ide.extension.machine.client.inject.factories.EntityFactory;
import org.eclipse.che.ide.extension.machine.client.inject.factories.TerminalFactory;
import org.eclipse.che.ide.extension.machine.client.outputspanel.console.CommandConsoleFactory;
import org.eclipse.che.ide.extension.machine.client.processes.actions.ConsoleTreeContextMenuFactory;

import javax.validation.constraints.NotNull;

/**
 * Presenter for managing terminals of additional terminals' panel.
 *
 * @author Roman Nikitenko
 */
@Singleton
public class TerminalsPanelPresenter extends ConsolesPanelPresenter {

    @Inject
    public TerminalsPanelPresenter(ConsolesPanelView view,
                                   EventBus eventBus,
                                   DtoFactory dtoFactory,
                                   DialogFactory dialogFactory,
                                   EntityFactory entityFactory,
                                   TerminalFactory terminalFactory,
                                   CommandConsoleFactory commandConsoleFactory,
                                   CommandTypeRegistry commandTypeRegistry,
                                   WorkspaceAgent workspaceAgent,
                                   NotificationManager notificationManager,
                                   MachineLocalizationConstant localizationConstant,
                                   MachineServiceClient machineService,
                                   MachineResources resources,
                                   AppContext appContext,
                                   ConsoleTreeContextMenuFactory consoleTreeContextMenuFactory) {
        super(view, eventBus, dtoFactory, dialogFactory, entityFactory, terminalFactory, commandConsoleFactory, commandTypeRegistry,
              workspaceAgent, notificationManager, localizationConstant, machineService, resources, appContext, consoleTreeContextMenuFactory);
    }

    @Override
    public void addCommandOutput(@NotNull String machineId, @NotNull OutputConsole outputConsole) {
        //we don't support this operation for additional terminals' panel
    }

    @Override
    protected void updateCommandOutput(@NotNull final String command, @NotNull OutputConsole outputConsole) {
        //we don't support this operation for additional terminals' panel
    }
}
