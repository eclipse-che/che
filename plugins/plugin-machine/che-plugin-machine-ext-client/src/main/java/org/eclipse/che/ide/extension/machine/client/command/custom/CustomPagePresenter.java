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
package org.eclipse.che.ide.extension.machine.client.command.custom;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.extension.machine.client.command.CommandConfigurationPage;

import javax.validation.constraints.NotNull;

/**
 * Page allows to edit arbitrary command.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class CustomPagePresenter implements CustomPageView.ActionDelegate, CommandConfigurationPage<CustomCommandConfiguration> {

    private final CustomPageView view;

    private CustomCommandConfiguration editedConfiguration;
    private String                     originCommandLine;
    private DirtyStateListener         listener;

    @Inject
    public CustomPagePresenter(CustomPageView view) {
        this.view = view;
        view.setDelegate(this);
    }

    @Override
    public void resetFrom(@NotNull CustomCommandConfiguration configuration) {
        editedConfiguration = configuration;
        originCommandLine = configuration.getCommandLine();
    }

    @Override
    public void go(AcceptsOneWidget container) {
        container.setWidget(view);

        view.setCommandLine(editedConfiguration.getCommandLine());
    }

    @Override
    public boolean isDirty() {
        return !originCommandLine.equals(editedConfiguration.getCommandLine());
    }

    @Override
    public void setDirtyStateListener(@NotNull DirtyStateListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCommandLineChanged() {
        editedConfiguration.setCommandLine(view.getCommandLine());
        listener.onDirtyStateChanged();
    }
}
