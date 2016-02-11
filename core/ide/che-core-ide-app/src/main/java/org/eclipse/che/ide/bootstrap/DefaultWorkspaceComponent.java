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
package org.eclipse.che.ide.bootstrap;

import com.google.common.base.Strings;
import com.google.gwt.core.client.Callback;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.machine.gwt.client.MachineManager;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.workspace.gwt.client.WorkspaceServiceClient;
import org.eclipse.che.api.workspace.shared.dto.UsersWorkspaceDto;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.actions.WorkspaceSnapshotCreator;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.preferences.PreferencesManager;
import org.eclipse.che.ide.core.Component;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.statepersistance.dto.AppState;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;
import org.eclipse.che.ide.ui.loaders.initialization.InitialLoadingInfo;
import org.eclipse.che.ide.ui.loaders.initialization.LoaderPresenter;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.ide.websocket.MessageBusProvider;
import org.eclipse.che.ide.context.BrowserQueryFieldRenderer;
import org.eclipse.che.ide.workspace.create.CreateWorkspacePresenter;
import org.eclipse.che.ide.workspace.start.StartWorkspacePresenter;

import java.util.List;

import static org.eclipse.che.ide.statepersistance.AppStateManager.PREFERENCE_PROPERTY_NAME;

/**
 * Performs default start of IDE - creates new or starts latest workspace.
 * Used when no {@code factory} specified.
 *
 * @author Max Shaposhnik (mshaposhnik@codenvy.com)
 *
 */
@Singleton
public class DefaultWorkspaceComponent extends WorkspaceComponent implements Component {

    @Inject
    public DefaultWorkspaceComponent(WorkspaceServiceClient workspaceServiceClient,
                                     CreateWorkspacePresenter createWorkspacePresenter,
                                     StartWorkspacePresenter startWorkspacePresenter,
                                     CoreLocalizationConstant locale,
                                     DtoUnmarshallerFactory dtoUnmarshallerFactory,
                                     EventBus eventBus,
                                     LoaderPresenter loader,
                                     AppContext appContext,
                                     Provider<MachineManager> machineManagerProvider,
                                     NotificationManager notificationManager,
                                     MessageBusProvider messageBusProvider,
                                     BrowserQueryFieldRenderer browserQueryFieldRenderer,
                                     DialogFactory dialogFactory,
                                     PreferencesManager preferencesManager,
                                     DtoFactory dtoFactory,
                                     InitialLoadingInfo initialLoadingInfo,
                                     WorkspaceSnapshotCreator snapshotCreator) {
        super(workspaceServiceClient,
              createWorkspacePresenter,
              startWorkspacePresenter,
              locale,
              dtoUnmarshallerFactory,
              eventBus,
              loader,
              appContext,
              machineManagerProvider,
              notificationManager,
              messageBusProvider,
              browserQueryFieldRenderer,
              dialogFactory,
              preferencesManager,
              dtoFactory,
              initialLoadingInfo,
              snapshotCreator);
    }

    /** {@inheritDoc} */
    @Override
    public void start(final Callback<Component, Exception> callback) {
        this.callback = callback;

        workspaceServiceClient.getWorkspaces(SKIP_COUNT, MAX_COUNT).then(new Operation<List<UsersWorkspaceDto>>() {
            @Override
            public void apply(List<UsersWorkspaceDto> workspaces) throws OperationException {
                if (workspaces.isEmpty()) {
                    createWorkspacePresenter.show(workspaces, callback);
                } else {
                    String wsNameFromBrowser = browserQueryFieldRenderer.getWorkspaceName();
                    if (wsNameFromBrowser.isEmpty()) {
                        tryStartRecentWorkspaceIfExist(workspaces);
                    } else {
                        for (UsersWorkspaceDto workspace : workspaces) {
                            if (wsNameFromBrowser.equals(workspace.getName())) {
                                Log.info(getClass(), workspace.getName());
                                startWorkspaceById(workspace);
                                return;
                            }
                        }
                    }
                    createWorkspacePresenter.show(workspaces, callback);
                }
            }
        }).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError error) throws OperationException {
                needToReloadComponents = true;

                dialogFactory.createMessageDialog(locale.getWsErrorDialogTitle(),
                                                  locale.getWsErrorDialogContent(error.getMessage()),
                                                  null).show();
            }
        });
    }


    private void tryStartRecentWorkspaceIfExist(List<UsersWorkspaceDto> workspaces) {
        final String recentWorkspaceId = getRecentWorkspaceId();
        if (Strings.isNullOrEmpty(recentWorkspaceId)) {
            startWorkspacePresenter.show(workspaces, callback);
        } else {
            for(UsersWorkspaceDto workspace : workspaces) {
                if (workspace.getId().equals(recentWorkspaceId)) {
                    startWorkspaceById(workspace);
                    return;
                }
            }
        }
    }

    private String getRecentWorkspaceId() {
        String json = preferencesManager.getValue(PREFERENCE_PROPERTY_NAME);

        AppState appState = null;

        try {
            appState = dtoFactory.createDtoFromJson(json, AppState.class);
        } catch (Exception exception) {
            Log.error(getClass(), "Can't create object using json: " + exception);
        }

        if (appState != null) {
            return appState.getRecentWorkspaceId();
        }
        return null;
    }

    @Override
    public void tryStartWorkspace() {
    }
}
