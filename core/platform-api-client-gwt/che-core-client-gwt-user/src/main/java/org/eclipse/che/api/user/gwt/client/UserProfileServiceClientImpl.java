/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.user.gwt.client;

import com.google.inject.Inject;

import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.user.shared.dto.ProfileDescriptor;
import org.eclipse.che.ide.json.JsonHelper;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.AsyncRequestFactory;
import org.eclipse.che.ide.rest.RestContext;
import org.eclipse.che.ide.rest.StringMapUnmarshaller;
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
    private final String              PREFS;
    private final LoaderFactory       loaderFactory;
    private final AsyncRequestFactory asyncRequestFactory;

    @Inject
    protected UserProfileServiceClientImpl(@RestContext String restContext,
                                           LoaderFactory loaderFactory,
                                           AsyncRequestFactory asyncRequestFactory) {
        this.loaderFactory = loaderFactory;
        this.asyncRequestFactory = asyncRequestFactory;
        PROFILE = restContext + "/profile/";
        PREFS = PROFILE + "prefs";
    }

    /** {@inheritDoc} */
    @Override
    public void getCurrentProfile(AsyncRequestCallback<ProfileDescriptor> callback) {
        asyncRequestFactory.createGetRequest(PROFILE)
                           .header(ACCEPT, APPLICATION_JSON)
                           .loader(loaderFactory.newLoader("Retrieving current user's profile..."))
                           .send(callback);
    }

    /** {@inheritDoc} */
    @Override
    public void updateCurrentProfile(@NotNull Map<String, String> updates, AsyncRequestCallback<ProfileDescriptor> callback) {
        asyncRequestFactory.createPostRequest(PROFILE, null)
                           .header(ACCEPT, APPLICATION_JSON)
                           .header(CONTENT_TYPE, APPLICATION_JSON)
                           .data(JsonHelper.toJson(updates))
                           .loader(loaderFactory.newLoader("Updating current user's profile..."))
                           .send(callback);
    }

    /** {@inheritDoc} */
    @Override
    public void getProfileById(@NotNull String id, AsyncRequestCallback<ProfileDescriptor> callback) {
        String requestUrl = PROFILE + id;

        asyncRequestFactory.createGetRequest(requestUrl)
                           .header(ACCEPT, APPLICATION_JSON)
                           .loader(loaderFactory.newLoader("Getting user's profile..."))
                           .send(callback);
    }

    @Override
    public void getPreferences(AsyncRequestCallback<Map<String, String>> callback) {
        asyncRequestFactory.createGetRequest(PREFS)
                           .header(ACCEPT, APPLICATION_JSON)
                           .header(CONTENT_TYPE, APPLICATION_JSON)
                           .loader(loaderFactory.newLoader("Getting user's preferences..."))
                           .send(callback);
    }

    @Override
    public Promise<Map<String, String>> getPreferences() {
        return asyncRequestFactory.createGetRequest(PREFS)
                                  .header(ACCEPT, APPLICATION_JSON)
                                  .header(CONTENT_TYPE, APPLICATION_JSON)
                                  .loader(loaderFactory.newLoader("Getting user's preferences..."))
                                  .send(new StringMapUnmarshaller());
    }

    /** {@inheritDoc} */
    @Override
    public void updateProfile(@NotNull String id, Map<String, String> updates, AsyncRequestCallback<ProfileDescriptor> callback) {
        String requestUrl = PROFILE + id;

        asyncRequestFactory.createPostRequest(requestUrl, null)
                           .header(ACCEPT, APPLICATION_JSON)
                           .header(CONTENT_TYPE, APPLICATION_JSON)
                           .data(JsonHelper.toJson(updates))
                           .loader(loaderFactory.newLoader("Updating user's profile..."))
                           .send(callback);
    }

    /** {@inheritDoc} */
    @Override
    public void updatePreferences(@NotNull Map<String, String> update, AsyncRequestCallback<Map<String, String>> callback) {
        final String data = JsonHelper.toJson(update);
        asyncRequestFactory.createPostRequest(PREFS, null)
                           .header(ACCEPT, APPLICATION_JSON)
                           .header(CONTENT_TYPE, APPLICATION_JSON)
                           .data(data)
                           .loader(loaderFactory.newLoader("Updating user's preferences..."))
                           .send(callback);
    }

    /** {@inheritDoc} */
    @Override
    public Promise<Map<String, String>> updatePreferences(@NotNull Map<String, String> update) {
        final String data = JsonHelper.toJson(update);
        return asyncRequestFactory.createPostRequest(PREFS, null)
                                  .header(ACCEPT, APPLICATION_JSON)
                                  .header(CONTENT_TYPE, APPLICATION_JSON)
                                  .data(data)
                                  .loader(loaderFactory.newLoader("Updating user's preferences..."))
                                  .send(new StringMapUnmarshaller());
    }
}
