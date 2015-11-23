/*******************************************************************************
 * Copyright (c) 2012-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.ext.java.server;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.regex.Pattern;

/**
 * Provides value of web socket url to set up event bus between machine and api.
 *
 * @author Anton Korneta
 */
public class EventBusURLProvider extends ApiEndpointProvider {
    /** changes url protocol to web socket e.g https: to ws: */
    private static final Pattern URL_PROTOCOL_PATTERN = Pattern.compile("([A-Za-z]{3,9}:)");

    @Inject
    @Named("user.token")
    String token;

    @Override
    public String get() {
        return URL_PROTOCOL_PATTERN.matcher(super.get()).replaceFirst("ws:") + "/eventbus/?token=" + token;
    }
}
