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

import org.eclipse.che.ide.api.mvp.View;

/**
 * View of {@link GitOutputConsolePresenter}.
 *
 * @author Andrey Plotnikov
 */
public interface GitOutputPartView extends View<GitOutputPartView.ActionDelegate> {

    interface ActionDelegate {
        /**
         * Handle user clicks on clear console button.
         */
        void onClearClicked();
        /**
         * Handle user clicks on scroll console button.
         */
        void onScrollClicked();
    }

    /**
     * Print text in console area.
     *
     * @param text
     *         text that need to be shown
     */
    void print(String text);

    /**
     * Print colored text in console area.
     *
     * @param text
     *         text that need to be shown
     * @param color
     *         color of the text
     */

    void print(String text, String color);

    /**
     * Set title of console part.
     *
     * @param title
     *         title that need to be set
     */
    void setTitle(String title);

    /** Clear console. Remove all messages. */
    void clear();

    /**
     * Scroll to bottom of the view.
     */
    void scrollBottom();
}
