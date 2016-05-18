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
 * View for output console.
 *
 * @author Artem Zatsarynnyi
 */
public interface OutputConsoleView extends View<OutputConsoleView.ActionDelegate> {

    /**
     * Shows the command line to the console.
     *
     * @param commandLine
     *          command line
     */
    void showCommandLine(String commandLine);

    /**
     * Shows the command preview URL.
     *
     * @param previewUrl
     *          preview URL
     */
    void showPreviewUrl(String previewUrl);

    /**
     * Prints text.
     *
     * @param text
     *         text to print
     * @param cr
     *         if {@code true} - next message should replace the current one,
     *         if {@code false} - next message will be printed in a new line
     */
    void print(String text, boolean cr);

    /**
     * Hides command title and command label.
     */
    void hideCommand();

    /**
     * Hides preview title and preview label.
     */
    void hidePreview();

    /**
     * Wraps text in the console.
     *
     * @param wrap
     *          set <code>true</code> to wrap the text
     */
    void wrapText(boolean wrap);

    /**
     * Scrolls output to the end.
     */
    void scrollToEnd();

    /**
     * Clears the console.
     */
    void clearConsole();

    /**
     * Toggles `Wrap text` button.
     *
     * @param toggle
     *          use <code>true</code> to toggle the button
     */
    void toggleWrapTextButton(boolean toggle);

    /**
     * Enables or disables `Scroll to the end` button.
     *
     * @param enable
     *          new enabled state for the button
     */
    void enableScrollToEndButton(boolean enable);

    /** Action handler for the view actions/controls. */
    interface ActionDelegate {

        /** Handle click on `Wrap text` button. */
        void wrapTextButtonClicked();

        /** Handle click on `Scroll to end` button. */
        void scrollToEndButtonClicked();

        /** Handle click on `Clear console` button. */
        void clearConsoleButtonClicked();

        /** Handle scrolling the output. */
        void onOutputScrolled(boolean bottomReached);

    }

}
