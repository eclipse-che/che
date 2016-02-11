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

import org.eclipse.che.ide.api.mvp.View;

/**
 * View for {@link OutputConsole}.
 *
 * @author Artem Zatsarynnyi
 */
public interface OutputConsoleView extends View<OutputConsoleView.ActionDelegate> {

    /** Print the command line to the console. */
    void printCommandLine(String commandLine);

    /** Print the command preview Url. */
    void printPreviewUrl(String previewUrl);

    /**
     * Print the message.
     *
     * @param message
     *         text to print
     * @param cr
     *         if {@code true} - next message should replace the current one,
     *         if {@code false} - next message will be printed in a new line
     */
    void print(String message, boolean cr);

    /** Scrolls console to bottom. */
    void scrollBottom();

    /** Hides command title and command label */
    void hideCommand();

    /** Hides preview title and preview label */
    void hidePreview();

    /** Action handler for the view actions/controls. */
    interface ActionDelegate {
    }
}
