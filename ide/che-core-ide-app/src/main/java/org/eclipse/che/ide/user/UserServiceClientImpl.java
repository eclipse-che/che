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

import org.eclipse.che.api.user.shared.dto.UserDto;
import org.eclipse.che.ide.MimeType;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.user.UserServiceClient;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.AsyncRequestFactory;
import org.eclipse.che.ide.ui.loaders.request.LoaderFactory;

import javax.validation.constraints.NotNull;

import static com.google.gwt.http.client.RequestBuilder.DELETE;
import static org.eclipse.che.ide.rest.HTTPHeader.ACCEPT;
import static org.eclipse.che.ide.rest.HTTPHeader.CONTENT_TYPE;

/**
 * Implementation of {@link UserServiceClient}.
 *
 * @author Ann Shumilova
 */
public class UserServiceClientImpl implements UserServiceClient {
    private final String              USER;
    private final String              CREATE;
    private final String              FIND;
    private final String              PASSWORD;
    private final LoaderFactory       loaderFactory;
    private final AsyncRequestFactory asyncRequestFactory;

    @Inject
    protected UserServiceClientImpl(AppContext appContext,
                                    LoaderFactory loaderFactory,
                                    AsyncRequestFactory asyncRequestFactory) {
        this.loaderFactory = loaderFactory;
        this.asyncRequestFactory = asyncRequestFactory;
        USER = appContext.getMasterEndpoint() + "/user/";
        CREATE = USER + "create";
        FIND = USER + "find";
        PASSWORD = USER + "password";
    }

    /** {@inheritDoc} */
    @Override
    public void createUser(@NotNull String token, boolean isTemporary, AsyncRequestCallback<UserDto> callback) {
        StringBuilder requestUrl = new StringBuilder(CREATE);
        requestUrl.append("?token=").append(token).append("&temporary=").append(isTemporary);

        asyncRequestFactory.createPostRequest(requestUrl.toString(), null)
                           .header(ACCEPT, MimeType.APPLICATION_JSON)
                           .loader(loaderFactory.newLoader("Creating user..."))
                           .send(callback);
    }

    /** {@inheritDoc} */
    @Override
    public void getCurrentUser(AsyncRequestCallback<UserDto> callback) {

        asyncRequestFactory.createGetRequest(USER)
                           .header(ACCEPT, MimeType.APPLICATION_JSON)
                           .loader(loaderFactory.newLoader("Retrieving current user..."))
                           .send(callback);
    }

    /** {@inheritDoc} */
    @Override
    public void updatePassword(@NotNull String password, AsyncRequestCallback<Void> callback) {
        // TODO form parameter
        String requestUrl = PASSWORD + "?password=" + password;

        asyncRequestFactory.createPostRequest(requestUrl, null)
                           .header(CONTENT_TYPE, MimeType.APPLICATION_FORM_URLENCODED)
                           .loader(loaderFactory.newLoader("Updating user's password..."))
                           .send(callback);
    }

    /** {@inheritDoc} */
    @Override
    public void getUserById(@NotNull String id, AsyncRequestCallback<UserDto> callback) {
        String requestUrl = USER + id;

        asyncRequestFactory.createGetRequest(requestUrl)
                           .header(ACCEPT, MimeType.APPLICATION_JSON)
                           .loader(loaderFactory.newLoader("Retrieving user..."))
                           .send(callback);
    }

    /** {@inheritDoc} */
    @Override
    public void getUserByAlias(@NotNull String alias, AsyncRequestCallback<UserDto> callback) {
        String requestUrl = FIND + "?alias=" + alias;

        asyncRequestFactory.createGetRequest(requestUrl)
                           .header(ACCEPT, MimeType.APPLICATION_JSON)
                           .loader(loaderFactory.newLoader("Retrieving user..."))
                           .send(callback);
    }

    /** {@inheritDoc} */
    @Override
    public void removeUser(@NotNull String id, AsyncRequestCallback<Void> callback) {
        String requestUrl = USER + id;

        asyncRequestFactory.createRequest(DELETE, requestUrl, null, false)
                           .loader(loaderFactory.newLoader("Deleting user..."))
                           .send(callback);
    }

}
