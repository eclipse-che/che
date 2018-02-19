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
package org.eclipse.che.multiuser.keycloak.server;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.core.rest.DefaultHttpJsonRequestFactory;
import org.eclipse.che.api.core.rest.HttpJsonRequest;
import org.eclipse.che.api.core.rest.shared.dto.Link;

/** Creates {@link KeycloakHttpJsonRequest} instances. */
@Singleton
public class KeycloakHttpJsonRequestFactory extends DefaultHttpJsonRequestFactory {

  @Inject
  public KeycloakHttpJsonRequestFactory() {}

  @Override
  public HttpJsonRequest fromUrl(@NotNull String url) {
    return new KeycloakHttpJsonRequest(url);
  }

  @Override
  public HttpJsonRequest fromLink(@NotNull Link link) {
    return new KeycloakHttpJsonRequest(link);
  }
}
