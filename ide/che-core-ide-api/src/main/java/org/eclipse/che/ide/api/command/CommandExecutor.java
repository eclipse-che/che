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
package org.eclipse.che.ide.api.command;

import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.ide.api.macro.Macro;

/**
 * Allows to execute a command.
 *
 * @author Artem Zatsarynnyi
 */
public interface CommandExecutor {

    /**
     * Sends the the given {@code command} to the specified {@code machine} for execution.
     * <p><b>Note</b> that all {@link Macro}s will be expanded into
     * real values before sending the {@code command} for execution.
     *
     * @param command
     *         command to execute
     * @param machine
     *         machine to execute the command
     * @see Macro
     */
    void executeCommand(CommandImpl command, Machine machine);
}
