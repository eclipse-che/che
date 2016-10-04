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
package org.eclipse.che.ide.workspace.start;

import com.google.gwt.core.client.Callback;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.machine.shared.dto.SnapshotDto;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceDto;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.component.Component;
import org.eclipse.che.ide.api.workspace.WorkspaceServiceClient;
import org.eclipse.che.ide.ui.loaders.LoaderPresenter;
import org.eclipse.che.ide.workspace.WorkspaceComponentProvider;

import java.util.List;

/**
 * Toast notification appearing on the top of the IDE and containing a proposal message to start
 *   current workspace and the button to perform the operation.
 *
 * @author Vitaliy Guliy
 */
@Singleton
public class StartWorkspaceNotification {

    interface WorkspaceStarterUiBinder extends UiBinder<Widget, StartWorkspaceNotification> {
    }

    private final WorkspaceStarterUiBinder      uiBinder;

    private final LoaderPresenter               loader;
    private final AppContext                    appContext;
    private final WorkspaceServiceClient        workspaceServiceClient;
    private final WorkspaceComponentProvider    workspaceComponentProvider;

    @UiField
    Button                                      button;

    @UiField
    CheckBox                                    restore;

    private String                              workspaceID;

    @Inject
    public StartWorkspaceNotification(LoaderPresenter loader,
                                      WorkspaceStarterUiBinder uiBinder,
                                      AppContext appContext,
                                      WorkspaceServiceClient workspaceServiceClient,
                                      WorkspaceComponentProvider workspaceComponentProvider) {
        this.loader = loader;
        this.uiBinder = uiBinder;
        this.appContext = appContext;
        this.workspaceServiceClient = workspaceServiceClient;
        this.workspaceComponentProvider = workspaceComponentProvider;
    }

    /**
     * Displays a notification with a proposal to start workspace with ID.
     *
     * @param workspaceID
     *          workspace ID
     */
    public void show(String workspaceID) {
        this.workspaceID = workspaceID;

        workspaceServiceClient.getSnapshot(workspaceID).then(new Operation<List<SnapshotDto>>() {
            @Override
            public void apply(List<SnapshotDto> snapshots) throws OperationException {
                Widget widget = uiBinder.createAndBindUi(StartWorkspaceNotification.this);

                if (snapshots.isEmpty()) {
                    restore.setVisible(false);
                }

                loader.show(LoaderPresenter.Phase.WORKSPACE_STOPPED, widget);
            }
        });
    }

    @UiHandler("button")
    void startClicked(ClickEvent e) {
        loader.setSuccess(LoaderPresenter.Phase.WORKSPACE_STOPPED);
        workspaceComponentProvider.get().startWorkspace(workspaceID, new Callback<Component, Exception>() {
            @Override
            public void onSuccess(Component result) {
            }
            @Override
            public void onFailure(Exception reason) {
            }
        }, false, restore.getValue().booleanValue());
    }

}
