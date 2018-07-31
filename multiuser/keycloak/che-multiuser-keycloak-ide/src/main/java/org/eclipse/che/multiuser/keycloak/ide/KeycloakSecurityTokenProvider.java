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
package org.eclipse.che.multiuser.keycloak.ide;

import javax.inject.Inject;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.security.oauth.SecurityTokenProvider;

public class KeycloakSecurityTokenProvider extends SecurityTokenProvider {
  @Inject KeycloakProvider keycloakProvider;

  @Override
  public Promise<String> getSecurityToken() {
    if (keycloakProvider.isKeycloakDisabled()) {
      return super.getSecurityToken();
    } else {
      return keycloakProvider.getUpdatedToken(5);
    }
  }
}
