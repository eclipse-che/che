/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.bootstrap;

import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.RUNNING;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.api.factory.shared.dto.FactoryDto;
import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.QueryParameters;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.factory.FactoryServiceClient;
import org.eclipse.che.ide.api.theme.ThemeAgent;
import org.eclipse.che.ide.api.workspace.model.WorkspaceImpl;
import org.eclipse.che.ide.context.AppContextImpl;
import org.eclipse.che.ide.context.BrowserAddress;
import org.eclipse.che.ide.core.StandardComponentInitializer;
import org.eclipse.che.ide.preferences.StyleInjector;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;
import org.eclipse.che.ide.workspace.WorkspacePresenter;
import org.eclipse.che.ide.workspace.WorkspaceServiceClient;

/**
 * Represents IDE initialization strategy in case of loading from a Factory. Inherits initialization
 * steps from {@link DefaultIdeInitializationStrategy} and adds.
 */
@Singleton
class FactoryIdeInitializationStrategy extends DefaultIdeInitializationStrategy {

  private final QueryParameters queryParameters;
  private final FactoryServiceClient factoryServiceClient;

  @Inject
  FactoryIdeInitializationStrategy(
      WorkspaceServiceClient workspaceServiceClient,
      AppContext appContext,
      BrowserAddress browserAddress,
      CurrentUserInitializer currentUserInitializer,
      ThemeAgent themeAgent,
      StyleInjector styleInjector,
      Provider<StandardComponentInitializer> standardComponentsInitializerProvider,
      Provider<WorkspacePresenter> workspacePresenterProvider,
      EventBus eventBus,
      QueryParameters queryParameters,
      DialogFactory dialogFactory,
      FactoryServiceClient factoryServiceClient) {
    super(
        workspaceServiceClient,
        appContext,
        browserAddress,
        currentUserInitializer,
        themeAgent,
        styleInjector,
        standardComponentsInitializerProvider,
        workspacePresenterProvider,
        eventBus,
        dialogFactory);

    this.queryParameters = queryParameters;
    this.factoryServiceClient = factoryServiceClient;
  }

  @Override
  protected Promise<Void> initAppContext() {
    return super.initAppContext()
        .thenPromise(
            aVoid ->
                getFactory()
                    .then(
                        (Function<FactoryDto, Void>)
                            factory -> {
                              ((AppContextImpl) appContext).setFactory(factory);
                              return null;
                            })
                    .catchError(
                        (Operation<PromiseError>)
                            err -> {
                              throw new OperationException(
                                  "Unable to load Factory: " + err.getMessage(), err.getCause());
                            })
                    .then(
                        arg -> {
                          if (RUNNING != appContext.getWorkspace().getStatus()) {
                            throw new OperationException(
                                "Can't load Factory. Workspace is not running.");
                          }
                        }));
  }

  @Override
  public Promise<WorkspaceImpl> getWorkspaceToStart() {
    final String workspaceId = queryParameters.getByName("workspaceId");

    return workspaceServiceClient.getWorkspace(workspaceId);
  }

  private Promise<FactoryDto> getFactory() {
    Map<String, String> factoryParameters = new HashMap<>();
    for (Map.Entry<String, String> queryParam : queryParameters.getAll().entrySet()) {
      String key = queryParam.getKey();
      if (key.startsWith("factory-")) {
        factoryParameters.put(key.substring("factory-".length()), queryParam.getValue());
      }
    }

    Promise<FactoryDto> factoryPromise;
    // Factory may be based on id or on parameters
    if (factoryParameters.containsKey("id")) {
      factoryPromise = factoryServiceClient.getFactory(factoryParameters.get("id"), true);
    } else {
      factoryPromise = factoryServiceClient.resolveFactory(factoryParameters, true);
    }

    return factoryPromise;
  }
}
