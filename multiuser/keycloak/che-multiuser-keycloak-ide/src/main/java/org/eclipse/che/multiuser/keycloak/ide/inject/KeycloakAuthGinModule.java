/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
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
