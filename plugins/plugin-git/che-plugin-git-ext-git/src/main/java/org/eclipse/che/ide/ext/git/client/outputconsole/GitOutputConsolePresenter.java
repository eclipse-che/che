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
package org.eclipse.che.ide.ext.git.client.outputconsole;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.ide.ext.git.client.GitResources;
import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * Git output View Part.
 *
 * @author Vitaly Parfonov
 */

public class GitOutputConsolePresenter implements GitOutputPartView.ActionDelegate, GitOutputConsole {
    private String title;

    private final GitOutputPartView view;
    private final GitResources      resources;

    /** Construct empty Part */
    @Inject
    public GitOutputConsolePresenter(GitOutputPartView view,
                                     GitResources resources,
                                     final EventBus eventBus,
                                     @Assisted String title) {
        this.view = view;
        this.title = title;
        this.resources = resources;
        this.view.setDelegate(this);

    }

    /** {@inheritDoc} */
    @Override
    public void go(AcceptsOneWidget container) {
        container.setWidget(view);
    }

    /**
     * Print text on console.
     *
     * @param text
     *         text that need to be shown
     */
    public void print(String text) {
        String[] lines = text.split("\n");
        for (String line : lines) {
            view.print(line.isEmpty() ? " " : line);
        }
        view.scrollBottom();
    }

    /** {@inheritDoc} */
    public void clear() {
        view.clear();
    }

    /** {@inheritDoc} */
    @Override
    public void onClearClicked() {
        clear();
    }

    @Override
    public void onScrollClicked() {
        view.scrollBottom();
    }

    public void printInfo(String text) {
        view.printInfo(text);
        view.scrollBottom();
    }

    public void printWarn(String text) {
        view.printWarn(text);
        view.scrollBottom();
    }

    public void printError(String text) {
        view.printError(text);
        view.scrollBottom();
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public SVGResource getTitleIcon() {
        return resources.gitOutput();
    }

    @Override
    public boolean isFinished() {
        return true;
    }

    @Override
    public void onClose() {

    }
}
