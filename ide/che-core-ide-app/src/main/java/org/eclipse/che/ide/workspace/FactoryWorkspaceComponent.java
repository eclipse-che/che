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
package org.eclipse.che.ide.workspace;

import com.google.gwt.core.client.Callback;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.factory.shared.dto.FactoryDto;
import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.FunctionException;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceDto;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.component.Component;
import org.eclipse.che.ide.api.dialogs.DialogFactory;
import org.eclipse.che.ide.api.factory.FactoryServiceClient;
import org.eclipse.che.ide.api.machine.MachineManager;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.preferences.PreferencesManager;
import org.eclipse.che.ide.api.workspace.WorkspaceServiceClient;
import org.eclipse.che.ide.collections.Jso;
import org.eclipse.che.ide.collections.js.JsoArray;
import org.eclipse.che.ide.context.AppContextImpl;
import org.eclipse.che.ide.context.BrowserQueryFieldRenderer;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.ui.loaders.LoaderPresenter;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.ide.websocket.MessageBusProvider;
import org.eclipse.che.ide.workspace.create.CreateWorkspacePresenter;
import org.eclipse.che.ide.workspace.start.StartWorkspacePresenter;

import java.util.HashMap;
import java.util.Map;

import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.RUNNING;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;

/**
 * Retrieves specified factory, and reuse previously created workspace for this factory.
 *
 * @author Max Shaposhnik
 * @author Florent Benoit
 */
@Singleton
public class FactoryWorkspaceComponent extends WorkspaceComponent {

    private final FactoryServiceClient           factoryServiceClient;
    private       String                         workspaceId;

    @Inject
    public FactoryWorkspaceComponent(WorkspaceServiceClient workspaceServiceClient,
                                     CreateWorkspacePresenter createWorkspacePresenter,
                                     StartWorkspacePresenter startWorkspacePresenter,
                                     FactoryServiceClient factoryServiceClient,
                                     CoreLocalizationConstant locale,
                                     DtoUnmarshallerFactory dtoUnmarshallerFactory,
                                     EventBus eventBus,
                                     AppContext appContext,
                                     Provider<MachineManager> machineManagerProvider,
                                     NotificationManager notificationManager,
                                     MessageBusProvider messageBusProvider,
                                     BrowserQueryFieldRenderer browserQueryFieldRenderer,
                                     DialogFactory dialogFactory,
                                     PreferencesManager preferencesManager,
                                     DtoFactory dtoFactory,
                                     WorkspaceEventsHandler workspaceEventsHandler,
                                     LoaderPresenter loader) {
        super(workspaceServiceClient,
              createWorkspacePresenter,
              startWorkspacePresenter,
              locale,
              dtoUnmarshallerFactory,
              eventBus,
              appContext,
              machineManagerProvider,
              notificationManager,
              messageBusProvider,
              browserQueryFieldRenderer,
              dialogFactory,
              preferencesManager,
              dtoFactory,
              workspaceEventsHandler,
              loader);
        this.factoryServiceClient = factoryServiceClient;
    }

    @Override
    public void start(final Callback<Component, Exception> callback) {
        this.callback = callback;
        Jso factoryParams = browserQueryFieldRenderer.getParameters();
        JsoArray<String> keys = factoryParams.getKeys();
        Map<String, String> factoryParameters = new HashMap<>();
        // check all factory parameters
        for (String key : keys.toList()) {
            if (key.startsWith("factory-")) {
                String value = factoryParams.getStringField(key);
                factoryParameters.put(key.substring("factory-".length()), value);
            }
        }

        // get workspace ID to use dedicated workspace for this factory
        this.workspaceId = browserQueryFieldRenderer.getParameterFromURLByName("workspaceId");

        Promise<FactoryDto> factoryPromise;
        // now search if it's a factory based on id or from parameters
        if (factoryParameters.containsKey("id")) {
            factoryPromise = factoryServiceClient.getFactory(factoryParameters.get("id"), true);
        } else {
            factoryPromise = factoryServiceClient.resolveFactory(factoryParameters, true);
        }

        Promise<Void> promise = factoryPromise.then(new Function<FactoryDto, Void>() {
            @Override
            public Void apply(final FactoryDto factory) throws FunctionException {
                
                if (appContext instanceof AppContextImpl) {
                    ((AppContextImpl)appContext).setFactory(factory);
                }

                // get workspace
                tryStartWorkspace();
                return null;
            }
        }).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError error) throws OperationException {
                Log.error(FactoryWorkspaceComponent.class, "Unable to load Factory", error);
                callback.onFailure(new Exception(error.getCause()));
            }
        });

    }

    @Override
    public void tryStartWorkspace() {
        if (this.workspaceId == null) {
            notificationManager.notify(locale.failedToLoadFactory(), locale.workspaceIdUndefined(), FAIL, FLOAT_MODE);
            return;
        }

        getWorkspaceToStart().then(checkWorkspaceIsStarted()).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError arg) throws OperationException {
                notificationManager.notify(locale.workspaceNotReady(workspaceId), locale.workspaceGetFailed(), FAIL, FLOAT_MODE);
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
                    notificationManager.notify(locale.failedToLoadFactory(), locale.workspaceNotRunning(), FAIL, FLOAT_MODE);
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
