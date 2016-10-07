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
package org.eclipse.che.ide.api.macro;

import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.command.CommandImpl;
import org.eclipse.che.ide.api.command.CommandManager;

/**
 * Expands all {@link Macro}s in the given string.
 *
 * @author Artem Zatsarynnyi
 * @see Macro
 * @see CommandManager#executeCommand(CommandImpl, Machine)
 */
public interface MacroProcessor {

    /**
     * Expands all known macros in the given string.
     * If macro is unknown it will be skipped.
     *
     * @param text
     *         string which may contain macros
     * @return a promise that resolves to the given {@code text} with expanded macros
     */
    Promise<String> expandMacros(String text);
}
