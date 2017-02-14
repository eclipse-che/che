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
package org.eclipse.che.ide.user;

import com.google.inject.Inject;

import org.eclipse.che.api.user.shared.dto.ProfileDto;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.user.UserProfileServiceClient;
import org.eclipse.che.ide.json.JsonHelper;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.AsyncRequestFactory;
import org.eclipse.che.ide.ui.loaders.request.LoaderFactory;

import javax.validation.constraints.NotNull;
import java.util.Map;

import static org.eclipse.che.ide.MimeType.APPLICATION_JSON;
import static org.eclipse.che.ide.rest.HTTPHeader.ACCEPT;
import static org.eclipse.che.ide.rest.HTTPHeader.CONTENT_TYPE;

/**
 * Implementation for {@link UserProfileServiceClient}.
 *
 * @author Ann Shumilova
 */
public class UserProfileServiceClientImpl implements UserProfileServiceClient {
    private final String              PROFILE;
    private final LoaderFactory       loaderFactory;
    private final AsyncRequestFactory asyncRequestFactory;

    @Inject
    protected UserProfileServiceClientImpl(AppContext appContext,
                                           LoaderFactory loaderFactory,
                                           AsyncRequestFactory asyncRequestFactory) {
        this.loaderFactory = loaderFactory;
        this.asyncRequestFactory = asyncRequestFactory;
        PROFILE = appContext.getMasterEndpoint() + "/profile/";
    }

    /** {@inheritDoc} */
    @Override
    public void getCurrentProfile(AsyncRequestCallback<ProfileDto> callback) {
        asyncRequestFactory.createGetRequest(PROFILE)
                           .header(ACCEPT, APPLICATION_JSON)
                           .loader(loaderFactory.newLoader("Retrieving current user's profile..."))
                           .send(callback);
    }

    /** {@inheritDoc} */
    @Override
    public void updateCurrentProfile(@NotNull Map<String, String> updates, AsyncRequestCallback<ProfileDto> callback) {
        asyncRequestFactory.createPostRequest(PROFILE, null)
                           .header(ACCEPT, APPLICATION_JSON)
                           .header(CONTENT_TYPE, APPLICATION_JSON)
                           .data(JsonHelper.toJson(updates))
                           .loader(loaderFactory.newLoader("Updating current user's profile..."))
                           .send(callback);
    }

    /** {@inheritDoc} */
    @Override
    public void getProfileById(@NotNull String id, AsyncRequestCallback<ProfileDto> callback) {
        String requestUrl = PROFILE + id;

        asyncRequestFactory.createGetRequest(requestUrl)
                           .header(ACCEPT, APPLICATION_JSON)
                           .loader(loaderFactory.newLoader("Getting user's profile..."))
                           .send(callback);
    }

    /** {@inheritDoc} */
    @Override
    public void updateProfile(@NotNull String id, Map<String, String> updates, AsyncRequestCallback<ProfileDto> callback) {
        String requestUrl = PROFILE + id;

        asyncRequestFactory.createPostRequest(requestUrl, null)
                           .header(ACCEPT, APPLICATION_JSON)
                           .header(CONTENT_TYPE, APPLICATION_JSON)
                           .data(JsonHelper.toJson(updates))
                           .loader(loaderFactory.newLoader("Updating user's profile..."))
                           .send(callback);
    }
}
