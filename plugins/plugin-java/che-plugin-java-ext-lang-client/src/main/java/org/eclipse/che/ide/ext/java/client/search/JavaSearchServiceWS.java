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
package org.eclipse.che.ide.ext.java.client.search;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.ext.java.shared.dto.search.FindUsagesRequest;
import org.eclipse.che.ide.ext.java.shared.dto.search.FindUsagesResponse;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.ui.loaders.request.LoaderFactory;
import org.eclipse.che.ide.ui.loaders.request.MessageLoader;
import org.eclipse.che.ide.websocket.MessageBuilder;
import org.eclipse.che.ide.websocket.MessageBus;
import org.eclipse.che.ide.websocket.MessageBusProvider;
import org.eclipse.che.ide.websocket.WebSocketException;
import org.eclipse.che.ide.websocket.rest.RequestCallback;

import static com.google.gwt.http.client.RequestBuilder.POST;
import static org.eclipse.che.api.promises.client.callback.AsyncPromiseHelper.createFromAsyncRequest;
import static org.eclipse.che.ide.MimeType.APPLICATION_JSON;
import static org.eclipse.che.ide.rest.HTTPHeader.ACCEPT;
import static org.eclipse.che.ide.rest.HTTPHeader.CONTENTTYPE;

/**
 * WebSocket implementation of JavaSearchService
 *
 * @author Evgen Vidolob
 */
@Singleton
public class JavaSearchServiceWS implements JavaSearchService {

    private MessageBusProvider     provider;
    private DtoFactory             dtoFactory;
    private MessageLoader          loader;
    private DtoUnmarshallerFactory unmarshallerFactory;
    private String                 pathToService;

    @Inject
    public JavaSearchServiceWS(MessageBusProvider provider,
                               DtoFactory dtoFactory,
                               LoaderFactory loaderFactory,
                               DtoUnmarshallerFactory unmarshallerFactory) {
        this.provider = provider;
        this.dtoFactory = dtoFactory;
        this.loader = loaderFactory.newLoader();
        this.unmarshallerFactory = unmarshallerFactory;
        this.pathToService = "/java/search/";
    }

    @Override
    public Promise<FindUsagesResponse> findUsages(final FindUsagesRequest request) {
        final MessageBus messageBus = provider.getMachineMessageBus();
        return createFromAsyncRequest(callback -> {
            final MessageBuilder builder = new MessageBuilder(POST, pathToService + "find/usages");
            builder.data(dtoFactory.toJson(request))
                   .header(CONTENTTYPE, APPLICATION_JSON)
                   .header(ACCEPT, APPLICATION_JSON);
            loader.show();

            try {
                messageBus.send(builder.build(), new RequestCallback<FindUsagesResponse>(
                        unmarshallerFactory.newWSUnmarshaller(FindUsagesResponse.class)) {
                    @Override
                    protected void onSuccess(FindUsagesResponse result) {
                        loader.hide();
                        callback.onSuccess(result);
                    }

                    @Override
                    protected void onFailure(Throwable exception) {
                        loader.hide();
                        callback.onFailure(exception);
                    }
                });
            } catch (WebSocketException e) {
                loader.hide();
                callback.onFailure(e);
            }
        });
    }
}
