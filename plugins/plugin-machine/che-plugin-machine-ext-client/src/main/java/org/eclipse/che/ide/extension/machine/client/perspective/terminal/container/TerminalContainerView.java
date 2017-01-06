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
package org.eclipse.che.ide.extension.machine.client.perspective.terminal.container;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.ImplementedBy;

import org.eclipse.che.ide.extension.machine.client.perspective.terminal.TerminalPresenter;

import javax.validation.constraints.NotNull;

/**
 * Provides methods which allows control view representation of container with terminals.
 *
 * @author Dmitry Shnurenko
 */
@ImplementedBy(TerminalContainerViewImpl.class)
interface TerminalContainerView extends IsWidget {

    /**
     * Adds terminal to container.
     *
     * @param terminal
     *         terminal which will be added
     */
    void addTerminal(@NotNull TerminalPresenter terminal);

    /**
     * Shows current terminal in container.
     *
     * @param terminal
     *         terminal which will be shown
     */
    void showTerminal(@NotNull TerminalPresenter terminal);

    /**
     * Change visibility of terminals' container.
     *
     * @param visible
     *         <code>true</code> container is visible, <code>false</code> container is not visible
     */
    void setVisible(boolean visible);
}
