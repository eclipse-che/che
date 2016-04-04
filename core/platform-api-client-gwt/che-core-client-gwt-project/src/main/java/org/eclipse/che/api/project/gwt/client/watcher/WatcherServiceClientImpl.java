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
package org.eclipse.che.api.project.gwt.client.watcher;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

import org.eclipse.che.api.machine.gwt.client.WsAgentUrlProvider;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.callback.AsyncPromiseHelper;
import org.eclipse.che.ide.rest.AsyncRequestFactory;

import javax.validation.constraints.NotNull;

import static org.eclipse.che.api.promises.client.callback.PromiseHelper.newCallback;
import static org.eclipse.che.api.promises.client.callback.PromiseHelper.newPromise;

/**
 * The class contains business logic which allows to send request to
 * {@link org.eclipse.che.api.project.server.watcher.WatcherService}
 *
 * @author Dmitry Shnurenko
 */
final class WatcherServiceClientImpl implements WatcherServiceClient {

    private final AsyncRequestFactory asyncRequestFactory;
    private final WsAgentUrlProvider  urlProvider;

    @Inject
    public WatcherServiceClientImpl(AsyncRequestFactory asyncRequestFactory,
                                    WsAgentUrlProvider urlProvider) {
        this.asyncRequestFactory = asyncRequestFactory;
        this.urlProvider = urlProvider;
    }

    /** {@inheritDoc} */
    @Override
    public Promise<Void> registerRecursiveWatcher(@NotNull final String workspaceId) {
        return newPromise(new AsyncPromiseHelper.RequestCall<Void>() {
            @Override
            public void makeCall(AsyncCallback<Void> callback) {
                String url = urlProvider.get() + "/watcher/" + workspaceId + "/register";

                asyncRequestFactory.createGetRequest(url).send(newCallback(callback));
            }
        });
    }
}
