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

import javax.inject.Provider;
import java.net.URI;

/**
 * Provides URI of Che API endpoint for usage inside machine to be able to connect to host machine using docker host IP.
 *
 * @author Alexander Garagatyi
 */
public class UriApiEndpointProvider implements Provider<URI> {

    public static final String API_ENDPOINT_URL_VARIABLE = "CHE_API";

    @Override
    public URI get() {
        try {
            return new URI(System.getenv(API_ENDPOINT_URL_VARIABLE));
        } catch (Exception e) {
            throw new RuntimeException("System variable CHE_API contain invalid value of Che api endpoint:" +
                                       System.getenv(API_ENDPOINT_URL_VARIABLE));
        }
    }
}
