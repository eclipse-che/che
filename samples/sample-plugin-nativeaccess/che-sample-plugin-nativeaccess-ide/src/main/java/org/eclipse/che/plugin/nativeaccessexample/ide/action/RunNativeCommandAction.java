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
package org.eclipse.che.plugin.nativeaccessexample.ide.action;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.plugin.nativeaccessexample.machine.client.command.CommandManager;

/**
 * Action for opening a part which embeds javascript code.
 */
@Singleton
public class RunNativeCommandAction extends Action {

    public final static String ACTION_ID = "runNativeCommandSAction";

    private CommandManager commandManager;

    @Inject
    public RunNativeCommandAction(CommandManager commandManager) {
        super("Run native command demo");
        this.commandManager = commandManager;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Log.warn(getClass(), "Executing native command...");
        commandManager.execute("cd && touch che-was-here");
    }

}
