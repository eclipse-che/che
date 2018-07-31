/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.ext.java.client.search;

import static org.eclipse.che.api.promises.client.callback.AsyncPromiseHelper.createFromAsyncRequest;
import static org.eclipse.che.ide.api.jsonrpc.Constants.WS_AGENT_JSON_RPC_ENDPOINT_ID;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.EMERGE_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.api.core.jsonrpc.commons.RequestTransmitter;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.ext.java.shared.dto.search.FindUsagesRequest;
import org.eclipse.che.ide.ext.java.shared.dto.search.FindUsagesResponse;
import org.eclipse.che.ide.ui.loaders.request.LoaderFactory;
import org.eclipse.che.ide.ui.loaders.request.MessageLoader;

/** JSON-RPC implementation of JavaSearchService */
@Singleton
public class JavaSearchJsonRpcClient implements JavaSearchService {
  private final RequestTransmitter transmitter;
  private final NotificationManager notificationManager;
  private final MessageLoader loader;

  @Inject
  public JavaSearchJsonRpcClient(
      NotificationManager notificationManager,
      LoaderFactory loaderFactory,
      RequestTransmitter transmitter) {
    this.notificationManager = notificationManager;
    this.loader = loaderFactory.newLoader();
    this.transmitter = transmitter;
  }

  @Override
  public Promise<FindUsagesResponse> findUsages(final FindUsagesRequest request) {
    return createFromAsyncRequest(
        callback -> {
          loader.show();
          transmitter
              .newRequest()
              .endpointId(WS_AGENT_JSON_RPC_ENDPOINT_ID)
              .methodName("javaSearch/findUsages")
              .paramsAsDto(request)
              .sendAndReceiveResultAsDto(FindUsagesResponse.class, 20_000)
              .onSuccess(
                  response -> {
                    loader.hide();
                    callback.onSuccess(response);
                  })
              .onFailure(
                  error -> {
                    notificationManager.notify(
                        "Find usage request failed", error.getMessage(), FAIL, EMERGE_MODE);
                    loader.hide();
                  })
              .onTimeout(
                  () -> {
                    notificationManager.notify(
                        "Find usage request failed", "Failed due timeout", FAIL, EMERGE_MODE);
                    loader.hide();
                  });
        });
  }
}
