package com.redhat.che.keycloak.ide;

import org.eclipse.che.ide.rest.AsyncRequest;

import com.google.gwt.http.client.RequestBuilder;

/**
 * KeycloakAsyncRequests
 */
public class KeycloakAsyncRequest extends AsyncRequest{

    public KeycloakAsyncRequest (RequestBuilder.Method method,
                                String url, 
                                boolean async) {
        super(method, url, async);
    }
}