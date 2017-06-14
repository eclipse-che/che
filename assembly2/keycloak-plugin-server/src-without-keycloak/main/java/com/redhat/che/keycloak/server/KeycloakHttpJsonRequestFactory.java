package com.redhat.che.keycloak.server;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;

import org.eclipse.che.api.core.rest.DefaultHttpJsonRequestFactory;

/**
 * 
 */
@Singleton
public class KeycloakHttpJsonRequestFactory extends DefaultHttpJsonRequestFactory {

    @Inject
    public KeycloakHttpJsonRequestFactory() {
    }

}