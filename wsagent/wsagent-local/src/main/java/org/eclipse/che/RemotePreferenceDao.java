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

import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.UnauthorizedException;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.api.user.server.spi.PreferenceDao;
import org.eclipse.che.commons.env.EnvironmentContext;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * Delegates calls to {@link UserProfileService}.
 *
 * @author Yevhenii Voevodin
 */
@Deprecated
@Singleton
public class RemotePreferenceDao implements PreferenceDao {

    private final String                 prefsUrl;
    private final HttpJsonRequestFactory requestFactory;

    @Inject
    public RemotePreferenceDao(@Named("che.api") String apiUrl, HttpJsonRequestFactory requestFactory) {
        this.prefsUrl = apiUrl + "/preferences";
        this.requestFactory = requestFactory;
    }

    @Override
    public void setPreferences(String userId, Map<String, String> preferences) throws ServerException {
        requireNonNull(preferences, "Required non-null preferences");
        checkUserId(requireNonNull(userId, "Required non-null user id"));
        try {
            requestFactory.fromUrl(prefsUrl)
                          .usePostMethod()
                          .setBody(preferences)
                          .request();
        } catch (IOException | UnauthorizedException | ForbiddenException | ConflictException | NotFoundException | BadRequestException ex) {
            throw new ServerException(ex.getLocalizedMessage(), ex);
        }
    }

    @Override
    public Map<String, String> getPreferences(String userId) throws ServerException {
        checkUserId(requireNonNull(userId, "Required non-null user id"));
        try {
            return requestFactory.fromUrl(prefsUrl)
                                 .useGetMethod()
                                 .request()
                                 .asProperties();
        } catch (IOException | UnauthorizedException | ForbiddenException | ConflictException | NotFoundException | BadRequestException ex) {
            throw new ServerException(ex.getLocalizedMessage(), ex);
        }
    }

    @Override
    public Map<String, String> getPreferences(String userId, String filter) throws ServerException {
        requireNonNull(filter, "Required non-null filter");
        checkUserId(requireNonNull(userId, "Required non-null user id"));
        try {
            return requestFactory.fromUrl(prefsUrl)
                                 .useGetMethod()
                                 .addQueryParam("filter", filter)
                                 .request()
                                 .asProperties();
        } catch (IOException | UnauthorizedException | ForbiddenException | ConflictException | NotFoundException | BadRequestException ex) {
            throw new ServerException(ex.getLocalizedMessage(), ex);
        }
    }

    /**
     * User identifier {@code userId} is not used as this implementation delegates {@link UserProfileService#removePreferences(List)} (String)}.
     */
    @Override
    public void remove(String userId) throws ServerException {
        checkUserId(requireNonNull(userId, "Required non-null user id"));
        try {
            requestFactory.fromUrl(prefsUrl)
                          .useDeleteMethod()
                          .request();
        } catch (IOException | UnauthorizedException | ForbiddenException | ConflictException | NotFoundException | BadRequestException ex) {
            throw new ServerException(ex.getLocalizedMessage(), ex);
        }
    }

    /**
     * Checks that {@code userId} is equal to current user id.
     */
    private void checkUserId(String userId) throws ServerException {
        if (!EnvironmentContext.getCurrent().getSubject().getUserId().equals(userId)) {
            throw new ServerException("This method is not allowed for user '" + userId + "'");
        }
    }
}

