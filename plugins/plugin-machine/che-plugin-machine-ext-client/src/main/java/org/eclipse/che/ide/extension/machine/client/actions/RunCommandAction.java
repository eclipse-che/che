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

import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.extension.machine.client.MachineLocalizationConstant;
import org.eclipse.che.ide.extension.machine.client.command.CommandConfiguration;
import org.eclipse.che.ide.extension.machine.client.command.CommandManager;
import org.eclipse.che.ide.util.loging.Log;


/**
 * Allows to run predefined command without UI.
 *
 * @author Max Shaposhnik
 *
 */
public class RunCommandAction extends Action {

    public static final String NAME_PARAM_ID = "name";

    private final SelectCommandComboBoxReady  selectCommandAction;
    private final CommandManager              commandManager;
    private final MachineLocalizationConstant localizationConstant;


    @Inject
    public RunCommandAction(SelectCommandComboBoxReady selectCommandAction,
                            MachineLocalizationConstant localizationConstant,
                            CommandManager commandManager) {
        this.selectCommandAction = selectCommandAction;
        this.localizationConstant = localizationConstant;
        this.commandManager = commandManager;
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        if (event.getParameters() == null) {
            Log.error(getClass(), localizationConstant.runCommandEmptyParamsMessage());
            return;
        }

        String name = event.getParameters().get(NAME_PARAM_ID);
        if (name == null) {
            Log.error(getClass(), localizationConstant.runCommandEmptyNameMessage());
            return;
        }

        final CommandConfiguration command = selectCommandAction.getCommandByName(name);
        if (command != null) {
            commandManager.execute(command);
        }
    }
}
