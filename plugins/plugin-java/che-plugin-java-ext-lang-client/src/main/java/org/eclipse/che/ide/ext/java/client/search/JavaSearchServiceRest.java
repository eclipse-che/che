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
package org.eclipse.che.ide.ext.java.client.search;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.machine.gwt.client.WsAgentUrlProvider;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.callback.AsyncPromiseHelper;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.ext.java.shared.dto.search.FindUsagesRequest;
import org.eclipse.che.ide.ext.java.shared.dto.search.FindUsagesResponse;
import org.eclipse.che.ide.rest.AsyncRequestFactory;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.ui.loaders.request.LoaderFactory;
import org.eclipse.che.ide.ui.loaders.request.MessageLoader;

import static org.eclipse.che.api.promises.client.callback.PromiseHelper.newCallback;
import static org.eclipse.che.api.promises.client.callback.PromiseHelper.newPromise;
import static org.eclipse.che.ide.MimeType.APPLICATION_JSON;
import static org.eclipse.che.ide.rest.HTTPHeader.CONTENT_TYPE;

/**
 * Default implementation for <code>JavaSearchService</code>
 *
 * @author Evgen Vidolob
 */
@Singleton
public class JavaSearchServiceRest implements JavaSearchService {

    private final AsyncRequestFactory    asyncRequestFactory;
    private final DtoUnmarshallerFactory unmarshallerFactory;
    private final MessageLoader          loader;
    private final String                 pathToService;
    private final WsAgentUrlProvider     urlProvider;

    @Inject
    public JavaSearchServiceRest(AsyncRequestFactory asyncRequestFactory,
                                 DtoUnmarshallerFactory unmarshallerFactory,
                                 LoaderFactory loaderFactory,
                                 AppContext appContext,
                                 WsAgentUrlProvider urlProvider) {
        this.asyncRequestFactory = asyncRequestFactory;
        this.unmarshallerFactory = unmarshallerFactory;
        this.loader = loaderFactory.newLoader();
        this.urlProvider = urlProvider;
        this.pathToService = "/jdt/" + appContext.getWorkspace().getId() + "/search/";
    }

    @Override
    public Promise<FindUsagesResponse> findUsages(final FindUsagesRequest request) {
        return newPromise(new AsyncPromiseHelper.RequestCall<FindUsagesResponse>() {
            @Override
            public void makeCall(AsyncCallback<FindUsagesResponse> callback) {

                asyncRequestFactory.createPostRequest(urlProvider.get() + pathToService + "find/usages", request)
                                   .header(CONTENT_TYPE, APPLICATION_JSON)
                                   .loader(loader)
                                   .send(newCallback(callback, unmarshallerFactory.newUnmarshaller(FindUsagesResponse.class)));
            }
        });
    }
}
