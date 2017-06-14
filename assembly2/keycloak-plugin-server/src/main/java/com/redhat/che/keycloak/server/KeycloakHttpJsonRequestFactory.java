package com.redhat.che.keycloak.server;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;

import org.eclipse.che.api.core.rest.DefaultHttpJsonRequestFactory;
import org.eclipse.che.api.core.rest.HttpJsonRequest;
import org.eclipse.che.api.core.rest.shared.dto.Link;

/**
 * 
 */
@Singleton
public class KeycloakHttpJsonRequestFactory extends DefaultHttpJsonRequestFactory {

    @Inject
    public KeycloakHttpJsonRequestFactory() {
    }

    @Override
    public HttpJsonRequest fromUrl(@NotNull String url) {
        System.out.println(" setAuthorizationHeader for " + url);
        return super.fromUrl(url).setAuthorizationHeader("Internal");
    }

    @Override
    public HttpJsonRequest fromLink(@NotNull Link link) {
        System.out.println(" setAuthorizationHeader for " + link);       
        return super.fromLink(link).setAuthorizationHeader("Internal");
    }

}