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
package org.eclipse.che.ide.api.user;

import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.user.shared.dto.ProfileDescriptor;
import org.eclipse.che.ide.rest.AsyncRequestCallback;

import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * GWT Client for User Profile Service.
 *
 * @author Ann Shumilova
 */
public interface UserProfileServiceClient {

    /**
     * Get current user's profile.
     *
     * @param callback
     */
    void getCurrentProfile(AsyncRequestCallback<ProfileDescriptor> callback);

    /**
     * Update current user's profile.
     *
     * @param updates
     *         attributes to update
     * @param callback
     */
    void updateCurrentProfile(@NotNull Map<String, String> updates, AsyncRequestCallback<ProfileDescriptor> callback);

    /**
     * Get profile by id.
     *
     * @param id
     *         profile's id
     * @param callback
     */
    void getProfileById(@NotNull String id, AsyncRequestCallback<ProfileDescriptor> callback);

    /**
     * Get user preferences
     *
     * @param callback
     *         which contains some action with user preferences
     * @deprecated use {@link #getPreferences()}
     */
    @Deprecated
    void getPreferences(AsyncRequestCallback<Map<String, String>> callback);

    /**
     * Get user preferences
     *
     * @return the promise which either uses preferences for some actions or rejects with an error
     */
    Promise<Map<String, String>> getPreferences();

    /**

    /**
     * Update profile.
     *
     * @param id
     *         profile's id
     * @param updates
     *         attributes to update
     * @param callback
     */
    void updateProfile(@NotNull String id, Map<String, String> updates, AsyncRequestCallback<ProfileDescriptor> callback);

    /**
     * Update preferences.
     *
     * @param prefsToUpdate
     *         preferences to update
     * @param callback
     *         which contains some action with user preferences
     * @deprecated use {@link #updatePreferences(Map)}
     */
    @Deprecated
    void updatePreferences(@NotNull Map<String, String> prefsToUpdate, AsyncRequestCallback<Map<String, String>> callback);

    /**
     * Update preferences.
     *
     * @param prefsToUpdate
     * @return promise which either uses preferences for some actions or rejects with an error
     */
    Promise<Map<String, String>> updatePreferences(@NotNull Map<String, String> prefsToUpdate);
}
