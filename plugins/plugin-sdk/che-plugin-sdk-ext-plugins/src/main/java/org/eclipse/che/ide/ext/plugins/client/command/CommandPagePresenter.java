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
package org.eclipse.che.ide.ext.plugins.client.command;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.extension.machine.client.command.CommandConfigurationPage;

import javax.validation.constraints.NotNull;

/**
 * Page allows to configure {@link GwtCheCommandConfiguration} command parameters.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class CommandPagePresenter implements CommandPageView.ActionDelegate, CommandConfigurationPage<GwtCheCommandConfiguration> {

    private final CommandPageView view;

    private GwtCheCommandConfiguration editedConfiguration;
    /** GWT module value before any editing. */
    private String                     originGwtModule;
    /** Code server address value before any editing. */
    private String                     originCodeServerAddress;
    private String                     originClassPath;
    private DirtyStateListener         listener;

    @Inject
    public CommandPagePresenter(CommandPageView view) {
        this.view = view;
        view.setDelegate(this);
    }

    @Override
    public void resetFrom(@NotNull GwtCheCommandConfiguration configuration) {
        editedConfiguration = configuration;
        originGwtModule = configuration.getGwtModule();
        originCodeServerAddress = configuration.getCodeServerAddress();
        originClassPath = configuration.getClassPath();
    }

    @Override
    public void go(AcceptsOneWidget container) {
        container.setWidget(view);

        view.setGwtModule(editedConfiguration.getGwtModule());
        view.setCodeServerAddress(editedConfiguration.getCodeServerAddress());
        view.setClassPath(editedConfiguration.getClassPath());
    }

    @Override
    public boolean isDirty() {
        return !(originGwtModule.equals(editedConfiguration.getGwtModule()) &&
                 originCodeServerAddress.equals(editedConfiguration.getCodeServerAddress()) &&
                 originClassPath.equals(editedConfiguration.getClassPath()));
    }

    @Override
    public void setDirtyStateListener(@NotNull DirtyStateListener listener) {
        this.listener = listener;
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

    @Override
    public void onClassPathChanged() {
        editedConfiguration.setClassPath(view.getClassPath());
        listener.onDirtyStateChanged();
    }
}
