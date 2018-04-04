/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.multiuser.machine.authentication.ide;

import static com.google.common.net.HttpHeaders.AUTHORIZATION;

import com.google.gwt.core.client.Callback;
import com.google.gwt.http.client.RequestBuilder;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.callback.CallbackPromiseHelper;
import org.eclipse.che.ide.rest.AsyncRequest;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.Unmarshallable;

/**
 * Modify each machine request and add the authorization header with the token value.
 *
 * @author Anton Korneta
 */
public class MachineAsyncRequest extends AsyncRequest {

  private final String token;

  protected MachineAsyncRequest(
      RequestBuilder.Method method, String url, boolean async, String token) {
    super(method, url, async);
    this.token = token;
  }

  @Override
  public Promise<Void> send() {
    requestBuilder.setIncludeCredentials(true);
    header(AUTHORIZATION, token);
    return super.send();
  }

  @Override
  public <R> Promise<R> send(final Unmarshallable<R> unmarshaller) {
    return CallbackPromiseHelper.createFromCallback(
        new CallbackPromiseHelper.Call<R, Throwable>() {
          @Override
          public void makeCall(final Callback<R, Throwable> callback) {
            send(
                new AsyncRequestCallback<R>(unmarshaller) {
                  @Override
                  protected void onSuccess(R result) {
                    callback.onSuccess(result);
                  }

                  @Override
                  protected void onFailure(Throwable exception) {
                    callback.onFailure(exception);
                  }
                });
          }
        });
  }

  @Override
  public void send(final AsyncRequestCallback<?> callback) {
    requestBuilder.setIncludeCredentials(true);
    header(AUTHORIZATION, token);
    super.send(callback);
  }
}
