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
package org.eclipse.che.wsagent.server;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.core.rest.DefaultHttpJsonRequestFactory;
import org.eclipse.che.api.core.rest.HttpJsonRequest;
import org.eclipse.che.api.core.rest.shared.dto.Link;

@Singleton
public class AgentHttpJsonRequestFactory extends DefaultHttpJsonRequestFactory {

  private final String TOKEN;

  @Inject
  public AgentHttpJsonRequestFactory(@Named("user.token") String token) {
    this.TOKEN = token;
  }

  @Override
  public HttpJsonRequest fromUrl(@NotNull String url) {
    return super.fromUrl(url).setAuthorizationHeader(TOKEN);
  }

  @Override
  public HttpJsonRequest fromLink(@NotNull Link link) {
    return super.fromLink(link).setAuthorizationHeader(TOKEN);
  }
}
