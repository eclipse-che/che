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

import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceDto;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.app.CurrentUser;
import org.eclipse.che.ide.api.workspace.WorkspaceServiceClient;
import org.eclipse.che.ide.context.BrowserAddress;

/**
 * Performs essential initialization routines of the IDE application, such as:
 * <ul>
 * <li>initializing {@link CurrentUser} (loads profile, preferences);</li>
 * <li>initializing UI (sets theme, injects CSS styles);</li>
 * <li>initializing {@link AppContext}.</li>
 * </ul>
 */
@Singleton
class GeneralIdeInitializer implements IdeInitializer {

    protected final WorkspaceServiceClient workspaceServiceClient;
    protected final AppContext             appContext;
    protected final BrowserAddress         browserAddress;

    @Inject
    GeneralIdeInitializer(WorkspaceServiceClient workspaceServiceClient,
                          AppContext appContext,
                          BrowserAddress browserAddress) {
        this.workspaceServiceClient = workspaceServiceClient;
        this.appContext = appContext;
        this.browserAddress = browserAddress;
    }

    @Override
    public Promise<WorkspaceDto> getWorkspaceToStart() {
        final String workspaceKey = browserAddress.getWorkspaceKey();

        return workspaceServiceClient.getWorkspace(workspaceKey);
    }

    @Override
    public Promise<Void> init() {
        return getWorkspaceToStart().then((Function<WorkspaceDto, Void>)workspace -> {
            appContext.setWorkspace(workspace);
//            browserAddress.setAddress(workspace.getNamespace(), workspace.getConfig().getName());
            return null;
        }).catchError((Operation<PromiseError>)err -> {
            throw new OperationException("Can not get workspace: " + err.getCause());
        });
    }
}
