/*******************************************************************************
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.keycloak.server.deploy;

import com.google.inject.servlet.ServletModule;

import org.eclipse.che.keycloak.server.KeycloakAuthenticationFilter;
import org.eclipse.che.keycloak.server.KeycloakEnvironmentInitalizationFilter;

import javax.inject.Singleton;


public class KeycloakServletModule extends ServletModule {
    @Override
    protected void configureServlets() {
        bind(KeycloakAuthenticationFilter.class).in(Singleton.class);

        // Not contains '/websocket', /docs/ (for swagger) and not ends with '/ws' or '/eventbus' or '/settings/'
        filterRegex("^(?!.*(/websocket/?|/docs/))(?!.*(/ws/?|/eventbus/?|/settings/?)$).*").through(KeycloakAuthenticationFilter.class);
        filterRegex("^(?!.*(/websocket/?|/docs/))(?!.*(/ws/?|/eventbus/?|/settings/?)$).*").through(KeycloakEnvironmentInitalizationFilter.class);
    }
}
