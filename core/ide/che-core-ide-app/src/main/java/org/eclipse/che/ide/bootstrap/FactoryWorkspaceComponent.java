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

import com.google.gwt.core.client.Callback;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.factory.gwt.client.FactoryServiceClient;
import org.eclipse.che.api.factory.shared.dto.Factory;
import org.eclipse.che.api.machine.gwt.client.MachineManager;
import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.FunctionException;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.promises.client.js.Promises;
import org.eclipse.che.api.workspace.gwt.client.WorkspaceServiceClient;
import org.eclipse.che.api.workspace.shared.dto.UsersWorkspaceDto;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.actions.WorkspaceSnapshotCreator;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.preferences.PreferencesManager;
import org.eclipse.che.ide.core.Component;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;
import org.eclipse.che.ide.ui.loaders.initialization.InitialLoadingInfo;
import org.eclipse.che.ide.ui.loaders.initialization.LoaderPresenter;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.ide.websocket.MessageBusProvider;
import org.eclipse.che.ide.context.BrowserQueryFieldRenderer;
import org.eclipse.che.ide.workspace.create.CreateWorkspacePresenter;
import org.eclipse.che.ide.workspace.start.StartWorkspacePresenter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;

/**
 * Retrieves specified factory, and creates and/or starts workspace configured in it.
 *
 * @author Max Shaposhnik
 */
@Singleton
public class FactoryWorkspaceComponent extends WorkspaceComponent implements Component {
    private static final String FACTORY_ID_ATTRIBUTE = "factoryId";

    private final FactoryServiceClient factoryServiceClient;
    private       Factory              factory;

    @Inject
    public FactoryWorkspaceComponent(WorkspaceServiceClient workspaceServiceClient,
                                     FactoryServiceClient factoryServiceClient,
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
        this.factoryServiceClient = factoryServiceClient;
    }

    @Override
    public void start(final Callback<Component, Exception> callback) {
        this.callback = callback;
        String factoryParams = browserQueryFieldRenderer.getParameterFromURLByName("factory");
        factoryServiceClient.getFactory(factoryParams, true,
                                        new AsyncRequestCallback<Factory>(dtoUnmarshallerFactory.newUnmarshaller(Factory.class)) {
                                            @Override
                                            protected void onSuccess(Factory result) {
                                                factory = result;
                                                appContext.setFactory(result);
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
        final WorkspaceConfigDto workspaceConfigDto = factory.getWorkspace();

        if (workspaceConfigDto == null) {
            notificationManager.notify(locale.failedToLoadFactory(), locale.workspaceConfigUndefined(), FAIL, true);
            return;
        }

        getWorkspaceToStart().then(startWorkspace()).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError arg) throws OperationException {
                Log.error(getClass(), arg.getMessage());
            }
        });
    }

    /**
     * Gets {@link Promise} of workspace according to {@code factory} {@link org.eclipse.che.api.factory.shared.dto.Policies}.
     * <p/>
     * <p>Return policy for workspace:
     * <p><i>perClick</i> - every click from any user always creates a new workspace every time and if policy is not specified<br/>
     * it will be used by default.
     * <p/>
     * <p><i>perUser</i> - create one workspace for a user, a 2nd click from same user reloads the same workspace.
     * <p/>
     * <p><i>perAccount</i> - only create workspace for all users. A 2nd click from any user reloads the same workspace<br/>
     * Note that if location = owner, then only 1 workspace ever is created. If location = acceptor<br/>
     * it's one workspace for each unique user.
     */
    private Promise<UsersWorkspaceDto> getWorkspaceToStart() {
        final WorkspaceConfigDto workspaceConfigDto = factory.getWorkspace();
        final String policy = factory.getPolicies() == null ? "perClick" : factory.getPolicies().getCreate();
        switch (policy) {
            case "perUser":
                return getWorkspaceByConditionOrCreateNew(workspaceConfigDto, new Function<UsersWorkspaceDto, Boolean>() {
                    @Override
                    public Boolean apply(UsersWorkspaceDto existWs) throws FunctionException {
                        return factory.getId().equals(existWs.getAttributes().get(FACTORY_ID_ATTRIBUTE));
                    }
                });
            case "perAccount":
                return getWorkspaceByConditionOrCreateNew(workspaceConfigDto, new Function<UsersWorkspaceDto, Boolean>() {
                    @Override
                    public Boolean apply(UsersWorkspaceDto arg) throws FunctionException {
                        //TODO rework it when account will be ready
                        return workspaceConfigDto.getName().equals(arg.getName());
                    }
                });
            case "perClick":
            default:
                return getWorkspaceByConditionOrCreateNew(workspaceConfigDto, new Function<UsersWorkspaceDto, Boolean>() {
                    @Override
                    public Boolean apply(UsersWorkspaceDto arg) throws FunctionException {
                        return false;
                    }
                });
        }
    }

    /**
     * Gets the workspace by condition which is determined by given {@link Function}
     * if workspace found by condition then it will be returned in other way new workspace will be returned.
     */
    private Promise<UsersWorkspaceDto> getWorkspaceByConditionOrCreateNew(final WorkspaceConfigDto workspaceConfigDto,
                                                                          final Function<UsersWorkspaceDto, Boolean> condition) {
        return workspaceServiceClient.getWorkspaces(0, 0)
                                     .thenPromise(new Function<List<UsersWorkspaceDto>, Promise<UsersWorkspaceDto>>() {
                                         @Override
                                         public Promise<UsersWorkspaceDto> apply(List<UsersWorkspaceDto> workspaces)
                                                 throws FunctionException {
                                             for (UsersWorkspaceDto existsWs : workspaces) {
                                                 if (condition.apply(existsWs)) {
                                                     return Promises.resolve(existsWs);
                                                 }
                                             }
                                             return createWorkspaceWithCounterName(workspaces, workspaceConfigDto);
                                         }
                                     });
    }

    /**
     * Create workspace with counter in name and add factoryId attribute
     * if workspace with specified name already exist.
     */
    private Promise<UsersWorkspaceDto> createWorkspaceWithCounterName(final List<UsersWorkspaceDto> workspaces,
                                                                      final WorkspaceConfigDto workspaceConfigDto) {
        workspaceConfigDto.getAttributes().put(FACTORY_ID_ATTRIBUTE, factory.getId());
        final Set<String> workspacesNames = new HashSet<>();
        final String wsName = workspaceConfigDto.getName();
        for (UsersWorkspaceDto workspace : workspaces) {
            workspacesNames.add(workspace.getName());
        }
        if (!workspacesNames.contains(wsName)) {
            return workspaceServiceClient.create(workspaceConfigDto, null);
        }
        String genName = wsName;
        int counter = 1;
        while (workspacesNames.contains(genName)) {
            genName = wsName + '-' + counter++;
        }
        workspaceConfigDto.withName(genName);
        workspaceConfigDto.getAttributes().put(FACTORY_ID_ATTRIBUTE, factory.getId());
        return workspaceServiceClient.create(workspaceConfigDto, null);
    }
}

