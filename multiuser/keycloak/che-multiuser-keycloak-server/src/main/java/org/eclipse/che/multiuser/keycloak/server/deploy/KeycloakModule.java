/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.multiuser.keycloak.server.deploy;

import com.google.inject.AbstractModule;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.api.user.server.TokenValidator;
import org.eclipse.che.api.user.server.spi.ProfileDao;
import org.eclipse.che.multiuser.api.account.personal.PersonalAccountUserManager;
import org.eclipse.che.multiuser.keycloak.server.KeycloakConfigurationService;
import org.eclipse.che.multiuser.keycloak.server.KeycloakTokenValidator;
import org.eclipse.che.multiuser.keycloak.server.KeycloakUserManager;
import org.eclipse.che.multiuser.keycloak.server.dao.KeycloakProfileDao;
import org.eclipse.che.multiuser.keycloak.server.oauth2.KeycloakOAuthAuthenticationService;

public class KeycloakModule extends AbstractModule {
  @Override
  protected void configure() {

    bind(HttpJsonRequestFactory.class)
        .to(org.eclipse.che.multiuser.keycloak.server.KeycloakHttpJsonRequestFactory.class);
    bind(TokenValidator.class).to(KeycloakTokenValidator.class);
    bind(KeycloakConfigurationService.class);
    bind(KeycloakOAuthAuthenticationService.class);

    bind(ProfileDao.class).to(KeycloakProfileDao.class);
    bind(PersonalAccountUserManager.class).to(KeycloakUserManager.class);
  }
}
