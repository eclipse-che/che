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
package org.eclipse.che.plugin.svn.ide.common;

import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.ide.api.parts.base.BaseActionDelegate;


/**
 * View for {@link SubversionOutputConsolePresenter}.
 */
public interface SubversionOutputConsoleView extends View<SubversionOutputConsoleView.ActionDelegate> {

    /**
     * Action handler for the view actions/controls.
     */
    interface ActionDelegate {
        /**
         * Handle the clear button.
         */
        void onClearClicked();

        /**
         * Handle user clicks on scroll console button.
         */
        void onScrollClicked();
    }

    /**
     * Print text in view.
     *
     * @param text
     *         The text to display
     */
    void print(final String text);

    /**
     * Print text in a given color in view.
     *
     * @param text
     *         The text to display
     * @param color
     *         The color to use
     */
    void print(final String text, final String color);

    /**
     * Print text with a given style in view.
     *
     * @param text
     *         The text to display
     * @param style
     *         The style to use
     */
    void printPredefinedStyle(final String text, final String style);

    /**
     * Set title of view.
     *
     * @param title
     *         The title to display
     */
    void setTitle(final String title);

    /**
     * Clear console. Remove all messages.
     */
    void clear();

    /**
     * Scroll to bottom of the view.
     */
    void scrollBottom();
}
