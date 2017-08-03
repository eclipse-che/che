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

import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.api.user.server.TokenValidator;
import org.eclipse.che.keycloak.server.KeycloakConfigurationService;
import org.eclipse.che.keycloak.server.KeycloakTokenValidator;


public class KeycloakModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(HttpJsonRequestFactory.class).to(org.eclipse.che.keycloak.server.KeycloakHttpJsonRequestFactory.class);
        bind(TokenValidator.class).to(KeycloakTokenValidator.class);
        bind(KeycloakConfigurationService.class);
    }
}
