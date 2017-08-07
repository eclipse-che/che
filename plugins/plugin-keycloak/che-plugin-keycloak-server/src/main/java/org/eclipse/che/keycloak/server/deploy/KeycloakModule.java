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
package org.eclipse.che.keycloak.server.deploy;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.name.Names;

import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.api.user.server.TokenValidator;
import org.eclipse.che.keycloak.server.KeycloakConfigurationService;
import org.eclipse.che.keycloak.server.KeycloakTokenValidator;
import org.eclipse.che.keycloak.shared.KeycloakConstants;
import org.keycloak.adapters.KeycloakConfigResolver;


public class KeycloakModule extends AbstractModule {
    @Override
    protected void configure() {

        MapBinder<String, String> redirects = MapBinder.newMapBinder(
                binder(),
                String.class,
                String.class, Names.named(KeycloakConstants.REWRITE_RULE_SETTING));
        redirects.addBinding("^/wsmaster/api/(.*)$").toInstance("/api/$1");


        bind(HttpJsonRequestFactory.class).to(org.eclipse.che.keycloak.server.KeycloakHttpJsonRequestFactory.class);
        bind(TokenValidator.class).to(KeycloakTokenValidator.class);
        bind(KeycloakConfigResolver.class).to(org.eclipse.che.keycloak.server.WsMasterKeycloakConfigResolver.class);
        bind(KeycloakConfigurationService.class);
    }
}
