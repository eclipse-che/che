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
package org.eclipse.che.ide.command;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.command.CommandImpl;
import org.eclipse.che.ide.api.command.CommandManager;
import org.eclipse.che.ide.api.command.CommandProducer;

/**
 * Action for executing command which produced by {@link CommandProducer}.
 *
 * @author Artem Zatsarynnyi
 * @see CommandProducer
 */
public class CommandProducerAction extends Action {

    private final CommandProducer commandProducer;
    private final Machine         machine;
    private final CommandManager  commandManager;

    @Inject
    public CommandProducerAction(@Assisted String name,
                                 @Assisted CommandProducer commandProducer,
                                 @Assisted Machine machine,
                                 CommandManager commandManager) {
        super(name);

        this.commandProducer = commandProducer;
        this.machine = machine;
        this.commandManager = commandManager;
    }

    @Override
    public void update(ActionEvent e) {
        e.getPresentation().setEnabledAndVisible(commandProducer.isApplicable());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        CommandImpl command = commandProducer.createCommand(machine);
        commandManager.executeCommand(command, machine);
    }
}
