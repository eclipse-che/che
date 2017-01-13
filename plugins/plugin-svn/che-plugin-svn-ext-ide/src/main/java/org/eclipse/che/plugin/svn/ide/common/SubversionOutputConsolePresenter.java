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

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.theme.Style;
import org.eclipse.che.plugin.svn.ide.SubversionExtensionLocalizationConstants;
import org.eclipse.che.plugin.svn.ide.SubversionExtensionResources;
import org.vectomatic.dom.svg.ui.SVGResource;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * Presenter for the {@link SubversionOutputConsoleView}.
 */
public class SubversionOutputConsolePresenter implements SubversionOutputConsoleView.ActionDelegate, SubversionOutputConsole {

    private final SubversionExtensionResources resources;
    private final SubversionOutputConsoleView  view;
    private final String                       title;

    private final List<ActionDelegate>         actionDelegates = new ArrayList<>();

    @Inject
    public SubversionOutputConsolePresenter(final SubversionExtensionLocalizationConstants constants,
                                            final SubversionExtensionResources resources,
                                            final SubversionOutputConsoleView view,
                                            final AppContext appContext,
                                            @Assisted String title) {
        this.view = view;
        this.view.setDelegate(this);

        this.title = title;
        this.resources = resources;

        final Project project = appContext.getRootProject();

        if (project != null) {
            view.print(constants.consoleProjectName(project.getName()) + "\n");
        }
    }

    @Override
    public void go(final AcceptsOneWidget container) {
        container.setWidget(this.view);
    }

    public void print(@NotNull final String text) {
        final String[] lines = text.split("\n");
        for (String line : lines) {
            view.print(line.isEmpty() ? " " : line);
        }
        view.scrollBottom();

        for (ActionDelegate actionDelegate : actionDelegates) {
            actionDelegate.onConsoleOutput(this);
        }
    }

    /**
     * Print colored text in console.
     *
     * @param text
     *         text that need to be shown
     * @param color
     */
    @Override
    public void print(@NotNull String text, @NotNull String color) {
        view.print(text, color);
        view.scrollBottom();

        for (ActionDelegate actionDelegate : actionDelegates) {
            actionDelegate.onConsoleOutput(this);
        }
    }

    /**
     * Print executed command in console.
     *
     * @param text
     *         command text
     */
    @Override
    public void printCommand(@NotNull String text) {
        view.printPredefinedStyle(text, "font-weight: bold; font-style: italic;");

        for (ActionDelegate actionDelegate : actionDelegates) {
            actionDelegate.onConsoleOutput(this);
        }
    }

    /**
     * Print error in console.
     *
     * @param text
     *         text that need to be shown as error
     */
    @Override
    public void printError(@NotNull String text) {
        print(text, Style.getVcsConsoleErrorColor());
    }

    public void clear() {
        view.clear();
    }

    @Override
    public void onClearClicked() {
        clear();
    }

    @Override
    public void onScrollClicked() {
        view.scrollBottom();
    }

    @Override
    public String getTitle() {
        return title;
    }

    /**
     * Returns the title SVG image resource of this console.
     *
     * @return the title SVG image resource
     */
    @Override
    public SVGResource getTitleIcon() {
        return resources.outputIcon();
    }

    /**
     * Checks whether the console is finished outputting or not.
     */
    @Override
    public boolean isFinished() {
        return true;
    }

    /**
     * Stop process.
     */
    @Override
    public void stop() {
    }

    /**
     * Called when console is closed.
     */
    @Override
    public void close() {
        actionDelegates.clear();
    }

    @Override
    public void addActionDelegate(ActionDelegate actionDelegate) {
        actionDelegates.add(actionDelegate);
    }

}
