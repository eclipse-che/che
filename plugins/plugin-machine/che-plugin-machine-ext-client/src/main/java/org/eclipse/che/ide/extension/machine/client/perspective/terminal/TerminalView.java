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
package org.eclipse.che.ide.extension.machine.client.perspective.terminal;

import com.google.inject.ImplementedBy;

import org.eclipse.che.ide.api.mvp.View;

import javax.validation.constraints.NotNull;

/**
 * The interface defines methods to control displaying of terminal.
 *
 * @author Dmitry Shnurenko
 */
@ImplementedBy(TerminalViewImpl.class)
interface TerminalView extends View<TerminalView.ActionDelegate> {

    interface ActionDelegate {
        void setTerminalSize(int x, int y);

        /**
         * Set focus on the terminal panel.
         *
         * @param focused
         *         {@code true} if terminal should be in focus
         */
        void setFocus(boolean focused);
    }

    /**
     * Change visibility state of panel.
     *
     * @param visible
     *         <code>true</code> panel is visible,<code>false</code> panel is not visible
     */
    void setVisible(boolean visible);

    /**
     * Opens current terminal.
     *
     * @param terminal
     *         terminal which will be opened
     */
    void openTerminal(@NotNull TerminalJso terminal);

    /**
     * Shows special error message when terminal is failed.
     *
     * @param message
     *         message which will be shown
     */
    void showErrorMessage(@NotNull String message);
}
