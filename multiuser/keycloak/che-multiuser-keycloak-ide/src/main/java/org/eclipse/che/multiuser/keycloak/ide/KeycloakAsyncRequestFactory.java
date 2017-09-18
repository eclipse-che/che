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

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.http.client.RequestBuilder;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import java.util.List;
import org.eclipse.che.ide.MimeType;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.rest.AsyncRequest;
import org.eclipse.che.ide.rest.HTTPHeader;
import org.eclipse.che.multiuser.machine.authentication.ide.MachineAsyncRequestFactory;
import org.eclipse.che.multiuser.machine.authentication.ide.MachineTokenServiceClient;

/** KeycloakAuthAsyncRequestFactory */
@Singleton
public class KeycloakAsyncRequestFactory extends MachineAsyncRequestFactory {

  private final DtoFactory dtoFactory;
  private KeycloakProvider keycloakProvider;

  @Inject
  public KeycloakAsyncRequestFactory(
      KeycloakProvider keycloakProvider,
      DtoFactory dtoFactory,
      Provider<MachineTokenServiceClient> machineTokenServiceProvider,
      AppContext appContext,
      EventBus eventBus) {
    super(dtoFactory, machineTokenServiceProvider, appContext, eventBus);
    this.dtoFactory = dtoFactory;
    this.keycloakProvider = keycloakProvider;
  }

  @Override
  protected AsyncRequest doCreateRequest(
      RequestBuilder.Method method, String url, Object dtoBody, boolean async) {
    AsyncRequest request = super.doCreateRequest(method, url, dtoBody, async);
    if (!isWsAgentRequest(url)) {
      AsyncRequest asyncRequest = new KeycloakAsyncRequest(keycloakProvider, method, url, async);
      if (dtoBody != null) {
        if (dtoBody instanceof List<?>) {
          asyncRequest.data(dtoFactory.toJson((List<?>) dtoBody));
        } else if (dtoBody instanceof String) {
          asyncRequest.data((String) dtoBody);
        } else {
          asyncRequest.data(dtoFactory.toJson(dtoBody));
        }
        asyncRequest.header(HTTPHeader.CONTENT_TYPE, MimeType.APPLICATION_JSON);
      } else if (method.equals(RequestBuilder.POST) || method.equals(RequestBuilder.PUT)) {

        /*
          Here we need to setup wildcard mime type in content-type header, because CORS filter
          responses with 403 error in case if user makes POST/PUT request with null body and without
          content-type header. Setting content-type header with wildcard mime type solves this problem.
          Note, this issue need to be investigated, because the problem may be occurred as a bug in
          CORS filter.
        */

        asyncRequest.header(HTTPHeader.CONTENT_TYPE, MimeType.WILDCARD);
      }
      return asyncRequest;
    }
    return request;
  }

  public static native String getKeycloakSettings(String keycloakSettingsEndpoint) /*-{
        var myReq = new XMLHttpRequest();
        myReq.open('GET', '' + keycloakSettingsEndpoint, false);
        myReq.send(null);
        return myReq.responseText;
    }-*/;

  public static native JavaScriptObject getWindow() /*-{
        return $wnd;
    }-*/;
}
