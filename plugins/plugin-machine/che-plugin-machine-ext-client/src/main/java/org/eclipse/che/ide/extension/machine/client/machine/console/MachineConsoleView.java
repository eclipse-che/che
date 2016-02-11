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
package org.eclipse.che.ide.extension.machine.client.machine.console;

import com.google.gwt.user.client.ui.AcceptsOneWidget;

import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.ide.api.parts.base.BaseActionDelegate;

/**
 * View of {@link MachineConsolePresenter}.
 *
 * @author Artem Zatsarynnyi
 */
public interface MachineConsoleView extends View<MachineConsoleView.ActionDelegate> {

    /**
     * Returns toolbar panel.
     *
     * @return toolbar panel
     */
    AcceptsOneWidget getToolbarPanel();

    /**
     * Print message to console area.
     *
     * @param message
     *         message to output
     */
    void print(String message);

    /**
     * Set view's title.
     *
     * @param title
     *         new title
     */
    void setTitle(String title);

    /** Clears the console. */
    void clear();

    /** Scrolls console to bottom. */
    void scrollBottom();

    void setVisible(boolean visible);

    interface ActionDelegate extends BaseActionDelegate {
    }
}