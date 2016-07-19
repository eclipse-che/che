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
package org.eclipse.che.ide.extension.machine.client.processes.container;

import com.google.gwt.user.client.ui.SimplePanel;

import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.ide.api.parts.base.BaseActionDelegate;

/**
 * View of {@link ConsolesContainerPresenter}.
 *
 * @author Roman Nikitenko
 */
public interface ConsolesContainerView extends View<ConsolesContainerView.ActionDelegate> {

    /**
     * Set view's title.
     *
     * @param title
     *         new title
     */
    void setTitle(String title);

    /**
     * Sets whether this view is visible.
     *
     * @param visible
     *         <code>true</code> to show the view, <code>false</code> to
     *         hide it
     */
    void setVisible(boolean visible);

    /**
     * Returns container for displaying all processes.
     * Processes will be displayed at the top or on the left of 'Consoles' area depending on the selected mode.
     */
    SimplePanel getProcessesContainer();

    /**
     * Returns container for displaying additional terminals panel.
     * Terminals will be displayed at the bottom or on the right of 'Consoles' area depending on the selected mode.
     */
    SimplePanel getTerminalsContainer();

    /**
     * Uses the entire 'Consoles' area to display all processes and hides additional terminals panel.
     */
    void applyDefaultMode();

    /**
     * Divides 'Consoles' area into two equal vertical parts.
     * Displays all processes on the left and additional terminals panel on the right(only terminals).
     */
    void splitVertically();

    /**
     * Divides 'Consoles' area into two equal horizontal parts.
     * Displays all processes on top and additional terminals panel below(only terminals).
     */
    void splitHorizontally();

    interface ActionDelegate extends BaseActionDelegate {

        /**
         * Will be called when user clicks 'Default Mode' button
         *
         */
        void onDefaultModeClick();

        /**
         * Will be called when user clicks 'Split Vertically' button
         *
         */
        void onSplitVerticallyClick();

        /**
         * Will be called when user clicks 'Split Horizontally' button
         *
         */
        void onSplitHorizontallyClick();
    }
}
