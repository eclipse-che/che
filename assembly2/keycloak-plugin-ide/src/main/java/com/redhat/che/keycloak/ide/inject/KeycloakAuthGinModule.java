package com.redhat.che.keycloak.ide.inject;

import org.eclipse.che.ide.api.extension.ExtensionGinModule;
import org.eclipse.che.ide.rest.AsyncRequestFactory;

import com.google.gwt.inject.client.AbstractGinModule;

/**
 * KeycloakAuthGinModule
 */
@ExtensionGinModule
public class KeycloakAuthGinModule extends AbstractGinModule{
    
    @Override
    public void configure(){
        bind(AsyncRequestFactory.class).to(com.redhat.che.keycloak.ide.KeycloakAsyncRequestFactory.class);
    }
}