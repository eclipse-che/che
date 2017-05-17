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
package org.eclipse.che.ide.bootstrap;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceDto;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.factory.FactoryServiceClient;
import org.eclipse.che.ide.api.workspace.WorkspaceServiceClient;
import org.eclipse.che.ide.context.BrowserAddress;
import org.eclipse.che.ide.context.QueryParameters;

/** Performs initialization of the CHE IDE application in case of loading a Factory. */
@Singleton
class FactoryIdeInitializer extends GeneralIdeInitializer {

    private final QueryParameters      queryParameters;
    private final FactoryServiceClient factoryServiceClient;

    @Inject
    FactoryIdeInitializer(WorkspaceServiceClient workspaceServiceClient,
                          AppContext appContext,
                          BrowserAddress browserAddress,
                          QueryParameters queryParameters,
                          FactoryServiceClient factoryServiceClient) {
        super(workspaceServiceClient, appContext, browserAddress);

        this.queryParameters = queryParameters;
        this.factoryServiceClient = factoryServiceClient;
    }

    @Override
    public Promise<WorkspaceDto> getWorkspaceToStart() {
        final String workspaceId = queryParameters.getByName("workspaceId");

        return workspaceServiceClient.getWorkspace(workspaceId);
    }

    @Override
    public Promise<Void> init() {
        return super.init().then(aVoid -> {
            // TODO
            // get/resolve factory
            // appContext.setFactory(factory);
            // check whether the workspace is running
            // ...
        });
    }
}
