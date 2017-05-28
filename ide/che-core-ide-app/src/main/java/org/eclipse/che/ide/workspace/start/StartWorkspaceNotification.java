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
package org.eclipse.che.ide.workspace.start;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import org.eclipse.che.ide.bootstrap.CurrentWorkspaceManager;
import org.eclipse.che.ide.ui.loaders.LoaderPresenter;

/**
 * Toast notification appearing on the top of the IDE and containing a proposal message to start
 * current workspace and the button to perform the operation.
 *
 * @author Vitaliy Guliy
 */
@Singleton
public class StartWorkspaceNotification {

    private final WorkspaceStarterUiBinder          uiBinder;
    private final LoaderPresenter                   loader;
    private final Provider<CurrentWorkspaceManager> currentWorkspaceManagerProvider;

    @UiField
    Button   button;
    @UiField
    CheckBox restore;

    @Inject
    public StartWorkspaceNotification(LoaderPresenter loader,
                                      WorkspaceStarterUiBinder uiBinder,
                                      Provider<CurrentWorkspaceManager> currentWorkspaceManagerProvider) {
        this.loader = loader;
        this.uiBinder = uiBinder;
        this.currentWorkspaceManagerProvider = currentWorkspaceManagerProvider;
    }

    /** Displays a notification with a proposal to start current workspace. */
    public void show() {
        Widget widget = uiBinder.createAndBindUi(StartWorkspaceNotification.this);
        loader.show(LoaderPresenter.Phase.WORKSPACE_STOPPED, widget);
    }

    /**
     * Hides a notification.
     */
    public void hide() {
        loader.setSuccess(LoaderPresenter.Phase.WORKSPACE_STOPPED);
    }

    @UiHandler("button")
    void startClicked(ClickEvent e) {
        loader.setSuccess(LoaderPresenter.Phase.WORKSPACE_STOPPED);
        currentWorkspaceManagerProvider.get().startWorkspace(restore.getValue());
    }

    interface WorkspaceStarterUiBinder extends UiBinder<Widget, StartWorkspaceNotification> {
    }
}
