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
package org.eclipse.che.api.core.rest;

import org.eclipse.che.api.core.rest.shared.dto.Link;

import javax.inject.Singleton;
import javax.validation.constraints.NotNull;

/**
 * Creates {@link DefaultHttpJsonRequest} instances.
 *
 * @author Yevhenii Voevodin
 */
@Singleton
public class DefaultHttpJsonRequestFactory implements HttpJsonRequestFactory {

    @Override
    public HttpJsonRequest fromUrl(@NotNull String url) {
        return new DefaultHttpJsonRequest(url);
    }

    @Override
    public HttpJsonRequest fromLink(@NotNull Link link) {
        return new DefaultHttpJsonRequest(link);
    }
}
