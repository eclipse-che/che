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
package org.eclipse.che;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.ws.rs.core.UriBuilder;

/**
 * Provides value of web socket url to set up event bus between machine and api.
 *
 * @author Anton Korneta
 * @author Alexander Garagatyi
 */
public class EventBusURLProvider implements Provider<String> {
    @Inject
    @Named("user.token")
    String token;

    @Inject
    @Named("che.api")
    String apiEndpoint;

    @Override
    public String get() {
        return UriBuilder.fromUri(apiEndpoint)
                         .scheme(apiEndpoint.startsWith("https") ? "wss" : "ws")
                         .path("/eventbus/")
                         .queryParam("token", token)
                         .build()
                         .toString();
    }
}
