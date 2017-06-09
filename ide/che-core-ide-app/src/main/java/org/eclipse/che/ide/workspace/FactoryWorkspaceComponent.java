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
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.core.jsonrpc.commons.RequestTransmitter;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.factory.shared.dto.FactoryDto;
import org.eclipse.che.api.promises.client.Function;
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
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.preferences.PreferencesManager;
import org.eclipse.che.ide.api.workspace.WorkspaceServiceClient;
import org.eclipse.che.ide.context.AppContextImpl;
import org.eclipse.che.ide.context.BrowserAddress;
import org.eclipse.che.ide.context.QueryParameters;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.ui.loaders.LoaderPresenter;
import org.eclipse.che.ide.util.loging.Log;
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

    private final QueryParameters      queryParameters;
    private final FactoryServiceClient factoryServiceClient;
    private       String               workspaceId;

    @Inject
    public FactoryWorkspaceComponent(WorkspaceServiceClient workspaceServiceClient,
                                     CreateWorkspacePresenter createWorkspacePresenter,
                                     StartWorkspacePresenter startWorkspacePresenter,
                                     FactoryServiceClient factoryServiceClient,
                                     CoreLocalizationConstant locale,
                                     DtoUnmarshallerFactory dtoUnmarshallerFactory,
                                     EventBus eventBus,
                                     AppContext appContext,
                                     NotificationManager notificationManager,
                                     BrowserAddress browserAddress,
                                     DialogFactory dialogFactory,
                                     PreferencesManager preferencesManager,
                                     DtoFactory dtoFactory,
                                     LoaderPresenter loader,
                                     QueryParameters queryParameters,
                                     RequestTransmitter requestTransmitter) {
        super(workspaceServiceClient,
              createWorkspacePresenter,
              startWorkspacePresenter,
              locale,
              dtoUnmarshallerFactory,
              eventBus,
              appContext,
              notificationManager,
              browserAddress,
              dialogFactory,
              preferencesManager,
              dtoFactory,
              loader,
              requestTransmitter);
        this.factoryServiceClient = factoryServiceClient;
        this.queryParameters = queryParameters;
    }

    @Override
    public void start(final Callback<Component, Exception> callback) {
        this.callback = callback;
        Map<String, String> factoryParameters = new HashMap<>();
        for (Map.Entry<String, String> queryParam : queryParameters.getAll().entrySet()) {
            String key = queryParam.getKey();
            if (key.startsWith("factory-")) {
                factoryParameters.put(key.substring("factory-".length()), queryParam.getValue());
            }
        }

        // get workspace ID to use dedicated workspace for this factory
        this.workspaceId = queryParameters.getByName("workspaceId");

        Promise<FactoryDto> factoryPromise;
        // now search if it's a factory based on id or from parameters
        if (factoryParameters.containsKey("id")) {
            factoryPromise = factoryServiceClient.getFactory(factoryParameters.get("id"), true);
        } else {
            factoryPromise = factoryServiceClient.resolveFactory(factoryParameters, true);
        }

        factoryPromise.then((Function<FactoryDto, Void>)factory -> {
            if (appContext instanceof AppContextImpl) {
                (appContext).setFactory(factory);
            }
            // get workspace
            tryStartWorkspace();
            return null;
        }).catchError(error -> {
            Log.error(FactoryWorkspaceComponent.class, "Unable to load Factory", error);
            callback.onFailure(new Exception(error.getCause()));
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
        return workspace -> {
            if (!RUNNING.equals(workspace.getStatus())) {
                notificationManager.notify(locale.failedToLoadFactory(), locale.workspaceNotRunning(), FAIL, FLOAT_MODE);
                throw new OperationException(locale.workspaceNotRunning());
            } else {
                startWorkspace().apply(workspace);
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
