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
package org.eclipse.che.plugin.jsonexample.ide.editor;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.AsyncRequestFactory;
import org.eclipse.che.ide.ui.loaders.request.LoaderFactory;

import java.util.List;

/**
 * Client for retrieving a list of valid completions from a server service.
 */
@Singleton
public class JsonExampleCodeAssistClient {

    private final AppContext          appContext;
    private final AsyncRequestFactory asyncRequestFactory;
    private final LoaderFactory       loaderFactory;

    /**
     * Constructor.
     *
     * @param appContext
     *         the IDE application context
     * @param asyncRequestFactory
     *         asynchronous request factory for creating the server request
     * @param loaderFactory
     *         the loader factory for displaying a message during loading
     */
    @Inject
    public JsonExampleCodeAssistClient(
            AppContext appContext,
            AsyncRequestFactory asyncRequestFactory,
            LoaderFactory loaderFactory) {
        this.appContext = appContext;
        this.asyncRequestFactory = asyncRequestFactory;
        this.loaderFactory = loaderFactory;

    }

    public void computeProposals(AsyncRequestCallback<List<String>> callback) {
        String url = appContext.getDevMachine().getWsAgentBaseUrl() + "/json-example-completions/";
        asyncRequestFactory
                .createGetRequest(url, false)
                .loader(loaderFactory.newLoader("Loading example completions..."))
                .send(callback);
    }
}
