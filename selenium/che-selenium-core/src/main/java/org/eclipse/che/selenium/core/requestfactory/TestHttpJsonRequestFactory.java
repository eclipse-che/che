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
package org.eclipse.che.selenium.core.requestfactory;

import org.eclipse.che.api.core.rest.DefaultHttpJsonRequestFactory;
import org.eclipse.che.api.core.rest.HttpJsonRequest;
import org.eclipse.che.api.core.rest.shared.dto.Link;

import javax.validation.constraints.NotNull;

/**
 * @author Dmytro Nochevnov
 */
public abstract class TestHttpJsonRequestFactory extends DefaultHttpJsonRequestFactory {
    @Override
    public HttpJsonRequest fromUrl(@NotNull String url) {
        return super.fromUrl(url)
                    .setAuthorizationHeader(getAuthToken());
    }

    @Override
    public HttpJsonRequest fromLink(@NotNull Link link) {
        return super.fromLink(link)
                    .setAuthorizationHeader(getAuthToken());
    }

    abstract protected String getAuthToken();
}
