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

import org.eclipse.che.api.core.jsonrpc.commons.RequestTransmitter;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.ext.java.shared.dto.search.FindUsagesRequest;
import org.eclipse.che.ide.ext.java.shared.dto.search.FindUsagesResponse;
import org.eclipse.che.ide.ui.loaders.request.LoaderFactory;
import org.eclipse.che.ide.ui.loaders.request.MessageLoader;

import static org.eclipse.che.api.promises.client.callback.AsyncPromiseHelper.createFromAsyncRequest;

/**
 * WebSocket implementation of JavaSearchService
 */
@Singleton
public class JavaSearchJsonRpcClient implements JavaSearchService {

    private static final int    TIMEOUT               = 20_000;
    private static final String ENDPOINT_ID           = "ws-agent";
    private static final String METHOD_NAME           = "javaSearch/findUsages";
    private static final String TIMEOUT_ERROR_MESSAGE = "Failed due timeout";

    private final RequestTransmitter transmitter;
    private final MessageLoader      loader;

    @Inject
    public JavaSearchJsonRpcClient(LoaderFactory loaderFactory, RequestTransmitter transmitter) {
        this.loader = loaderFactory.newLoader();
        this.transmitter = transmitter;
    }

    @Override
    public Promise<FindUsagesResponse> findUsages(final FindUsagesRequest request) {
        return createFromAsyncRequest(callback -> {
            loader.show();
            transmitter.newRequest()
                       .endpointId(ENDPOINT_ID)
                       .methodName(METHOD_NAME)
                       .paramsAsDto(request)
                       .sendAndReceiveResultAsDto(FindUsagesResponse.class, TIMEOUT)
                       .onSuccess(response -> {
                           loader.hide();
                           callback.onSuccess(response);
                       })
                       .onFailure(error -> {
                           loader.hide();
                           callback.onFailure(new RuntimeException(error.getMessage()));
                       })
                       .onTimeout(() -> {
                           loader.hide();
                           callback.onFailure(new RuntimeException(TIMEOUT_ERROR_MESSAGE));
                       });
        });
    }
}
