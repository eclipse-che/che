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
     * @param carriageReturn
     *         if {@code true} - next message should replace the current one,
     *         if {@code false} - next message will be printed in a new line
     */
    void print(String text, boolean carriageReturn);

    /**
     * Prints colored text.
     *
     * @param text
     *         text to print
     * @param carriageReturn
     *         if {@code true} - next message should replace the current one,
     *         if {@code false} - next message will be printed in a new line
     * @param color
     *         color of the text or NULL
     */
    void print(String text, boolean carriageReturn, String color);

    /**
     * Returns the console text.
     *
     * @return
     *         console text
     */
    String getText();

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
     * Enables auto scroll when output.
     */
    void enableAutoScroll(boolean enable);

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
     * Toggles `Scroll to the end` button.
     *
     * @param toggle
     *          use <code>true</code> to toggle the button
     */
    void toggleScrollToEndButton(boolean toggle);

    /**
     * Sets visibility for Re-Run button.
     *
     * @param visible
     *          use <code>true</code> to show the button
     */
    void setReRunButtonVisible(boolean visible);

    /**
     * Sets visibility for Stop button.
     *
     * @param visible
     *          use <code>true</code> to show the button
     */
    void setStopButtonVisible(boolean visible);

    /**
     * Enables or disables Stop process button.
     *
     * @param enable
     *          new enabled state for the button
     */
    void enableStopButton(boolean enable);

    /** Action handler for the view actions/controls. */
    interface ActionDelegate {

        /** Handle click on `Run process` button. */
        void reRunProcessButtonClicked();

        /** Handle click on `Stop process` button. */
        void stopProcessButtonClicked();

        /** Handle click on `Clear console` button. */
        void clearOutputsButtonClicked();

        /** Handle click on `Download outputs` button. */
        void downloadOutputsButtonClicked();

        /** Handle click on `Wrap text` button. */
        void wrapTextButtonClicked();

        /** Handle click on `Scroll to end` button. */
        void scrollToBottomButtonClicked();

        /** Handle scrolling the output. */
        void onOutputScrolled(boolean bottomReached);

    }

}
