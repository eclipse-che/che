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
package org.eclipse.che.ide.ext.gwt.client.command;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.extension.machine.client.command.CommandConfigurationPage;

import javax.validation.constraints.NotNull;

/**
 * Page allows to configure GWT command parameters.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class GwtCommandPagePresenter implements GwtCommandPageView.ActionDelegate, CommandConfigurationPage<GwtCommandConfiguration> {

    private final GwtCommandPageView view;

    private GwtCommandConfiguration editedConfiguration;
    /** Working directory value before any editing. */
    private String                  originWorkingDirectory;
    /** GWT module value before any editing. */
    private String                  originGwtModule;
    /** Code server address value before any editing. */
    private String                  originCodeServerAddress;
    private DirtyStateListener      listener;

    @Inject
    public GwtCommandPagePresenter(GwtCommandPageView view) {
        this.view = view;
        view.setDelegate(this);
    }

    @Override
    public void resetFrom(@NotNull GwtCommandConfiguration configuration) {
        editedConfiguration = configuration;
        originWorkingDirectory = configuration.getWorkingDirectory();
        originGwtModule = configuration.getGwtModule();
        originCodeServerAddress = configuration.getCodeServerAddress();
    }

    @Override
    public void go(AcceptsOneWidget container) {
        container.setWidget(view);

        view.setWorkingDirectory(editedConfiguration.getWorkingDirectory());
        view.setGwtModule(editedConfiguration.getGwtModule());
        view.setCodeServerAddress(editedConfiguration.getCodeServerAddress());
    }

    @Override
    public boolean isDirty() {
        return !(originWorkingDirectory.equals(editedConfiguration.getWorkingDirectory()) &&
                 originGwtModule.equals(editedConfiguration.getGwtModule()) &&
                 originCodeServerAddress.equals(editedConfiguration.getCodeServerAddress()));
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
    public void onGwtModuleChanged() {
        editedConfiguration.setGwtModule(view.getGwtModule());
        listener.onDirtyStateChanged();
    }

    @Override
    public void onCodeServerAddressChanged() {
        editedConfiguration.setCodeServerAddress(view.getCodeServerAddress());
        listener.onDirtyStateChanged();
    }
}
