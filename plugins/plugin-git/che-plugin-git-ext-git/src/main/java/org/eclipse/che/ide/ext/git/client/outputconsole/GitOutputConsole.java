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
     * Print text in console.
     *
     * @param text
     *         text that need to be shown
     */
    void print(@NotNull String text);

    /**
     * Print colored text in console.
     *
     * @param text
     *         text that need to be shown
     * @param color
     *         color of printed text
     */
    void print(@NotNull String text, @NotNull String color);

    /**
     * Print error in console.
     *
     * @param text
     *         text that need to be shown as error
     */
    void printError(@NotNull String text);

    /** Clear console. Remove all messages. */
    void clear();
}
