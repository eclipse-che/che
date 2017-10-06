/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.multiuser.keycloak.ide;

import com.google.gwt.http.client.RequestBuilder;
import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.rest.AsyncRequest;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.HTTPHeader;
import org.eclipse.che.ide.rest.Unmarshallable;
import org.eclipse.che.ide.util.loging.Log;

/** KeycloakAsyncRequests */
public class KeycloakAsyncRequest extends AsyncRequest {

  private KeycloakProvider keycloakProvider;

  public KeycloakAsyncRequest(
      KeycloakProvider keycloakProvider, RequestBuilder.Method method, String url, boolean async) {
    super(method, url, async);
    this.keycloakProvider = keycloakProvider;
  }

  private void addAuthorizationHeader(String keycloakToken) {
    header(HTTPHeader.AUTHORIZATION, "Bearer " + keycloakToken);
  }

  private static interface Sender<R> {
    Promise<R> doSend();
  }

  private <R> Promise<R> doAfterKeycloakInitAndUpdate(Sender<R> sender) {
    return keycloakProvider
        .getUpdatedToken(5)
        .thenPromise(
            new Function<String, Promise<R>>() {
              @Override
              public Promise<R> apply(String keycloakToken) {
                addAuthorizationHeader(keycloakToken);
                try {
                  return sender.doSend();
                } catch (Throwable t) {
                  Log.error(getClass(), t);
                  throw t;
                }
              }
            });
  }

  @Override
  public Promise<Void> send() {
    return doAfterKeycloakInitAndUpdate(
        () -> {
          return KeycloakAsyncRequest.super.send();
        });
  }

  @Override
  public void send(AsyncRequestCallback<?> callback) {
    doAfterKeycloakInitAndUpdate(
        () -> {
          KeycloakAsyncRequest.super.send(callback);
          return null;
        });
  }

  @Override
  public <R> Promise<R> send(Unmarshallable<R> unmarshaller) {
    return doAfterKeycloakInitAndUpdate(
        () -> {
          return super.send(unmarshaller);
        });
  }
}
