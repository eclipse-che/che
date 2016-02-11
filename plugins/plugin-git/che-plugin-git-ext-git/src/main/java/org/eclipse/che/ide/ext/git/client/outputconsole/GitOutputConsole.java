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
package org.eclipse.che.ide.ext.git.client.outputconsole;

import org.eclipse.che.ide.api.outputconsole.OutputConsole;

import javax.validation.constraints.NotNull;


/**
 * Describes requirements for the console for displaying git output.
 *
 * @author Roman Nikitenko
 */
public interface GitOutputConsole extends OutputConsole {

    /**
     * Print text on console.
     *
     * @param text
     *         text that need to be shown
     */
    void print(@NotNull String text);

    /**
     * [INFO] text
     *
     * @param text
     */
    void printInfo(String text);

    /**
     * [ERROR] text
     *
     * @param text
     */
    void printError(String text);

    /**
     * [WARNING] text
     *
     * @param text
     */
    void printWarn(String text);

    /** Clear console. Remove all messages. */
    void clear();
}
