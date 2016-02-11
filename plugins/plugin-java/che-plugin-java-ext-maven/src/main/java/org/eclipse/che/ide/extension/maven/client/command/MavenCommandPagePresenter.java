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
package org.eclipse.che.ide.extension.maven.client.command;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.extension.machine.client.command.CommandConfigurationPage;

import javax.validation.constraints.NotNull;

/**
 * Page allows to configure Maven command parameters.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class MavenCommandPagePresenter implements MavenCommandPageView.ActionDelegate, CommandConfigurationPage<MavenCommandConfiguration> {

    private final MavenCommandPageView view;

    private MavenCommandConfiguration editedConfiguration;
    /** Working directory value before any editing. */
    private String                    originWorkingDirectory;
    /** Command line value before any editing. */
    private String                    originCommandLine;
    private DirtyStateListener        listener;

    @Inject
    public MavenCommandPagePresenter(MavenCommandPageView view) {
        this.view = view;
        view.setDelegate(this);
    }

    @Override
    public void resetFrom(@NotNull MavenCommandConfiguration configuration) {
        editedConfiguration = configuration;
        originWorkingDirectory = configuration.getWorkingDirectory();
        originCommandLine = configuration.getCommandLine();
    }

    @Override
    public void go(AcceptsOneWidget container) {
        container.setWidget(view);

        view.setWorkingDirectory(editedConfiguration.getWorkingDirectory());
        view.setCommandLine(editedConfiguration.getCommandLine());
    }

    @Override
    public boolean isDirty() {
        return !(originWorkingDirectory.equals(editedConfiguration.getWorkingDirectory()) &&
                 originCommandLine.equals(editedConfiguration.getCommandLine()));
    }

    @Override
    public void setDirtyStateListener(@NotNull DirtyStateListener listener) {
        this.listener = listener;
    }

    @Override
    public void onWorkingDirectoryChanged() {
        editedConfiguration.setWorkingDirectory(view.getWorkingDirectory());
        listener.onDirtyStateChanged();
    }

    @Override
    public void onCommandLineChanged() {
        editedConfiguration.setCommandLine(view.getCommandLine());
        listener.onDirtyStateChanged();
    }
}
