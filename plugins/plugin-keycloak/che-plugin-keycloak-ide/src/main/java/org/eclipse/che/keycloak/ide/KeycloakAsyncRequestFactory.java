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
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.rest.AsyncRequest;
import org.eclipse.che.ide.rest.HTTPHeader;
import org.eclipse.che.machine.authentication.ide.MachineAsyncRequestFactory;
import org.eclipse.che.machine.authentication.ide.MachineTokenServiceClient;

/**
 * KeycloakAuthAsyncRequestFactory
 */
@Singleton
public class KeycloakAsyncRequestFactory extends MachineAsyncRequestFactory {

    @Inject
    public KeycloakAsyncRequestFactory(DtoFactory dtoFactory,
                                      Provider<MachineTokenServiceClient> machineTokenServiceProvider,
                                      AppContext appContext,
                                      EventBus eventBus) {
        super(dtoFactory, machineTokenServiceProvider, appContext, eventBus);
    }

    @Override
    protected AsyncRequest newAsyncRequest(RequestBuilder.Method method, String url, boolean async) {
        AsyncRequest request  = super.newAsyncRequest(method, url, async);
        if (!isWsAgentRequest(url)) {
            request.header(HTTPHeader.AUTHORIZATION, getBearerToken());
        }
        return request;
    }


    public static native String getBearerToken() /*-{
        //$wnd.keycloak.updateToken(10);
        return "Bearer " + $wnd.keycloak.token;
    }-*/;
}
