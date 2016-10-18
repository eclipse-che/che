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
package org.eclipse.che.ide.extension.machine.client.outputspanel.console;

import org.eclipse.che.api.machine.shared.dto.MachineProcessDto;
import org.eclipse.che.ide.api.outputconsole.OutputConsole;
import org.eclipse.che.ide.api.command.CommandImpl;

/**
 * Describes requirements for the console for command output.
 *
 * @author Artem Zatsarynnyi
 */
public interface CommandOutputConsole extends OutputConsole {

    /** Return command which output this console shows. */
    CommandImpl getCommand();

    /** Start listening to the output on the given WebSocket channel. */
    void listenToOutput(String wsChannel);

    /** Attaches to the process launched by the command. */
    void attachToProcess(MachineProcessDto process);
}
