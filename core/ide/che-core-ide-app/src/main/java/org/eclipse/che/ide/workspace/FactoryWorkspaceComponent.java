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
package org.eclipse.che.ide.workspace;

import com.google.gwt.core.client.Callback;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.factory.gwt.client.FactoryServiceClient;
import org.eclipse.che.api.factory.shared.dto.Factory;
import org.eclipse.che.api.machine.gwt.client.MachineManager;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.workspace.gwt.client.WorkspaceServiceClient;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceDto;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.actions.WorkspaceSnapshotCreator;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.component.Component;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.preferences.PreferencesManager;
import org.eclipse.che.ide.context.BrowserQueryFieldRenderer;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;
import org.eclipse.che.ide.ui.loaders.initialization.InitialLoadingInfo;
import org.eclipse.che.ide.ui.loaders.initialization.LoaderPresenter;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.ide.websocket.MessageBusProvider;
import org.eclipse.che.ide.workspace.create.CreateWorkspacePresenter;
import org.eclipse.che.ide.workspace.start.StartWorkspacePresenter;

import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.RUNNING;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;

/**
 * Retrieves specified factory, and reuse previously created workspace for this factory.
 *
 * @author Max Shaposhnik
 * @author Florent Benoit
 */
@Singleton
public class FactoryWorkspaceComponent extends WorkspaceComponent implements Component {
    private final FactoryServiceClient factoryServiceClient;
    private String                     workspaceId;

    @Inject
    public FactoryWorkspaceComponent(WorkspaceServiceClient workspaceServiceClient,
                                     CreateWorkspacePresenter createWorkspacePresenter,
                                     StartWorkspacePresenter startWorkspacePresenter,
                                     FactoryServiceClient factoryServiceClient,
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
        this.factoryServiceClient = factoryServiceClient;
    }

    @Override
    public void start(final Callback<Component, Exception> callback) {
        this.callback = callback;
        String factoryParams = browserQueryFieldRenderer.getParameterFromURLByName("factory");

        // get workspace ID to use dedicated workspace for this factory
        this.workspaceId = browserQueryFieldRenderer.getParameterFromURLByName("workspaceId");

        factoryServiceClient.getFactory(factoryParams, true,
                                        new AsyncRequestCallback<Factory>(dtoUnmarshallerFactory.newUnmarshaller(Factory.class)) {
                                            @Override
                                            protected void onSuccess(Factory result) {
                                                appContext.setFactory(result);

                                                // get workspace
                                                tryStartWorkspace();
                                            }

                                            @Override
                                            protected void onFailure(Throwable error) {
                                                Log.error(FactoryWorkspaceComponent.class, "Unable to load Factory", error);
                                                callback.onFailure(new Exception(error.getCause()));
                                            }
                                        });
    }

    @Override
    public void tryStartWorkspace() {
        if (this.workspaceId == null) {
            notificationManager.notify(locale.failedToLoadFactory(), locale.workspaceIdUndefined(), FAIL, true);
            return;
        }

        getWorkspaceToStart().then(checkWorkspaceIsStarted()).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError arg) throws OperationException {
                notificationManager.notify(locale.workspaceNotReady(workspaceId), locale.workspaceGetFailed(), FAIL, true);
                Log.error(getClass(), arg.getMessage());
            }
        });
    }


    /**
     * Checks if specified workspace has {@link WorkspaceStatus} which is {@code RUNNING}
     */
    protected Operation<WorkspaceDto> checkWorkspaceIsStarted() {
        return new Operation<WorkspaceDto>() {
            @Override
            public void apply(WorkspaceDto workspace) throws OperationException {
                if (!RUNNING.equals(workspace.getStatus())) {
                    notificationManager.notify(locale.failedToLoadFactory(), locale.workspaceNotRunning(), FAIL, true);
                    throw new OperationException(locale.workspaceNotRunning());
                } else {
                    startWorkspace().apply(workspace);
                }
            }
        };
    }


    /**
     * Gets {@link Promise} of workspace according to workspace ID specified in parameter.
     */
    private Promise<WorkspaceDto> getWorkspaceToStart() {
        // get workspace from the given id
        return this.workspaceServiceClient.getWorkspace(workspaceId);
    }

}

