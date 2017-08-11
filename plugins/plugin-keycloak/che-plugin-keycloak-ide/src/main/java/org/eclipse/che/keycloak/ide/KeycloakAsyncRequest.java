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
package org.eclipse.che.keycloak.ide;

import com.google.gwt.http.client.RequestBuilder;

import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.rest.AsyncRequest;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.HTTPHeader;
import org.eclipse.che.ide.rest.Unmarshallable;
import org.eclipse.che.ide.util.loging.Log;

/**
 * KeycloakAsyncRequests
 */
public class KeycloakAsyncRequest extends AsyncRequest{

    private Promise<Keycloak> keycloakPromise;

    public KeycloakAsyncRequest (Promise<Keycloak> keycloakPromise,
                                 RequestBuilder.Method method,
                                 String url,
                                 boolean async) {
        super(method, url, async);
        this.keycloakPromise = keycloakPromise;
    }

    private void addAuthorizationHeader(Keycloak keycloak) {
        header(HTTPHeader.AUTHORIZATION, "Bearer " + keycloak.getToken());
    }

    private static interface Sender<R> {
        Promise<R> doSend();
    }

    private <R> Promise<R> doAfterKeycloakInitAndUpdate(Sender<R> sender) {
        return keycloakPromise
                .thenPromise(new Function<Keycloak, Promise<R>>() {
                    @Override
                    public Promise<R> apply(Keycloak keycloak) {
                        Log.debug(getClass(), "Keycloak initialized with token: ", keycloak.getToken());
                        try {
                            return keycloak.updateToken(5).thenPromise(new Function<Boolean, Promise<R>>() {
                                @Override
                                public Promise<R> apply(Boolean refreshed) {
                                    if (refreshed) {
                                        Log.debug(getClass(),
                                                  "Keycloak updated token before sending the request `",
                                                  KeycloakAsyncRequest.this.requestBuilder.getUrl(),
                                                  "`. New token is : ",
                                                  keycloak.getToken());
                                    } else {
                                        Log.debug(getClass(),
                                                  "Keycloak didn't need to update token before sending the request `",
                                                  KeycloakAsyncRequest.this.requestBuilder.getUrl(),
                                                  "`");
                                    }
                                    addAuthorizationHeader(keycloak);
                                    try {
                                        return sender.doSend();
                                    } catch(Throwable t) {
                                        Log.error(getClass(), t);
                                        throw t;
                                    }
                                }
                            });
                        } catch(Throwable t) {
                            Log.error(getClass(), t);
                            throw t;
                        }
                    }
                });
    }

    @Override
    public Promise<Void> send() {
        return doAfterKeycloakInitAndUpdate(() -> {
            return KeycloakAsyncRequest.super.send();
        });
    }

    @Override
    public void send(AsyncRequestCallback< ? > callback) {
        doAfterKeycloakInitAndUpdate(() -> {
            KeycloakAsyncRequest.super.send(callback);
            return null;
        });
    }

    @Override
    public <R> Promise<R> send(Unmarshallable<R> unmarshaller) {
        return doAfterKeycloakInitAndUpdate(() -> {
            return super.send(unmarshaller);
        });
    }
}
