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
package org.eclipse.che.plugin.svn.ide.common;

import org.eclipse.che.ide.api.parts.PartPresenter;
import org.eclipse.che.ide.api.parts.base.BasePresenter;
import org.eclipse.che.ide.workspace.WorkBenchPartControllerImpl;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.plugin.svn.ide.SubversionExtensionLocalizationConstants;
import org.vectomatic.dom.svg.ui.SVGResource;

import org.eclipse.che.commons.annotation.Nullable;
import javax.validation.constraints.NotNull;

/**
 * Presenter for the {@link SubversionOutputConsoleView}.
 */
@Singleton
public class SubversionOutputConsolePresenter extends BasePresenter implements SubversionOutputConsoleView.ActionDelegate {

    private final SubversionOutputConsoleView view;
    private final String title;

    @Inject
    public SubversionOutputConsolePresenter(final SubversionExtensionLocalizationConstants constants, final SubversionOutputConsoleView view) {
        this.title = constants.subversionLabel();
        this.view = view;

        this.view.setTitle(title);
        this.view.setDelegate(this);
    }

    @Override
    public void onClearClicked() {
        clear();
    }

    @Override
    public void onOpen() {
        super.onOpen();

        Scheduler.get().scheduleFixedDelay(new Scheduler.RepeatingCommand() {
            @Override
            public boolean execute() {
                view.scrollBottom();

                return false;
            }
        }, WorkBenchPartControllerImpl.DURATION);
    }

    public void print(@NotNull final String text) {
        final String[] lines = text.split("\n");

        for (final String line : lines) {
            view.print(line.isEmpty() ? " " : line);
        }

        performPostOutputActions();
    }

    public void clear() {
        view.clear();
    }

    @Override
    public String getTitle() { return title; }

    @Override
    public void setVisible(boolean isVisible) {
        view.setVisible(isVisible);
    }

    @Override
    public IsWidget getView() {
        return view;
    }

    @Nullable
    @Override
    public ImageResource getTitleImage() { return null; }

    @Override
    public SVGResource getTitleSVGImage() {
        return null;
    }

    @Nullable
    @Override
    public String getTitleToolTip() {
        return "Displays Subversion command output";
    }

    @Override
    public void go(final AcceptsOneWidget container) {
        container.setWidget(this.view);
    }

    private void performPostOutputActions() {
        final PartPresenter activePart = partStack.getActivePart();

        if (activePart == null || !activePart.equals(this)) {
            partStack.setActivePart(this);
        }

        view.scrollBottom();
    }

}
