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
package org.eclipse.che.keycloak.ide;

import com.google.gwt.http.client.RequestBuilder;

import org.eclipse.che.ide.rest.AsyncRequest;

/**
 * KeycloakAsyncRequests
 */
public class KeycloakAsyncRequest extends AsyncRequest{

    public KeycloakAsyncRequest (RequestBuilder.Method method,
                                String url, 
                                boolean async) {
        super(method, url, async);
    }
}
