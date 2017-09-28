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
package org.eclipse.che.multiuser.keycloak.ide;

import javax.inject.Inject;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.security.oauth.SecurityTokenProvider;

public class KeycloakSecurityTokenProvider extends SecurityTokenProvider {
  @Inject KeycloakProvider keycloakProvider;

  @Override
  public Promise<String> getSecurityToken() {
    return keycloakProvider.getUpdatedToken(5);
  }
}
