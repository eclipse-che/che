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
package org.eclipse.che.ide.extension.machine.client;

import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.rest.AsyncRequestFactory;
import org.eclipse.che.ide.rest.StringUnmarshaller;

import javax.inject.Inject;

/**
 * @author Mihail Kuznyetsov.
 */
public class RecipeScriptDownloadServiceClientImpl implements RecipeScriptDownloadServiceClient {

    private final String restContext;
    private final AsyncRequestFactory asyncRequestFactory;

    @Inject
    public RecipeScriptDownloadServiceClientImpl(AppContext appContext, AsyncRequestFactory asyncRequestFactory) {
        this.restContext = appContext.getMasterEndpoint();
        this.asyncRequestFactory = asyncRequestFactory;
    }

    @Override
    public Promise<String> getRecipeScript(Machine machine) {
        return asyncRequestFactory
                .createGetRequest(restContext + "/recipe/script/" + machine.getWorkspaceId() + "/" + machine.getId())
                .send(new StringUnmarshaller());
    }
}
