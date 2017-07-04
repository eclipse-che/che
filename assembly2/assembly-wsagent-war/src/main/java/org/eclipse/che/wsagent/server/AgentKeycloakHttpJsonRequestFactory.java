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
package org.eclipse.che.wsagent.server;

import org.eclipse.che.api.core.rest.DefaultHttpJsonRequestFactory;
import org.eclipse.che.api.core.rest.HttpJsonRequest;
import org.eclipse.che.api.core.rest.shared.dto.Link;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;


@Singleton
public class AgentKeycloakHttpJsonRequestFactory extends DefaultHttpJsonRequestFactory {

    private final String TOKEN;

    @Inject
    public AgentKeycloakHttpJsonRequestFactory(@Named("user.token") String token) {
        this.TOKEN = token;
    }

    @Override
    public HttpJsonRequest fromUrl(@NotNull String url) {
        return super.fromUrl(url).setAuthorizationHeader("bearer " + TOKEN);
    }

    @Override
    public HttpJsonRequest fromLink(@NotNull Link link) {
        return super.fromLink(link).setAuthorizationHeader("bearer " + TOKEN);
    }

}
