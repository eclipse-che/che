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
package org.eclipse.che.multiuser.keycloak.ide.inject;

import com.google.gwt.inject.client.AbstractGinModule;
import org.eclipse.che.ide.api.extension.ExtensionGinModule;
import org.eclipse.che.ide.rest.AsyncRequestFactory;
import org.eclipse.che.multiuser.keycloak.ide.KeycloakProvider;
import org.eclipse.che.multiuser.keycloak.ide.KeycloakSecurityTokenProvider;
import org.eclipse.che.security.oauth.SecurityTokenProvider;

/** KeycloakAuthGinModule */
@ExtensionGinModule
public class KeycloakAuthGinModule extends AbstractGinModule {

  @Override
  public void configure() {
    bind(KeycloakProvider.class).asEagerSingleton();
    bind(AsyncRequestFactory.class)
        .to(org.eclipse.che.multiuser.keycloak.ide.KeycloakAsyncRequestFactory.class);
    bind(SecurityTokenProvider.class).to(KeycloakSecurityTokenProvider.class);
  }
}
