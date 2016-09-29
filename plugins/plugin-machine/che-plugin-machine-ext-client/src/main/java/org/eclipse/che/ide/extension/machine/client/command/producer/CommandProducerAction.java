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
package org.eclipse.che.ide.extension.machine.client.command.producer;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.extension.machine.client.command.CommandConfiguration;
import org.eclipse.che.ide.extension.machine.client.command.CommandManager;

/**
 * Action for executing command which produced by {@link CommandProducer}.
 *
 * @author Artem Zatsarynnyi
 * @see CommandProducer
 */
public class CommandProducerAction extends Action {

    private final CommandProducer commandProducer;
    private final CommandManager  commandManager;
    private final AppContext      appContext;

    @Inject
    public CommandProducerAction(@Assisted CommandProducer commandProducer, CommandManager commandManager, AppContext appContext) {
        super();

        this.commandProducer = commandProducer;
        this.commandManager = commandManager;
        this.appContext = appContext;
    }

    @Override
    public void update(ActionEvent e) {
        e.getPresentation().setText(commandProducer.getName());
        e.getPresentation().setEnabledAndVisible(commandProducer.isApplicable());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        CommandConfiguration command = commandProducer.createCommand();
        commandManager.executeCommand(command, appContext.getDevMachine().getDescriptor());
    }
}