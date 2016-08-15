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

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import org.eclipse.che.ide.api.outputconsole.OutputConsole;
import org.eclipse.che.ide.extension.machine.client.MachineResources;
import org.vectomatic.dom.svg.ui.SVGResource;

import java.util.ArrayList;
import java.util.List;

/**
 * Console panel for some text outputs.
 *
 * @author Valeriy Svydenko
 */
public class DefaultOutputConsole implements OutputConsole, OutputConsoleView.ActionDelegate {

    private final OutputConsoleView             view;
    private final MachineResources              resources;
    private       String                        title;

    private final List<ConsoleOutputListener>   outputListeners;

    private boolean                             wrapText;

    /** Follow output when printing text */
    private boolean                             followOutput = true;

    @Inject
    public DefaultOutputConsole(OutputConsoleView view,
                                MachineResources resources,
                                @Assisted String title) {
        this.view = view;
        this.title = title;
        this.resources = resources;
        outputListeners = new ArrayList<>();

        view.setDelegate(this);

        view.hideCommand();
        view.hidePreview();
        view.setReRunButtonVisible(false);
        view.setStopButtonVisible(false);
    }

    /**
     * Print text in the console.
     *
     * @param text
     *         text to be printed
     */
    public void printText(String text) {
        view.print(text, text.endsWith("\r"));

        for (ConsoleOutputListener outputListener : outputListeners) {
            outputListener.onConsoleOutput(this);
        }
    }

    /**
     * Print colored text in the console.
     *
     * @param text
     *         text to be printed
     * @param color
     *         color of the text or NULL
     */
    public void printText(String text, String color) {
        view.print(text, text.endsWith("\r"), color);

        for (ConsoleOutputListener outputListener : outputListeners) {
            outputListener.onConsoleOutput(this);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void go(AcceptsOneWidget container) {
        container.setWidget(view);
    }

    /** {@inheritDoc} */
    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public SVGResource getTitleIcon() {
        return resources.output();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isFinished() {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public void stop() {
    }

    @Override
    public void close() {
        outputListeners.clear();
    }

    @Override
    public void addOutputListener(ConsoleOutputListener listener) {
        outputListeners.add(listener);
    }

    @Override
    public void reRunProcessButtonClicked() {
    }

    @Override
    public void stopProcessButtonClicked() {
    }

    @Override
    public void clearOutputsButtonClicked() {
        view.clearConsole();
    }

    @Override
    public void wrapTextButtonClicked() {
        wrapText = !wrapText;
        view.wrapText(wrapText);
        view.toggleWrapTextButton(wrapText);
    }

    @Override
    public void scrollToBottomButtonClicked() {
        followOutput = !followOutput;

        view.toggleScrollToEndButton(followOutput);
        view.enableAutoScroll(followOutput);
    }

    @Override
    public void onOutputScrolled(boolean bottomReached) {
        followOutput = bottomReached;
        view.toggleScrollToEndButton(bottomReached);
    }

}
